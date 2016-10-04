package org.newspeaklanguage.compiler.ast;

import java.util.List;

public class MessagePattern extends AstNode {
  
  private final String selector;
  private final List<Argument> arguments;
  
  MessagePattern(String selector, List<Argument> arguments) {
    this.selector = selector;
    this.arguments = arguments;
  }

  public String selector() { return selector; }
  public List<Argument> arguments() { return arguments; }
  
  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visitMessagePattern(this);
  }
}
