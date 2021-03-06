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

import java.util.List;

import org.newspeaklanguage.compiler.semantics.ClassScope;

public class ClassDecl extends AstNode implements NameDefinition {

  private final String name;
  private final MessagePattern initMessage;
  private final String superclassName;
  private final MessagePattern superInitMessage;
  private final List<SlotDefinition> slots;
  private final List<ClassDecl> nestedClasses;
  private final List<Category> categories;
  private final List<Category> classCategories;
  
  /*
   * Information assigned by the analyzer.
   */
  /**
   * The scope defined by this class. The scope where the class itself is defined is its parent.
   */
  private ClassScope scope;
  /**
   * The enclosing class, if the class is nested.
   */
  private ClassDecl enclosingClass;
  /**
   * The name of the Java class this class should be compiled to.
   */
  private String implementationClassName;
  
  ClassDecl(String name, MessagePattern initMessage, String superclassName, MessagePattern superInitMessage,
      List<SlotDefinition> slots, List<ClassDecl> nestedClasses, List<Category> categories, List<Category> classCategories)
  {
    this.name = name;
    this.initMessage = initMessage;
    this.superclassName = superclassName;
    this.superInitMessage = superInitMessage;
    this.slots = slots;
    this.nestedClasses = nestedClasses;
    this.categories = categories;
    this.classCategories = classCategories;
  }
  
  public String name() { return name; }
  public MessagePattern initMessage() { return initMessage; }
  public String superclassName() { return superclassName; }
  public MessagePattern superInitMessage() { return superInitMessage; }
  public List<SlotDefinition> slots() { return slots; }
  public List<ClassDecl> nestedClasses() { return nestedClasses; }
  public List<Category> categories() { return categories; }
  public List<Category> classCategories() { return classCategories; }
 
  /*
   * Getting and setting analysis results
   */
  public ClassScope scope() {
    return scope;
  }

  public void setScope(ClassScope scope) {
    assert this.scope == null;
    this.scope = scope;
  }

  public ClassDecl enclosingClass() {
    return enclosingClass;
  }

  public void setEnclosingClass(ClassDecl enclosingClass) {
    assert this.enclosingClass == null;
    this.enclosingClass = enclosingClass;
  }

  public String implementationClassName() {
    return implementationClassName;
  }

  public void setImplementationClassName(String name) {
    assert this.implementationClassName == null;
    this.implementationClassName = name;
  }
  
  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visitClassDecl(this);
  }
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName() 
        + ":" + name() 
        + "(" + implementationClassName() + ")";
  }

  @Override
  public boolean isMutable() {
    return false;
  }
}
