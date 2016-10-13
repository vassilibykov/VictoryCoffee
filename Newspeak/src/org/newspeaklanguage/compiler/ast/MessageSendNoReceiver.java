package org.newspeaklanguage.compiler.ast;

import java.util.List;

import org.newspeaklanguage.compiler.semantics.NameMeaning;

/**
 * A message send with implicit receiver. This includes what would count as slot references.
 * 
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 *
 */
public class MessageSendNoReceiver extends AstNode {

  protected final String selector;
  protected final List<AstNode> arguments;
  protected final boolean isSetterSend;
  
  private NameMeaning meaning;

  public MessageSendNoReceiver(String selector, List<AstNode> arguments, boolean isSetterSend) {
    this.selector = selector;
    this.arguments = arguments;
    this.isSetterSend = isSetterSend;
  }

  public String selector() { return selector; }
  public List<AstNode> arguments() { return arguments; }
  public boolean isSetterSend() { return isSetterSend; }

  public int arity() {
    return arguments.size();
  }
  
  /**
   * A message send with no receiver and no arguments is potentially a name:
   * something that in the regular Smalltalk understanding would be a variable
   * reference. However, in Newspeak it still can be just a self send with no
   * receiver.
   */
  public boolean isName() {
    return arity() == 0;
  }
  
  public NameMeaning meaning() {
    return meaning;
  }

  public void setMeaning(NameMeaning meaning) {
    assert this.meaning == null;
    this.meaning = meaning;
  }
  
  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visitMessageSendNoReceiver(this);
  }
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "." + meaning
        + "(" + selector + ")";
  }
}