/*
 * Copyright (c) 2016 Vassili Bykov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.newspeaklanguage.compiler.codegen;

import java.lang.invoke.MethodHandle;

import org.newspeaklanguage.compiler.Descriptor;
import org.newspeaklanguage.compiler.NamingPolicy;
import org.newspeaklanguage.compiler.ast.Argument;
import org.newspeaklanguage.compiler.ast.AstNode;
import org.newspeaklanguage.compiler.ast.Block;
import org.newspeaklanguage.compiler.ast.Category;
import org.newspeaklanguage.compiler.ast.ClassDecl;
import org.newspeaklanguage.compiler.ast.CodeUnit;
import org.newspeaklanguage.compiler.ast.LiteralBoolean;
import org.newspeaklanguage.compiler.ast.LiteralNil;
import org.newspeaklanguage.compiler.ast.LiteralNumber;
import org.newspeaklanguage.compiler.ast.LiteralString;
import org.newspeaklanguage.compiler.ast.MessagePattern;
import org.newspeaklanguage.compiler.ast.MessageSendNoReceiver;
import org.newspeaklanguage.compiler.ast.MessageSendWithReceiver;
import org.newspeaklanguage.compiler.ast.Method;
import org.newspeaklanguage.compiler.ast.Outer;
import org.newspeaklanguage.compiler.ast.Return;
import org.newspeaklanguage.compiler.semantics.EnclosingObjectReference;
import org.newspeaklanguage.compiler.semantics.RewrittenNodeVisitor;
import org.newspeaklanguage.compiler.ast.Self;
import org.newspeaklanguage.compiler.ast.SlotDefinition;
import org.newspeaklanguage.compiler.ast.Super;
import org.newspeaklanguage.compiler.semantics.LocalVariable;
import org.newspeaklanguage.compiler.semantics.VariableAssignment;
import org.newspeaklanguage.compiler.semantics.VariableReference;
import org.newspeaklanguage.runtime.Box;
import org.newspeaklanguage.runtime.Builtins;
import org.newspeaklanguage.runtime.Closure;
import org.newspeaklanguage.runtime.IntReturnStack;
import org.newspeaklanguage.runtime.MessageDispatcher;
import org.newspeaklanguage.runtime.NsObject;
import org.newspeaklanguage.runtime.ObjectFactory;
import org.newspeaklanguage.runtime.PassThroughMessageSendWrapper;
import org.newspeaklanguage.runtime.ReturnPrimitiveValue;
import org.newspeaklanguage.runtime.StandardObject;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

// TODO blocks are not fully implemented

// TODO super sends are not implemented

/**
 * The generic method code generator. Generates a Java method to represent either
 * an individual Newspeak method, or a block contained in one. Two subclasses
 * of this class handle the specifics of each of those tasks.
 *
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 *
 */
abstract class CodeGenerator implements RewrittenNodeVisitor {

  /**
   * Using the supplied MethodVisitor, pick and generate an optimal
   * instruction to load the specified int value.
   */
  public static void generateLoadInt(MethodVisitor methodWriter, int value) {
    if (-1 <= value && value <= 5) {
      methodWriter.visitInsn(Opcodes.ICONST_0 + value);
    } else if (-128 <= value && value <= 127) {
      methodWriter.visitIntInsn(Opcodes.BIPUSH, value);
    } else {
      methodWriter.visitIntInsn(Opcodes.SIPUSH, value);
    }
  }

  public static void generateLoadUndefined(MethodVisitor methodWriter) {
    methodWriter.visitFieldInsn(
        Opcodes.GETSTATIC,
        Descriptor.internalClassName(NsObject.class),
        "UNDEFINED",
        Descriptor.OBJECT_TYPE_DESCRIPTOR);
  }

  public static void generateCreateReturnPrimitiveValue(MethodVisitor methodWriter) {
    methodWriter.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        ReturnPrimitiveValue.INTERNAL_CLASS_NAME,
        "create",
        ReturnPrimitiveValue.FACTORY_DESCRIPTOR,
        false
    );
  }

  public static void prepareToReturnIntFromPairOnStack(MethodVisitor methodWriter) {
    // initial stack: int, Object=Undefined
    methodWriter.visitInsn(Opcodes.SWAP);
    // stack: Object=Undefined, int
    methodWriter.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        IntReturnStack.INTERNAL_CLASS_NAME,
        "push",
        IntReturnStack.PUSH_DESCRIPTOR,
        false
    );
    // stack: Object=Undefined, which is what we want on return
  }

  public static void prepareToReturnSingleIntOnStack(MethodVisitor methodWriter) {
    // initial stack: int
    methodWriter.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        IntReturnStack.INTERNAL_CLASS_NAME,
        "push",
        IntReturnStack.PUSH_DESCRIPTOR,
        false
    );
    generateLoadUndefined(methodWriter);
    // stack: Object=Undefined, which is what we want on return
  }

  public static void generateLoadIntFromStack(MethodVisitor methodWriter) {
    methodWriter.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        IntReturnStack.INTERNAL_CLASS_NAME,
        "pop",
        IntReturnStack.POP_DESCRIPTOR,
        false
    );
  }

  /*
   * Instance side
   */
  protected final ClassGenerator classGenerator;

  protected final CodeUnit rootNode;
  protected final MethodVisitor methodWriter;

  CodeGenerator(ClassGenerator classGenerator, CodeUnit rootNode, MethodVisitor methodVisitor) {
    this.classGenerator = classGenerator;
    this.rootNode = rootNode;
    this.methodWriter = methodVisitor;
  }

  public ClassGenerator classGenerator() {
    return classGenerator;
  }

  public ClassWriter classWriter() {
    return classGenerator.classWriter();
  }

  public void generate() {
    methodWriter.visitCode();
    generateMethodPrologue();
    generateBody();
    methodWriter.visitMaxs(0, 0); // args ignored; the writer is set up to compute these
    methodWriter.visitEnd();
  }

  @Override
  public void visitMethod(Method method) {
    unexpectedVisit(method);
  }

  public void visit(AstNode node) {
    node.accept(this);
  }

  /**
   * A method prologue takes care of initializing all int parts of temps to hold 0
   * and all boxed temps to hold a new Box instance.
   */
  protected void generateMethodPrologue() {
    rootNode.scope().forEachTemp(each -> {
      if (each.isBoxed()) {
        methodWriter.visitTypeInsn(Opcodes.NEW, Box.INTERNAL_CLASS_NAME);
        methodWriter.visitInsn(Opcodes.DUP);
        methodWriter.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            Box.INTERNAL_CLASS_NAME,
            "<init>",
            "()V",
            false);
        methodWriter.visitVarInsn(Opcodes.ASTORE, each.index() + 1);
      }
      generateLoadInt(methodWriter, 0);
      methodWriter.visitVarInsn(Opcodes.ISTORE, each.index());
    });
  }

  /**
   * Subclasses will implement this as appropriate for their type. Methods and
   * blocks differ in their default return value.
   */
  protected abstract void generateBody();


  @Override
  public void visitBlock(Block block) {
    BlockDefiner definer = block.definer();
    int copiedValueCount = block.scope().copiedVariableCount();
    // the bogus intReceiver
    generateLoadInt(methodWriter, 0);
    // Generate a constructor call of Closure in either the positional argument or
    // the varargs form, depending on the number of copied values.
    methodWriter.visitTypeInsn(Opcodes.NEW, Closure.INTERNAL_CLASS_NAME);
    methodWriter.visitInsn(Opcodes.DUP);
    // push implementation method handle
    methodWriter.visitFieldInsn(
        Opcodes.GETSTATIC,
        definer.internalClassName(),
        definer.fieldName(),
        Descriptor.ofType(MethodHandle.class));
    // push the current receiver
    methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
    // No need to push a bogus int; Closure constructors are not general purpose methods
    // and they don't have a leading int receiver argument.
    // push all copied values
    if (copiedValueCount <= Closure.MAX_POSITIONAL_COPIED_VALUES) {
      // fixed arg initializer
      block.scope().forEachCopiedVariable(each -> {
        LocalVariable here = rootNode.scope().localVariableNamed(each.name()).get(); // always found or the analyzer is broken
        methodWriter.visitVarInsn(Opcodes.ILOAD, here.index());
        methodWriter.visitVarInsn(Opcodes.ALOAD, here.index() + 1);
      });
    } else {
      // vararg initializer
      generateLoadInt(methodWriter, copiedValueCount * 2);
      methodWriter.visitTypeInsn(Opcodes.ANEWARRAY, Descriptor.OBJECT_INTERNAL_CLASS_NAME);
      int i = 0;
      for (LocalVariable copied : block.scope().copiedVariables()) {
        LocalVariable here = rootNode.scope().localVariableNamed(copied.name()).get(); // always found or the analyzer is broken
        methodWriter.visitInsn(Opcodes.DUP);
        generateLoadInt(methodWriter, i++);
        methodWriter.visitVarInsn(Opcodes.ILOAD, here.index());
        methodWriter.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            Descriptor.internalClassName(Integer.class),
            "valueOf",
            Descriptor.ofMethod(Integer.class, int.class),
            false);
        methodWriter.visitInsn(Opcodes.AASTORE);
        methodWriter.visitInsn(Opcodes.DUP);
        generateLoadInt(methodWriter, i++);
        methodWriter.visitVarInsn(Opcodes.ALOAD, here.index() + 1);
        methodWriter.visitInsn(Opcodes.AASTORE);
      }
    }
    methodWriter.visitMethodInsn(
        Opcodes.INVOKESPECIAL,
        Closure.INTERNAL_CLASS_NAME,
        "<init>",
        Closure.constructorDescriptor(copiedValueCount),
        false);
  }

  @Override
  public void visitLiteralNumber(LiteralNumber literalNumber) {
    generateLoadInt(methodWriter, literalNumber.value().intValue());
    generateLoadUndefined(methodWriter);
  }

  @Override
  public void visitLiteralString(LiteralString literalString) {
    String string = literalString.value();
    LiteralValue literal = classGenerator.lookupLiteral(string);
    if (literal == null) {
      literal = LiteralValue.forString(string);
      classGenerator.addLiteral(literal);
    }
    generateLoadInt(methodWriter, 0);
    literal.generateLoad(methodWriter);
  }

  @Override
  public void visitMessagePattern(MessagePattern messagePattern) {
    messagePattern.arguments().forEach(this::visit);
  }

  /**
   * Generate code for a receiverless message send. Depending on the lexical
   * context previously examined by the analyzer, this send may be a variable
   * reference, a send to an enclosing object, or a self send.
   */
  @Override
  public void visitMessageSendNoReceiver(MessageSendNoReceiver messageNode) {
    unexpectedVisit(messageNode); // this node is supposed to be rewritten and not visited for code generation
  }

  /**
   * Generate code for access to a lexically visible variable. If the variable
   * is defined in this scope, it's available as a method frame slot.
   * It it's defined in an outer scope, it's still available as a method frame
   * slot as a copied down argument. If the original binding is mutable, the
   * copied down argument is a Box.
   * <p>
   * There are three cases of access:
   * a getter, a regular setter (the result is the receiver) and
   * a "setter setter" (a double-colon setter, the result is the set value).
   * Each of those has the subcases of a regular and a boxed representation.
   */
  @Override
  public void visitVariableReference(VariableReference varNode) {
    int index = varNode.localVariable().index();
    if (varNode.localVariable().isBoxed()) {
      methodWriter.visitVarInsn(Opcodes.ALOAD, index + 1);
      methodWriter.visitTypeInsn(Opcodes.CHECKCAST, Box.INTERNAL_CLASS_NAME);
      methodWriter.visitInsn(Opcodes.DUP); // stack: Box, Box
      methodWriter.visitFieldInsn(Opcodes.GETFIELD, Box.INTERNAL_CLASS_NAME, "intValue", Descriptor.INT_TYPE_DESCRIPTOR);
      methodWriter.visitInsn(Opcodes.SWAP); // stack: int, Box
      methodWriter.visitFieldInsn(Opcodes.GETFIELD, Box.INTERNAL_CLASS_NAME, "value", Descriptor.OBJECT_TYPE_DESCRIPTOR);
      // stack: int, Object
    } else {
      methodWriter.visitVarInsn(Opcodes.ILOAD, index);
      methodWriter.visitVarInsn(Opcodes.ALOAD, index + 1);
    }
  }

  @Override
  public void visitVariableAssignment(VariableAssignment node) {
    LocalVariable local = node.localVariable();
    int index = local.index();
    if (node.isPassThrough()) {
      // foo::
      visit(node.expression());
      methodWriter.visitInsn(Opcodes.DUP2); // the result is an int/Object pair
      if (local.isBoxed()) {
        // foo:: boxed
        methodWriter.visitVarInsn(Opcodes.ALOAD, index); // stack: int, Object, Box
        methodWriter.visitTypeInsn(Opcodes.CHECKCAST, Box.INTERNAL_CLASS_NAME);
        methodWriter.visitInsn(Opcodes.DUP_X2); // stack: Box, int, Object, Box
        methodWriter.visitInsn(Opcodes.SWAP); // stack: Box, int, Box, Object
        methodWriter.visitFieldInsn(Opcodes.PUTFIELD, Box.INTERNAL_CLASS_NAME, "value", Descriptor.OBJECT_TYPE_DESCRIPTOR);
        methodWriter.visitFieldInsn(Opcodes.PUTFIELD, Box.INTERNAL_CLASS_NAME, "intValue", Descriptor.INT_TYPE_DESCRIPTOR);
      } else {
        // foo:: unboxed
        methodWriter.visitVarInsn(Opcodes.ASTORE, index + 1); // store the object part
        methodWriter.visitVarInsn(Opcodes.ISTORE, index); // store the int
      }
      // the original value pair is now on top
    } else {
      // foo:
      if (local.isBoxed()) {
        // foo: boxed
        visit(node.expression()); // stack: int, Object
        methodWriter.visitVarInsn(Opcodes.ALOAD, index + 1);
        methodWriter.visitTypeInsn(Opcodes.CHECKCAST, Box.INTERNAL_CLASS_NAME); // stack: int, Object, Box
        methodWriter.visitInsn(Opcodes.DUP_X2); // stack: Box, int, Object, Box
        methodWriter.visitInsn(Opcodes.SWAP); // stack: Box, int, Box, Object
        methodWriter.visitFieldInsn(Opcodes.PUTFIELD, Box.INTERNAL_CLASS_NAME, "value", Descriptor.OBJECT_TYPE_DESCRIPTOR);
        methodWriter.visitFieldInsn(Opcodes.PUTFIELD, Box.INTERNAL_CLASS_NAME, "intValue", Descriptor.INT_TYPE_DESCRIPTOR);
      } else {
        // foo: unboxed
        visit(node.expression());
        methodWriter.visitVarInsn(Opcodes.ASTORE, index + 1);
        methodWriter.visitVarInsn(Opcodes.ISTORE, index);
      }
      methodWriter.visitInsn(Opcodes.ICONST_0);
      methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
    }
  }

  @Override
  public void visitEnclosingObjectReference(EnclosingObjectReference node) {
    int scopeLevel = node.scopeLevel();
    generateLoadInt(methodWriter, 0);
    generateLoadOfEnclosingObject(scopeLevel);
  }

  /**
   * Get the enclosing receiver on the stack with the equivalent of:
   * {@code this.nsClass.enclosingObjects[scopeLevel]}
   * {@code this} is an instance of a StandardClass subclass, so no
   * CHECKCAST is needed prior to getting its nsClass.
   * <p>
   * Note: this only loads the enclosing object, not the full object/int value pair.
   */
  private void generateLoadOfEnclosingObject(int level) {
    methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
    methodWriter.visitFieldInsn(
        Opcodes.GETFIELD,
        StandardObject.INTERNAL_CLASS_NAME,
        "nsClass",
        ObjectFactory.TYPE_DESCRIPTOR);
    methodWriter.visitFieldInsn(
        Opcodes.GETFIELD,
        ObjectFactory.INTERNAL_CLASS_NAME,
        "enclosingObjects",
        StandardObject.ARRAY_TYPE_DESCRIPTOR);
    generateLoadInt(methodWriter, level);
    methodWriter.visitInsn(Opcodes.AALOAD);
  }

  /**
   * Generate (usually) a traditional Smalltalk-style message send to an explicit receiver.
   * As everywhere, the receiver and each argument are represented by two stack entries.
   * The answer is returned normally if an Object, or as an exception if an int.
   * We have to adapt the outcome back into an Object/int pair on the stack.
   */
  @Override
  public void visitMessageSendWithReceiver(MessageSendWithReceiver node) {
    if (node.isSetterSend()) {
      assert node.arity() == 1;
      visit(node.receiver());
      visit(node.arguments().get(0));
      methodWriter.visitInvokeDynamicInsn(
          NamingPolicy.methodNameForSelector(node.selector()),
          callSiteTypeDescriptor(node.arity()),
          PassThroughMessageSendWrapper.bootstrapHandle()
      );
    } else {
      node.receiver().accept(this);
      node.arguments().forEach(this::visit);
      methodWriter.visitInvokeDynamicInsn(
          NamingPolicy.methodNameForSelector(node.selector()),
          callSiteTypeDescriptor(node.arity()),
          MessageDispatcher.bootstrapHandle());
    }
    Label objectResult = new Label();
    Label end = new Label();
    methodWriter.visitInsn(Opcodes.DUP); // stack: Object, Object
    generateLoadUndefined(methodWriter); // stack: Object, Object, Undefined
    methodWriter.visitJumpInsn(Opcodes.IF_ACMPNE, objectResult); // stack: Object
    // the object is undefined, need int from stack
    generateLoadIntFromStack(methodWriter); // stack: Object, int
    methodWriter.visitJumpInsn(Opcodes.GOTO, end);
// objectResult:
    methodWriter.visitLabel(objectResult); // stack: Object
    generateLoadInt(methodWriter, 0); // stack: Object, int
// end:
    methodWriter.visitLabel(end);
    methodWriter.visitInsn(Opcodes.SWAP); // stack: int, Object

  }

  @Override
  public void visitLiteralNil(LiteralNil literalNil) {
    generateLoadInt(methodWriter, 0);
    methodWriter.visitInsn(Opcodes.ACONST_NULL);
  }

  @Override
  public void visitLiteralBoolean(LiteralBoolean literalBoolean) {
    generateLoadInt(methodWriter, 0);
    methodWriter.visitFieldInsn(
        Opcodes.GETSTATIC,
        Builtins.INTERNAL_CLASS_NAME,
        literalBoolean.value() ? "TRUE" : "FALSE",
        Descriptor.OBJECT_TYPE_DESCRIPTOR);
  }

  @Override
  public void visitSelf(Self self) {
    generateLoadInt(methodWriter, 0);
    methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
  }

  @Override
  public void visitSuper(Super superNode) {
    unimplemented(superNode);
  }

  @Override
  public void visitOuter(Outer outer) {
    int scopeLevel = outer.targetClassScope().level();
    generateLoadInt(methodWriter, 0);
    generateLoadOfEnclosingObject(scopeLevel);
  }

  @Override
  public void visitReturn(Return returnNode) {
    returnNode.expression().accept(this); // stack: int, Object
    methodWriter.visitInsn(Opcodes.DUP);
    generateLoadUndefined(methodWriter); // stack: int, Object, Object, Undefined
    Label objectPresent = new Label();
    Label end = new Label();
    methodWriter.visitJumpInsn(Opcodes.IF_ACMPNE, objectPresent); // stack: int, Object
    // Object undefined, int is the return value
    methodWriter.visitInsn(Opcodes.POP); // stack: int
    prepareToReturnSingleIntOnStack(methodWriter); // stack: Object
    methodWriter.visitJumpInsn(Opcodes.GOTO, end);
// objectPresent:
    methodWriter.visitLabel(objectPresent);
    methodWriter.visitInsn(Opcodes.SWAP); // stack: Object, int
    methodWriter.visitInsn(Opcodes.POP); // stack: Object
// end:
    methodWriter.visitLabel(end);
    methodWriter.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void visitClassDecl(ClassDecl classDecl) {
    unexpectedNode(classDecl);
  }

  @Override
  public void visitCategory(Category category) {
    unexpectedNode(category);
  }

  /**
   * Return a descriptor string for a call site of a message send of the given arity.
   * The arity we understand as the number of method arguments in Newspeak sense, not
   * counting the receiver. The call site descriptor string should also include the
   * Object/int pair representing the receiver.
   */
  private String callSiteTypeDescriptor(int arity) {
    StringBuilder result = new StringBuilder();
    result.append("(");
    for (int i = 0; i < arity + 1; i++) {
      result
          .append(Descriptor.INT_TYPE_DESCRIPTOR)
          .append(Descriptor.OBJECT_TYPE_DESCRIPTOR);
    }
    result.append(")");
    result.append(Descriptor.OBJECT_TYPE_DESCRIPTOR);
    return result.toString();
  }

  @Override
  public void visitArgument(Argument argument) {
    unexpectedVisit(argument);
  }

  @Override
  public void visitSlotDefinition(SlotDefinition slotDefinition) {
    unexpectedVisit(slotDefinition);
  }

  private void unexpectedNode(AstNode node) {
    throw new IllegalArgumentException("Unexpected node in a method AST: " + node);
  }

  private void unexpectedVisit(AstNode node) {
    throw new IllegalArgumentException("Unexpected direct visit of node: " + node);
  }

  private void unimplemented(AstNode node) {
    throw new UnsupportedOperationException("Code generation is not yet implemented for " + node);
  }
}
