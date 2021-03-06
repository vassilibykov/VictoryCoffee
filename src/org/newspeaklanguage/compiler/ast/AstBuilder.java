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

package org.newspeaklanguage.compiler.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.newspeaklanguage.compiler.parser.NewspeakParser;
import org.newspeaklanguage.compiler.parser.NewspeakParser.AccessModifierContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.BinaryMessageContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.BinarySendContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.BlockArgsContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.BlockContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.BlockTempsContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ClassBodyContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ClassHeaderContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ClassSideDeclContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.CodeBodyContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ExpressionContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ImmutableSlotInitializerContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.InstanceSideDeclContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.KeywordMessageContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.KeywordSendContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.MessageSendContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.MutableSlotInitializerContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.ReceiverContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.SetterSendContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.SpecialReceiverContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.StatementContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.UnaryMessageContext;
import org.newspeaklanguage.compiler.parser.NewspeakParser.UnarySendContext;
import org.newspeaklanguage.compiler.parser.NewspeakVisitor;

/**
 * The bridge between our ANTLR grammar and the AST defined in this package.
 * 
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 *
 */
public class AstBuilder implements NewspeakVisitor<AstNode> {

  @Override
  public AstNode visitSourceUnit(NewspeakParser.SourceUnitContext ctx) {
    // A source unit consists of a classDecl and an EOF;
    // need to handle this or we'd get the result of visiting the EOF.
    return visit(ctx.classDecl());
  }
  
  @Override
  public AstNode visitClassDecl(NewspeakParser.ClassDeclContext ctx) {
    
    // This class name and initializer
    String name = ctx.IDENTIFIER(0).getText();
    NewspeakParser.MessagePatternContext initCtx = ctx.messagePattern(0);
    MessagePattern initMessage = initCtx == null ? null : (MessagePattern) visit(initCtx);
    
    // Superclass name and initializer
    TerminalNode superclassNameCtx = ctx.IDENTIFIER(1); 
    String superclassName = superclassNameCtx == null ? null : superclassNameCtx.getText();
    NewspeakParser.MessagePatternContext superCtx = ctx.messagePattern(1);
    MessagePattern superclassInitMessage = superCtx == null ? null : (MessagePattern) visit(superCtx);
    
    List<SlotDefinition> slots = ctx.classBody().classHeader().slotDecl().stream()
        .map(each -> (SlotDefinition) visit(each))
        .collect(Collectors.toList());
// The following is better in theory, but too verbose
//        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    
    List<ClassDecl> nestedClasses = ctx.classBody().instanceSideDecl().classDecl().stream()
        .map(each -> (ClassDecl) visit(each))
        .collect(Collectors.toList());
    
    List<Category> instCategories = ctx.classBody().instanceSideDecl().category().stream()
        .map(each -> (Category) visit(each))
        .collect(Collectors.toList());
    
    NewspeakParser.ClassSideDeclContext classSide = ctx.classBody().classSideDecl();
    List<Category> classCategories = classSide == null
        ? null
        : classSide.category().stream()
            .map(each -> (Category) visit(each))
            .collect(Collectors.toList());
    
    return new ClassDecl(name, initMessage,
        superclassName, superclassInitMessage, 
        slots, nestedClasses, instCategories, classCategories);
  }
 
  @Override
  public AstNode visitSlotDecl(NewspeakParser.SlotDeclContext ctx) {
    String name = ctx.IDENTIFIER().getText();
    boolean immutable = ctx.immutableSlotInitializer() != null;
    return new SlotDefinition(name, null, !immutable);
  }
  
  @Override
  public AstNode visitCategory(NewspeakParser.CategoryContext ctx) {
    String name = ctx.STRING().getText();
    List<Method> methods = ctx.methodDecl().stream()
        .map(each -> (Method) visit(each))
        .collect(Collectors.toList());
    return new Category(name, methods);
  }
  
  @Override
  public AstNode visitMethodDecl(NewspeakParser.MethodDeclContext ctx) {
    MessagePattern pattern = (MessagePattern) visit(ctx.messagePattern());
    List<SlotDefinition> temps = ctx.slotDecl().stream()
        .map(each -> (SlotDefinition) visit(each))
        .collect(Collectors.toList());
    List<AstNode> statements = ctx.codeBody().statement().stream()
        .map(each -> visit(each))
        .collect(Collectors.toList());
    return new Method(pattern, temps, statements);
  }
  
  @Override
  public AstNode visitUnaryPattern(NewspeakParser.UnaryPatternContext ctx) {
    return new MessagePattern(ctx.IDENTIFIER().getText(), Collections.emptyList());
  }
  
  @Override
  public AstNode visitBinaryPattern(NewspeakParser.BinaryPatternContext ctx) {
    Argument arg = new Argument(ctx.IDENTIFIER().getText()); 
    return new MessagePattern(ctx.BINARY_SELECTOR().getText(), Arrays.asList(arg));
  }
  
  @Override
  public AstNode visitKeywordPattern(NewspeakParser.KeywordPatternContext ctx) {
    Optional<String> partialSelector = ctx.KEYWORD().stream()
        .map(each -> each.getText())
        .reduce(String::concat);
    String selector = partialSelector.get();
    List<Argument> args = ctx.IDENTIFIER().stream()
        .map(each -> new Argument(each.getText()))
        .collect(Collectors.toList());
    return new MessagePattern(selector, args);
  }
  
  @Override
  public AstNode visitNilReceiver(NewspeakParser.NilReceiverContext ctx) {
    return new LiteralNil();
  }
  
  @Override
  public AstNode visitTrueReceiver(NewspeakParser.TrueReceiverContext ctx) {
    return new LiteralBoolean(true);
  }
  
  @Override
  public AstNode visitFalseReceiver(NewspeakParser.FalseReceiverContext ctx) {
    return new LiteralBoolean(false);
  }
  
  @Override
  public AstNode visitSelfReceiver(NewspeakParser.SelfReceiverContext ctx) {
    return new Self();
  }
  
  @Override
  public AstNode visitSuperReceiver(NewspeakParser.SuperReceiverContext ctx) {
    return new Super();
  }
  
  @Override
  public AstNode visitOuterReceiver(NewspeakParser.OuterReceiverContext ctx) {
    return new Outer(ctx.IDENTIFIER().getText());
  }
  
  @Override
  public AstNode visitIntegerLiteral(NewspeakParser.IntegerLiteralContext ctx) {
    int intValue = Integer.parseInt(ctx.INTEGER().getText());
    return new LiteralNumber(intValue);
  }
  
  @Override
  public AstNode visitStringLiteral(NewspeakParser.StringLiteralContext ctx) {
    String tokenText = ctx.STRING().getText(); // with leading and trailing 's
    return new LiteralString(tokenText.substring(1, tokenText.length() - 1));
  }

  @Override
  public AstNode visitBlockLiteral(NewspeakParser.BlockLiteralContext ctx) {
    List<Argument> args = Optional.ofNullable(ctx.block().blockArgs())
        .map(it -> it.BLOCK_ARG().stream()
            .map(each -> each.getText().substring(1))
            .map(each -> new Argument(each))
            .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
    List<SlotDefinition> temps = Optional.ofNullable(ctx.block().blockTemps())
        .map(it -> it.slotDecl().stream()
            .map(each -> (SlotDefinition) visit(each))
            .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
    List<AstNode> statements = ctx.block().codeBody().statement().stream()
        .map(this::visit)
        .collect(Collectors.toList());
    return new Block(args, temps, statements);
  }

  @Override
  public AstNode visitReturnStatement(NewspeakParser.ReturnStatementContext ctx) {
    AstNode expr = visit(ctx.expression());
    return new Return(expr);
  }

  @Override
  public AstNode visit(ParseTree tree) {
    return tree.accept(this);
  }

  @Override
  public AstNode visitChildren(RuleNode node) {
    throw new UnsupportedOperationException("this should not be necessary");
  }

  @Override
  public AstNode visitTerminal(TerminalNode node) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitErrorNode(ErrorNode node) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitClassBody(ClassBodyContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitClassHeader(ClassHeaderContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitInstanceSideDecl(InstanceSideDeclContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitClassSideDecl(ClassSideDeclContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitMutableSlotInitializer(MutableSlotInitializerContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitImmutableSlotInitializer(ImmutableSlotInitializerContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitAccessModifier(AccessModifierContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitCodeBody(CodeBodyContext ctx) {
    throw new IllegalStateException("this node should never be visited");
  }

  @Override
  public AstNode visitStatement(StatementContext ctx) {
    return visitFirstChild(ctx);
  }

  @Override
  public AstNode visitExpression(ExpressionContext ctx) {
    return visitFirstChild(ctx);
   }

  @Override
  public AstNode visitMessageSend(MessageSendContext ctx) {
    return visitFirstChild(ctx);
  }

  @Override
  public AstNode visitReceiver(ReceiverContext ctx) {
    TerminalNode name = ctx.IDENTIFIER();
    if (name != null) {
      // The IDENTIFIER option
      return new MessageSendNoReceiver(name.getText(), false, Collections.emptyList());
    }
    if (ctx.expression() != null) {
      // The '(' expression ')' option
      return visit(ctx.expression());
    }
    return visitFirstChild(ctx);
 }

  @Override
  public AstNode visitBinaryObject(NewspeakParser.BinaryObjectContext ctx) {
    return visitFirstChild(ctx);
  }

  @Override
  public AstNode visitKeywordObject(NewspeakParser.KeywordObjectContext ctx) {
    return visitFirstChild(ctx);
  }

  @Override
  public AstNode visitSetterSend(SetterSendContext ctx) {
    String keyword = ctx.SETTER_KEYWORD().getText();
    // A setter keyword has two colons at the end; we only need one. 
    String selector = keyword.substring(0, keyword.length() - 1);
    AstNode arg = visit(ctx.expression());
    return new MessageSendNoReceiver(selector, true, Arrays.asList(arg));
  }

  @Override
  public AstNode visitUnarySend(UnarySendContext ctx) {
    AstNode receiver = ctx.receiver().accept(this);
    return makeMessageSend(receiver, ctx.unaryMessage());
  }

  @Override
  public AstNode visitUnaryMessage(UnaryMessageContext ctx) {
    throw new IllegalStateException("this node should never be visited directly");
  }

  @Override
  public AstNode visitBinarySend(BinarySendContext ctx) {
    AstNode receiver = ctx.binaryObject().accept(this);
    return makeMessageSend(receiver, ctx.binaryMessage());
  }

  @Override
  public AstNode visitBinaryMessage(BinaryMessageContext ctx) {
    throw new IllegalStateException("this node should never be visited directly");
  }

  @Override
  public AstNode visitKeywordSend(KeywordSendContext ctx) {
    AstNode receiver = ctx.keywordObject().accept(this);
    return makeMessageSend(receiver, ctx.keywordMessage());
  }

  @Override
  public AstNode visitKeywordMessage(KeywordMessageContext ctx) {
    // This is called when a keyword message is encountered in a receiverless form.
    // Otherwise the containing KeywordSend is taking care of it. 
    Optional<String> selector = ctx.KEYWORD().stream()
        .map(each -> each.getText())
        .reduce(String::concat);
    List<AstNode> args = ctx.keywordObject().stream()
        .map(each -> each.accept(this))
        .collect(Collectors.toList());
    return new MessageSendNoReceiver(selector.get(), false, args);
  }

  @Override
  public AstNode visitSpecialReceiver(SpecialReceiverContext ctx) {
    return visitFirstChild(ctx);
  }

  @Override
  public AstNode visitBlock(BlockContext ctx) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public AstNode visitBlockArgs(BlockArgsContext ctx) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public AstNode visitBlockTemps(BlockTempsContext ctx) {
    throw new UnsupportedOperationException("not implemented yet");
  }
  
  private MessageSendWithReceiver makeMessageSend(AstNode receiver, UnaryMessageContext ctx) {
    MessageSendWithReceiver send = new MessageSendWithReceiver(
        receiver,
        ctx.IDENTIFIER().getText(),
        Collections.emptyList());
    UnaryMessageContext chainedMessage = ctx.unaryMessage();
    return chainedMessage == null
        ? send
        : makeMessageSend(send, chainedMessage);
  }
  
  private MessageSendWithReceiver makeMessageSend(AstNode receiver, BinaryMessageContext ctx) {
    AstNode arg = ctx.binaryObject().accept(this);
    MessageSendWithReceiver send = new MessageSendWithReceiver(
        receiver,
        ctx.BINARY_SELECTOR().getText(),
        Arrays.asList(arg));
    BinaryMessageContext chainedMessage = ctx.binaryMessage();
    return chainedMessage == null
        ? send
        : makeMessageSend(send, chainedMessage);
  }
  
  private MessageSendWithReceiver makeMessageSend(AstNode receiver, KeywordMessageContext ctx) {
    Optional<String> selector = ctx.KEYWORD().stream()
        .map(each -> each.getText())
        .reduce(String::concat);
    List<AstNode> args = ctx.keywordObject().stream()
        .map(each -> each.accept(this))
        .collect(Collectors.toList());
    return new MessageSendWithReceiver(
        receiver,
        selector.get(),
        args);
  }
  
  private AstNode visitFirstChild(ParserRuleContext ctx) {
    for (ParseTree node : ctx.children) {
      return node.accept(this);
    }
    throw new IllegalStateException("a required child is missing");
  }
}
