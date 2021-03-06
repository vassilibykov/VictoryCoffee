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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.newspeaklanguage.compiler.NamingPolicy;
import org.newspeaklanguage.compiler.ast.CodeUnit;

/**
 * A scope established by a method or a block. More exactly, this is an analyzer
 * record associated with a method or a block which keeps track of the block's
 * name bindings, as well as other things.
 *
 * The names defined by this scope
 * are accessor selectors to the method's arguments and temps. A variable always
 * adds a getter name to the scope. A setter name is only added is the variable
 * is not read-only.
 *
 * @author Vassili Bykov <newspeakbigot@gmail.com>
 */
public abstract class CodeScope extends Scope<CodeScopeEntry> {

  /**
   * The local variables of the implementation method frame which correspond to
   * the actual Newspeak arguments and temps of the block or method.
   */
  protected final List<LocalVariable> ownVariables = new ArrayList<>();

  CodeScope(CodeUnit definition, Scope<? extends ScopeEntry> parent) {
    super(definition, parent);
    definition.arguments().forEach(
        each -> ownVariables.add(new LocalVariable(each, false, false)));
    definition.temps().forEach(
        each -> ownVariables.add(new LocalVariable(each, true, false)));
  }

  public BlockScope asBlockScope() {
    return (BlockScope) this;
  }

  @Override
  public ClassScope outerClassNamed(String name) {
    return parent == null ? null : parent.outerClassNamed(name);
  }

  public Optional<LocalVariable> ownVariableNamed(String name) {
    return find(name, ownVariables);
  }

  public Optional<LocalVariable> localVariableNamed(String name) {
    return find(name, ownVariables);
  }

  public void assignLocalVariableIndices() {
    int index = firstOwnVariableIndex();
    for (LocalVariable var : ownVariables) {
      var.setIndex(index);
      index += 2; // each var gets 2 slots; one Object and one int
    }
  }

  @Override
  public CodeScopeEntry define(String name) {
    if (NamingPolicy.isSetterSelector(name)) {
      CodeScopeEntry newEntry = new CodeScopeEntry(name, this);
      names.put(name, newEntry);
      return newEntry;
    } else {
      return super.define(name);
    }
  }

  public void forEachOwnVariable(Consumer<LocalVariable> action) {
    ownVariables.forEach(action);
  }

  public void forEachTemp(Consumer<LocalVariable> action) {
    ownVariables.stream().filter(LocalVariable::isTemp).forEach(action);
  }

  /**
   * Mark a variable defined in this scope (an argument or a temp) as boxed. The
   * variable must exist.
   */
  public void markVariableAsBoxed(String name) {
    ownVariableNamed(name).get().setIsBoxed(true);
  }

  protected abstract int firstOwnVariableIndex();

  @Override
  protected CodeScopeEntry createScopeEntry(String name) {
    return new CodeScopeEntry(name, this);
  }

  // visible for testing
  List<String> ownVariableNames() {
    return ownVariables.stream()
        .map(each -> each.name())
        .collect(Collectors.toList());
  }

  protected Optional<LocalVariable> find(String name, List<LocalVariable> vars) {
    return vars.stream()
        .filter(some -> some.name().equals(name))
        .findAny();
  }

}
