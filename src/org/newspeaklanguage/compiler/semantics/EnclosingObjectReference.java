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

package org.newspeaklanguage.compiler.semantics;

import org.newspeaklanguage.compiler.ast.AstNode;
import org.newspeaklanguage.compiler.ast.AstNodeVisitor;

/**
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 */
public class EnclosingObjectReference extends AstNode {

  private final int scopeLevel;

  EnclosingObjectReference(int scopeLevel) {
    this.scopeLevel = scopeLevel;
  }

  public int scopeLevel() {
    return scopeLevel;
  }

  @Override
  public void accept(AstNodeVisitor visitor) {
    throw new IllegalArgumentException("This node should not be visited by a regular visitor");
  }

  @Override
  public void accept(RewrittenNodeVisitor visitor) {
    visitor.visitEnclosingObjectReference(this);
  }
}
