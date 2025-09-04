package com.example.bpmnconverter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonSchemaValidatorTest {

    private JsonSchemaValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new JsonSchemaValidator();
    }

    @Test
    void testValidIRJson() {
        String validJson = """
            {
              "processId": "test_process",
              "processName": "Test Process",
              "elements": [
                {"id": "start1", "type": "start", "name": "Start"},
                {"id": "end1", "type": "end", "name": "End"}
              ],
              "flows": [
                {"id": "flow1", "sourceRef": "start1", "targetRef": "end1"}
              ]
            }
            """;

        assertTrue(validator.isValid(validJson));
    }

    @Test
    void testInvalidIRJson() {
        String invalidJson = """
            {
              "processId": "test_process",
              "elements": [
                {"id": "start1", "type": "start", "name": "Start"}
              ]
            }
            """;

        assertFalse(validator.isValid(invalidJson));
    }

    @Test
    void testInvalidElementType() {
        String invalidJson = """
            {
              "processId": "test_process",
              "processName": "Test Process",
              "elements": [
                {"id": "start1", "type": "invalidType", "name": "Start"}
              ],
              "flows": []
            }
            """;

        assertFalse(validator.isValid(invalidJson));
    }
}
