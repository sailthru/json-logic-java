package io.github.jamsesso.jsonlogic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AnyExpressionTests {
  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  public void testEmptyArray() throws JsonLogicException {
    assertEquals(false, jsonLogic.apply("{\"any\": [[], {\">\": [{\"var\": \"\"}, 0]}]}", null));
  }

  @Test
  public void testAny() throws JsonLogicException {
    assertEquals(true, jsonLogic.apply("{\"any\": [[1, 2, 3], {\">\": [{\"var\": \"\"}, 0]}]}", null));
    assertEquals(true, jsonLogic.apply("{\"any\": [[1, 2, 3], {\">\": [{\"var\": \"\"}, 1]}]}", null));
    assertEquals(false, jsonLogic.apply("{\"any\": [[1, 2, 3], {\"<\": [{\"var\": \"\"}, 1]}]}", null));
  }
}
