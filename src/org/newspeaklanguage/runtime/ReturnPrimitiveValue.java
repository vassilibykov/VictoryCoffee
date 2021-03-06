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

/**
 * Used by generated code to return primitive ints from methods
 * which are supposed to return an Object.
 *
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 */
public final class ReturnPrimitiveValue {

  public static final String INTERNAL_CLASS_NAME = Descriptor.internalClassName(ReturnPrimitiveValue.class);
  public static final String FACTORY_DESCRIPTOR = Descriptor.ofMethod(ReturnPrimitiveValue.class, int.class);

  @SuppressWarnings("unused") // Called by generated code
  public static ReturnPrimitiveValue create(int value) {
    return new ReturnPrimitiveValue(value);
  }

  public final int value;

  private ReturnPrimitiveValue(int value) {
    this.value = value;
  }

}
