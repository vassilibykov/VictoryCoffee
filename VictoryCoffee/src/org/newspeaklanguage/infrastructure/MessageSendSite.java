package org.newspeaklanguage.infrastructure;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;


public final class MessageSendSite extends MutableCallSite {

  private static final String BOOTSTRAP_METHOD_DESCRIPTOR = MethodType.methodType(
      CallSite.class, Lookup.class, String.class, MethodType.class)
        .toMethodDescriptorString();
  
  public static Handle bootstrappedUsing(Class<?> hostClass, String methodName) {
    return new Handle(
        Opcodes.H_INVOKESTATIC,
        hostClass.getName().replace('.', '/'),
        methodName,
        BOOTSTRAP_METHOD_DESCRIPTOR,
        false);
  }
  
  public static MessageSendSite create(
      Lookup lookupAtCallSite,
      Lookup lookupAtImplementor,
      String selector,
      MethodType callSiteType,
      String fallbackMethodName,
      MethodType fallbackMethodType)
        throws NoSuchMethodException, IllegalAccessException
  {
   MessageSendSite thisSite = new MessageSendSite(selector, lookupAtCallSite, callSiteType);
   MethodHandle fallback = lookupAtImplementor
       .findStatic(lookupAtImplementor.lookupClass(), fallbackMethodName, fallbackMethodType)
       .bindTo(thisSite)
       .asType(callSiteType);
   thisSite.setTarget(fallback);
   return thisSite;
  }
  
  private static final MethodType CHECK_CLASS_TYPE =
      MethodType.methodType(boolean.class, Class.class, Object.class);
  
  public static boolean checkClass(Class<?> expectedClass, Object object) {
    return object.getClass() == expectedClass;
  }
  
  /*
   * Instance side
   */
  
  private final String selector;
  private final Lookup lookup;
  
  private MessageSendSite(String name, Lookup lookup, MethodType type) {
    super(type);
    this.selector = name;
    this.lookup = lookup;
  }
  
  public String selector() { return selector; }
  public Lookup lookup() { return lookup; }
  
  public void addInlineCache(Class<?> expectedClass, MethodHandle specialization)
      throws NoSuchMethodException, IllegalAccessException
  {
    Lookup here = MethodHandles.lookup();
    MethodHandle typeCheck = here
        .findStatic(here.lookupClass(), "checkClass", CHECK_CLASS_TYPE)
        .bindTo(expectedClass);
    typeCheck = typeCheck.asType(
        typeCheck.type().changeParameterType(0, this.type().parameterType(0)));
    MethodHandle cacheNode =
        MethodHandles.guardWithTest(typeCheck, specialization, getTarget());
    setTarget(cacheNode);
  }
}
