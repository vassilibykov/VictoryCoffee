package org.newspeaklanguage.compiler;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.newspeaklanguage.testsupport.Example;

public class MessageSendTests {

  @Test
  public void testUnaryMessageSend() {
    Example test = Example.testMethod(
        "test = (^self foo)\n"
        + "foo = (^'the foo')");
    assertTrue(test.isResult("the foo"));
  }

  @Test
  public void testUnaryMessageSendChain() {
    Example test = Example.testMethod(
        "test = (^self foo printString)\n"
        + "foo = (^'the foo')");
    assertTrue(test.isResult("'the foo'"));
  }

  @Test
  public void testBinaryMessageSend() {
    Example test = Example.testMethod(
        "test = (^self foo + self bar)\n"
        + "foo = (^'foo1')\n"
        + "bar = (^'bar2')");
    assertTrue(test.isResult("foo1bar2"));
  }

  @Test
  public void testBinaryAndUnaryMix() {
    Example test = Example.testMethod(
        "test = (^self foo printString + self bar printString)\n"
        + "foo = (^'foo1')\n"
        + "bar = (^'bar2')");
    assertTrue(test.isResult("'foo1''bar2'"));
  }

  @Test
  public void testKeywordMessageSend() {
    Example test = Example.testMethod(
        "test = (^self concat: 'foo' and: 'bar')\n"
        + "concat: a and: b = (^ a + b)");
    assertTrue(test.isResult("foobar"));
  }

  @Test
  public void testKeywordAndUnaryMix() {
    Example test = Example.testMethod(
        "test = (^self concat: 'foo' printString and: 'bar' printString)\n"
        + "concat: a and: b = (^ a + b)");
    assertTrue(test.isResult("'foo''bar'"));
  }

  @Test
  public void testAllMix() {
    Example test = Example.testMethod(
        "test = (^self concat: ('foo' + 'boo') printString and: ('bar') + 'baz' printString)\n"
        + "concat: a and: b = (^ a + b)");
    assertTrue(test.isResult("'fooboo'bar'baz'"));
  }

  @Test
  public void testImplicitSelfSendUnary() {
    Example test = Example.testMethod(
        "test = (^foobar)"
        + "foobar = (^'Hello')");
    assertTrue(test.isResult("Hello"));
  }

  @Test
  public void testImplicitSelfSendKeyword() {
    Example test = Example.testMethod(
            "test = (^concat: 'foo' and: 'bar')"
            + "concat: a and: b = (^ a + b)");
    assertTrue(test.isResult("foobar"));
  }

}
