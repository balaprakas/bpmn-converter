package com.example.bpmnconverter.controller;

import com.example.bpmnconverter.service.BpmnGeneratorService;
import com.example.bpmnconverter.service.BpmnParserService;
import com.example.bpmnconverter.service.LlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BpmnControllerTest {

    @Autowired
    private BpmnController bpmnController;

    @Test
    void testGenerateBpmn() {
        String freeText = "Customer applies for loan, if valid run risk assessment, then finalize loan";

        ResponseEntity<String> response = bpmnController.generateBpmn(freeText);

        assertEquals(200, response.getStatusCodeValue());
        String bpmnXml = response.getBody();
        assertNotNull(bpmnXml);
        assertTrue(bpmnXml.contains("bpmn:definitions"));
        assertTrue(bpmnXml.contains("callActivity"));
        assertTrue(bpmnXml.contains("risk_assessment"));
    }

    @Test
    void testSummarizeBpmn() throws Exception {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
              <bpmn:process id="loan_process" name="Loan Process" isExecutable="true">
                <bpmn:startEvent id="start1" name="Customer applies"/>
                <bpmn:userTask id="task1" name="Validate application"/>
                <bpmn:callActivity id="call1" name="Risk Assessment" calledElement="risk_assessment"/>
                <bpmn:userTask id="task2" name="Finalize loan"/>
                <bpmn:endEvent id="end1" name="Loan approved or rejected"/>
              </bpmn:process>
            </bpmn:definitions>
            """;

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "loan.bpmn",
                "application/xml",
                bpmnXml.getBytes()
        );

        ResponseEntity<String> response = bpmnController.summarizeBpmn(file);

        assertEquals(200, response.getStatusCodeValue());
        String summary = response.getBody();
        assertNotNull(summary);
        assertTrue(summary.contains("Loan Process starts"));
        assertTrue(summary.contains("Risk Assessment"));
    }

    @Test
    void testValidateIR() {
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

        ResponseEntity<String> response = bpmnController.validateIR(validJson);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Valid IR JSON", response.getBody());
    }
}
