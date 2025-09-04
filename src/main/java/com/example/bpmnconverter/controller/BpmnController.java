package com.example.bpmnconverter.controller;

import com.example.bpmnconverter.model.ProcessIR;
import com.example.bpmnconverter.service.BpmnGeneratorService;
import com.example.bpmnconverter.service.BpmnParserService;
import com.example.bpmnconverter.service.JsonSchemaValidator;
import com.example.bpmnconverter.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class BpmnController {

    @Autowired
    private LlmService llmService;

    @Autowired
    private BpmnGeneratorService bpmnGeneratorService;

    @Autowired
    private BpmnParserService bpmnParserService;

    @Autowired
    private JsonSchemaValidator validator;

    @PostMapping(value = "/generateBpmn", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> generateBpmn(@RequestBody String freeText) {
        try {
            ProcessIR processIR = llmService.convertTextToIR(freeText);
            String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(bpmnXml);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error generating BPMN: " + e.getMessage());
        }
    }

    @PostMapping("/summarizeBpmn")
    public ResponseEntity<String> summarizeBpmn(@RequestParam("file") MultipartFile file) {
        try {
            String bpmnXml = new String(file.getBytes(), StandardCharsets.UTF_8);
            ProcessIR processIR = bpmnParserService.parseBpmnToIR(bpmnXml);
            String summary = bpmnParserService.generateTextSummary(processIR);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(summary);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error parsing BPMN: " + e.getMessage());
        }
    }

    @PostMapping("/validateIR")
    public ResponseEntity<String> validateIR(@RequestBody String irJson) {
        if (validator.isValid(irJson)) {
            return ResponseEntity.ok("Valid IR JSON");
        } else {
            return ResponseEntity.badRequest()
                    .body("Invalid IR JSON: " + validator.validate(irJson));
        }
    }
}
