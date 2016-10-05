package org.newspeaklanguage.runtime;

/**
 * A superclass of all objects manipulated by compiled Newspeak code.
 * 
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 *
 */
public abstract class Object {
  
  public static final String INTERNAL_CLASS_NAME = Object.class.getName().replace('.', '/');
  public static final String TYPE_DESCRIPTOR = "L" + INTERNAL_CLASS_NAME + ";";
  public static final String CONSTRUCTOR_DESCRIPTOR = "(" + ObjectFactory.TYPE_DESCRIPTOR + ")V";
  
  public abstract ObjectFactory nsClass();
  
  public Object $class() {
    return nsClass();
  }
}
