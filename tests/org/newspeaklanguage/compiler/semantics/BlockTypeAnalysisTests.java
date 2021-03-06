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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.newspeaklanguage.compiler.Compiler;
import org.newspeaklanguage.compiler.ast.AstNode;
import org.newspeaklanguage.compiler.ast.MessageSendNoReceiver;

public class BlockTypeAnalysisTests {

  @Test
  public void testLocalReferenceRecognition() {
    AstNode tree = Compiler.parseAndAnalyze("class Test = ()"
    + "('testing'"
    + "test: foo = ("
    + "  ^[:bar :baz | | temp |"
    + "   self x: foo." // not local because  foo is in the outer method
    + "   [:quux | bar] value." // not local because bar is in the outer block
    + "   baz + temp] " // both are local because both are in the same scope
    + "     value: 3 value: 4. )"
    + ")");
    MessageSendNoReceiver fooRef = NodeFinder.findLocalVarReference("foo", tree);
    MessageSendNoReceiver barRef = NodeFinder.findLocalVarReference("bar", tree);
    MessageSendNoReceiver bazRef = NodeFinder.findLocalVarReference("baz", tree);
    MessageSendNoReceiver tempRef = NodeFinder.findLocalVarReference("temp", tree);
    assertFalse(varOf(fooRef).isLocal());
    assertFalse(varOf(barRef).isLocal());
    assertTrue(varOf(bazRef).isLocal());
    assertTrue(varOf(tempRef).isLocal());
  }

  private VariableReference varOf(MessageSendNoReceiver node) {
    return (VariableReference) node.rewritten();
  }

  @Test
  public void testCopiableReferenceRecognition() {
    AstNode tree = Compiler.parseAndAnalyze("class Test = ()"
    + "('testing'"
    + "test: foo and: boo = ( | temp1 |"
    + "  ^[:bar :baz | | temp2 |"
    + "   self x: foo." // copiable because foo is one level up and immutable
    + "   self x: temp1." // not copiable because temp1 is mutable
    + "   [:quux | bar] value." // copiable because bar is one level up and immutable  
    + "   [:frob | boo] value." // still copiable though boo is two levels up 
    + "   ^baz"  // baz is clean, not copiable
    + "]  value: 3 value: 4. )"
    + ")");
    MessageSendNoReceiver fooRef = NodeFinder.findLocalVarReference("foo", tree);
    MessageSendNoReceiver temp1Ref = NodeFinder.findLocalVarReference("temp1", tree);
    MessageSendNoReceiver barRef = NodeFinder.findLocalVarReference("bar", tree);
    MessageSendNoReceiver booRef = NodeFinder.findLocalVarReference("boo", tree);
    assertTrue(varOf(fooRef).isCopiable());
    assertFalse(varOf(temp1Ref).isCopiable());
    assertTrue(varOf(barRef).isCopiable());
    assertTrue(varOf(booRef).isCopiable());
  }

}
