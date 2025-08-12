package io.github.jamsesso.jsonlogic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VariableTests {
    private static final JsonLogic jsonLogic = new JsonLogic();

    @Test
    public void testEmptyString() throws JsonLogicException {
        assertEquals(3.14, jsonLogic.apply("{\"var\": \"\"}", 3.14));
    }

    @Test
    public void testMapAccess() throws JsonLogicException {
        Map<String, Double> data = new HashMap<String, Double>() {{
            put("pi", 3.14);
        }};

        assertEquals(3.14, jsonLogic.apply("{\"var\": \"pi\"}", data));
    }

    @Test
    public void testDefaultValue() throws JsonLogicException {
        assertEquals(3.14, jsonLogic.apply("{\"var\": [\"pi\", 3.14]}", null));
    }

    @Test
    public void testUndefined() throws JsonLogicException {
        assertNull(jsonLogic.apply("{\"var\": [\"pi\"]}", null));
        assertNull(jsonLogic.apply("{\"var\": \"\"}", null));
        assertNull(jsonLogic.apply("{\"var\": 0}", null));
    }

    @Test
    public void testArrayAccess() throws JsonLogicException {
        String[] data = new String[]{"hello", "world"};

        assertEquals("hello", jsonLogic.apply("{\"var\": 0}", data));
        assertEquals("world", jsonLogic.apply("{\"var\": 1}", data));
        assertNull(jsonLogic.apply("{\"var\": 2}", data));
        assertNull(jsonLogic.apply("{\"var\": 3}", data));
    }

    @Test
    public void testArrayAccessWithStringKeys() throws JsonLogicException {
        String[] data = new String[]{"hello", "world"};

        assertEquals("hello", jsonLogic.apply("{\"var\": \"0\"}", data));
        assertEquals("world", jsonLogic.apply("{\"var\": \"1\"}", data));
        assertNull(jsonLogic.apply("{\"var\": \"2\"}", data));
        assertNull(jsonLogic.apply("{\"var\": \"3\"}", data));
    }

    @Test
    public void testListAccess() throws JsonLogicException {
        List<String> data = Arrays.asList("hello", "world");

        assertEquals("hello", jsonLogic.apply("{\"var\": 0}", data));
        assertEquals("world", jsonLogic.apply("{\"var\": 1}", data));
        assertNull(jsonLogic.apply("{\"var\": 2}", data));
        assertNull(jsonLogic.apply("{\"var\": 3}", data));
    }

    @Test
    public void testListAccessWithStringKeys() throws JsonLogicException {
        List<String> data = Arrays.asList("hello", "world");

        assertEquals("hello", jsonLogic.apply("{\"var\": \"0\"}", data));
        assertEquals("world", jsonLogic.apply("{\"var\": \"1\"}", data));
        assertNull(jsonLogic.apply("{\"var\": \"2\"}", data));
        assertNull(jsonLogic.apply("{\"var\": \"3\"}", data));
    }

    @Test
    public void testComplexAccess() throws JsonLogicException {
        Map<String, Object> data = new HashMap<String, Object>() {{
            put("users", Arrays.asList(
                    new HashMap<String, Object>() {{
                        put("name", "John");
                        put("followers", 1337);
                    }},
                    new HashMap<String, Object>() {{
                        put("name", "Jane");
                        put("followers", 2048);
                    }}
            ));
        }};

        assertEquals("John", jsonLogic.apply("{\"var\": \"users.0.name\"}", data));
        assertEquals(1337.0, jsonLogic.apply("{\"var\": \"users.0.followers\"}", data));
        assertEquals("Jane", jsonLogic.apply("{\"var\": \"users.1.name\"}", data));
        assertEquals(2048.0, jsonLogic.apply("{\"var\": \"users.1.followers\"}", data));
    }

    @Test
    public void testJsonLogicNullVsMissing() throws Exception {
        // Rule: "and" of:
        // 1. "!" (not) of var "myChanges.expire_date.from", fallback true if missing
        // 2. "!!" (double not) of var "myChanges.expire_date.to"
        String rule = "{ \"and\": [ " +
                "{ \"!\": { \"var\": [\"myChanges.expire_date.from\", true] } }, " +
                "{ \"!!\": { \"var\": [\"myChanges.expire_date.to\"] } }" +
                " ] }";

        // Case 1: "from" present and null, "to" present and non-null
        Map<String, Object> expireDate = new HashMap<>();
        expireDate.put("from", null);
        expireDate.put("to", "a");

        Map<String, Object> myChanges = new HashMap<>();
        myChanges.put("expire_date", expireDate);

        Map<String, Object> data = new HashMap<>();
        data.put("myChanges", myChanges);

        // Expected: Should be TRUE (null should be treated as empty)
        Object result1 = jsonLogic.apply(rule, data);
        assertEquals("JsonLogic should treat null as present and empty", true, result1);

        // Case 2: "from" missing, "to" present
        expireDate.remove("from");
        // myChanges map is same object
        Object result2 = jsonLogic.apply(rule, data);
        // Expected: Should be FALSE (missing should hit the fallback "true" and thus negate the '!' logic)
        assertEquals("JsonLogic should treat missing as missing, not as null", false, result2);
    }

    @Test
    public void testJsonLogicListNullVsMissing() throws Exception {
        // Rule: "and" of:
        // 1. "!" (not) of var "list.0", fallback true if missing
        // 2. "!!" (double not) of var "list.1"
        String rule = "{ \"and\": [ " +
                "{ \"!\": { \"var\": [\"list.0\", true] } }, " +
                "{ \"!!\": { \"var\": [\"list.1\"] } }" +
                " ] }";

        // Case 1: index 0 present and null, index 1 present and non-null
        List<Object> list = new ArrayList<>(Arrays.asList(null, "a"));
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);

        // Expected: TRUE (index 0 is present and null)
        Object result1 = jsonLogic.apply(rule, data);
        assertEquals("JsonLogic should treat null element as present and empty", true, result1);

        // Case 2: index 0 is missing (out of bounds), index 1 present
        // To make index 0 missing, use an empty list or a list with one element and reference index 1
        List<Object> listMissing = new ArrayList<>(Collections.singletonList("a")); // index 0: "a"
        Map<String, Object> dataMissing = new HashMap<>();
        dataMissing.put("list", listMissing);

        // Now, "list.1" is missing (out of bounds), so fallback triggers
        String rule2 = "{ \"!\": { \"var\": [\"list.1\", true] } }";
        Object result2 = jsonLogic.apply(rule2, dataMissing);
        assertEquals("JsonLogic should treat missing index as missing, not as null", false, result2);

        // Optional: check index 0 missing
        List<Object> emptyList = new ArrayList<>();
        Map<String, Object> dataEmpty = new HashMap<>();
        dataEmpty.put("list", emptyList);

        Object result3 = jsonLogic.apply(rule2, dataEmpty);
        assertEquals("JsonLogic should treat missing index as missing, not as null (empty list)", false, result3);
    }

}
