package com.example.bpmnconverter.service;

import com.example.bpmnconverter.model.ProcessIR;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LlmService {

    @Autowired
    private JsonSchemaValidator validator;

    @Autowired
    private ObjectMapper objectMapper;

    public ProcessIR convertTextToIR(String freeText) {
        String prompt = buildPrompt(freeText);
        String llmResponse = callLlm(prompt);
        
        if (!validator.isValid(llmResponse)) {
            llmResponse = attemptSelfRepair(llmResponse, freeText);
        }
        
        try {
            return objectMapper.readValue(llmResponse, ProcessIR.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response to ProcessIR", e);
        }
    }

    private String buildPrompt(String freeText) {
        return String.format("""
            Convert the following free text description into a valid JSON representation following the IR schema.
            
            Schema requirements:
            - processId: unique identifier
            - processName: human-readable name
            - elements: array of process elements (start, end, task, userTask, serviceTask, scriptTask, exclusiveGateway, parallelGateway, subProcess, callActivity)
            - flows: array of sequence flows connecting elements
            
            For service tasks:
            - If API is known, include config with method, url, headers, body
            - If API is unknown, set requires_mapping=true
            
            For call activities:
            - If target process is known, set calledElement
            - If unknown, set requires_mapping=true
            
            Return ONLY valid JSON, no prose or explanation.
            
            Example:
            {
              "processId": "loan_process",
              "processName": "Loan Application Process",
              "elements": [
                {"id": "start1", "type": "start", "name": "Start"},
                {"id": "task1", "type": "userTask", "name": "Apply for loan"},
                {"id": "gateway1", "type": "exclusiveGateway", "name": "Valid application?"},
                {"id": "call1", "type": "callActivity", "name": "Risk Assessment", "config": {"calledElement": "risk_assessment"}},
                {"id": "task2", "type": "userTask", "name": "Finalize loan"},
                {"id": "end1", "type": "end", "name": "End"}
              ],
              "flows": [
                {"id": "flow1", "sourceRef": "start1", "targetRef": "task1"},
                {"id": "flow2", "sourceRef": "task1", "targetRef": "gateway1"},
                {"id": "flow3", "sourceRef": "gateway1", "targetRef": "call1", "condition": "valid"},
                {"id": "flow4", "sourceRef": "call1", "targetRef": "task2"},
                {"id": "flow5", "sourceRef": "task2", "targetRef": "end1"}
              ]
            }
            
            Free text: %s
            """, freeText);
    }

    private String callLlm(String prompt) {
        return generateMockResponse(prompt);
    }

    private String generateMockResponse(String prompt) {
        if (prompt.toLowerCase().contains("loan")) {
            return """
                {
                  "processId": "loan_process",
                  "processName": "Loan Application Process",
                  "elements": [
                    {"id": "start1", "type": "start", "name": "Start"},
                    {"id": "task1", "type": "userTask", "name": "Customer applies for loan"},
                    {"id": "gateway1", "type": "exclusiveGateway", "name": "Valid application?"},
                    {"id": "call1", "type": "callActivity", "name": "Risk Assessment", "config": {"calledElement": "risk_assessment"}},
                    {"id": "task2", "type": "userTask", "name": "Finalize loan"},
                    {"id": "end1", "type": "end", "name": "End"}
                  ],
                  "flows": [
                    {"id": "flow1", "sourceRef": "start1", "targetRef": "task1"},
                    {"id": "flow2", "sourceRef": "task1", "targetRef": "gateway1"},
                    {"id": "flow3", "sourceRef": "gateway1", "targetRef": "call1", "condition": "valid"},
                    {"id": "flow4", "sourceRef": "call1", "targetRef": "task2"},
                    {"id": "flow5", "sourceRef": "task2", "targetRef": "end1"}
                  ]
                }
                """;
        }
        
        return """
            {
              "processId": "generic_process",
              "processName": "Generic Process",
              "elements": [
                {"id": "start1", "type": "start", "name": "Start"},
                {"id": "task1", "type": "task", "name": "Process task"},
                {"id": "end1", "type": "end", "name": "End"}
              ],
              "flows": [
                {"id": "flow1", "sourceRef": "start1", "targetRef": "task1"},
                {"id": "flow2", "sourceRef": "task1", "targetRef": "end1"}
              ]
            }
            """;
    }

    private String attemptSelfRepair(String invalidJson, String originalText) {
        return callLlm("Fix this invalid JSON to match the IR schema: " + invalidJson);
    }
}
