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

package org.newspeaklanguage.runtime;

import org.newspeaklanguage.compiler.Descriptor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * A Newspeak closure: the result of evaluating a block expression.
 */
public class Closure extends StandardObject {

  public static final String INTERNAL_CLASS_NAME = Descriptor.internalClassName(Closure.class);
  public static final int MAX_POSITIONAL_COPIED_VALUES = 4;

  /**
   * Return a descriptor string for a Closure constructor that will accept the specified
   * number of copied values (in addition to the copied self which is always present).
   */
  public static String constructorDescriptor(int copiedValueCount) {
    StringBuilder builder = new StringBuilder(100);
    builder
        .append("(")
        .append(Descriptor.ofType(MethodHandle.class))
        .append(Descriptor.ofType(StandardObject.class));
    if (copiedValueCount <= MAX_POSITIONAL_COPIED_VALUES) {
      for (int i = 0; i < copiedValueCount; i++) {
        builder
            .append(Descriptor.OBJECT_TYPE_DESCRIPTOR)
            .append(Descriptor.INT_TYPE_DESCRIPTOR);
      }
    } else {
      builder.append("[").append(Descriptor.OBJECT_TYPE_DESCRIPTOR);
    }
    builder.append(")V");
    return builder.toString();
  }

  /**
   * The handle to the implementation method of the block, retrieved from the BlockHandle
   * and bound to the copied receiver and copied values, if any.
   */
  protected final MethodHandle implementation;
  /**
   * The number of arguments of the implementation method after it'e been bound to all
   * the copied values. In other words, it's the arity of the original block.
   */
  protected final int arity;

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf) {
    this(implMethodHandle.bindTo(copiedSelf));
  }

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf,
                 Object copied1, int int1) {
    this(MethodHandles.insertArguments(implMethodHandle, 0, copiedSelf, copied1, int1));
  }

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf,
                 Object copied1, int int1, Object copied2, int int2) {
    this(MethodHandles.insertArguments(implMethodHandle, 0, copiedSelf, copied1, int1, copied2, int2));
  }

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf,
                 Object copied1, int int1, Object copied2, int int2, Object copied3, int int3) {
    this(MethodHandles.insertArguments(implMethodHandle, 0, copiedSelf, copied1, int1, copied2, int2, copied3, int3));
  }

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf,
                 Object copied1, int int1, Object copied2, int int2, Object copied3, int int3, Object copied4, int int4) {
    this(MethodHandles.insertArguments(implMethodHandle, 0, copiedSelf, copied1, int1, copied2, int2, copied3, int3, copied4, int4));
  }

  public Closure(MethodHandle implMethodHandle, StandardObject copiedSelf, Object... copiedValues) {
    super(null); // TODO set up a class object and pass it in
    MethodHandle impl = implMethodHandle.bindTo(copiedSelf);
    this.implementation = MethodHandles.insertArguments(impl, 0, copiedValues);
    this.arity = this.implementation.type().parameterCount();
  }

  private Closure(MethodHandle alreadyBoundHandle) {
    super(null);// TODO set up a class object and pass it in
    this.implementation = alreadyBoundHandle;
    this.arity = alreadyBoundHandle.type().parameterCount() / 2;
  }

  public Object $value(int unused) {
    if (arity != 0) {
      throw new RuntimeError("this closure expects " + arity + " arguments, but was called with 0");
    }
    try {
      return implementation.invokeExact();
    } catch (Throwable e) {
      throw new RuntimeError("closure invocation error", e);
    }
  }

  public Object $value$(int unused, Object arg1, int int1) {
    if (arity != 1) {
      throw new RuntimeError("this closure expects " + arity + " arguments, but was called with 1");
    }
    try {
      return implementation.invokeExact(arg1, int1);
    } catch (Throwable e) {
      throw new RuntimeError("closure invocation error", e);
    }
  }

  public Object $value$value$(int unused, Object arg1, int int1, Object arg2, int int2) {
    if (arity != 2) {
      throw new RuntimeError("this closure expects " + arity + " arguments, but was called with 2");
    }
    try {
      return implementation.invokeExact(arg1, int1, arg2, int2);
    } catch (Throwable e) {
      throw new RuntimeError("closure invocation error", e);
    }
  }

  public Object $value$value$value$(int unused, Object arg1, int int1, Object arg2, int int2, Object arg3, int int3) {
    if (arity != 3) {
      throw new RuntimeError("this closure expects " + arity + " arguments, but was called with 3");
    }
    try {
      return implementation.invokeExact(arg1, int1, arg2, int2, arg3, int3);
    } catch (Throwable e) {
      throw new RuntimeError("closure invocation error", e);
    }
  }

  public Object $value$value$value$value$(int unused, Object arg1, int int1, Object arg2, int int2, Object arg3, int int3, Object arg4, int int4) {
    if (arity != 4) {
      throw new RuntimeError("this closure expects " + arity + " arguments, but was called with 4");
    }
    try {
      return implementation.invokeExact(arg1, int1, arg2, int2, arg3, int3, arg4, int4);
    } catch (Throwable e) {
      throw new RuntimeError("closure invocation error", e);
    }
  }

}
