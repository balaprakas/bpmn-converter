package com.example.bpmnconverter.service;

import com.example.bpmnconverter.model.ProcessElement;
import com.example.bpmnconverter.model.ProcessFlow;
import com.example.bpmnconverter.model.ProcessIR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BpmnGeneratorServiceTest {

    private BpmnGeneratorService bpmnGeneratorService;

    @BeforeEach
    void setUp() {
        bpmnGeneratorService = new BpmnGeneratorService();
    }

    @Test
    void testSimpleTaskFlow() {
        List<ProcessElement> elements = Arrays.asList(
                new ProcessElement("start1", "start", "Start"),
                new ProcessElement("task1", "userTask", "Complete task"),
                new ProcessElement("end1", "end", "End")
        );

        List<ProcessFlow> flows = Arrays.asList(
                new ProcessFlow("flow1", "start1", "task1"),
                new ProcessFlow("flow2", "task1", "end1")
        );

        ProcessIR processIR = new ProcessIR("simple_process", "Simple Process", elements, flows);
        String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);

        assertTrue(bpmnXml.contains("<bpmn:startEvent id=\"start1\""));
        assertTrue(bpmnXml.contains("<bpmn:userTask id=\"task1\""));
        assertTrue(bpmnXml.contains("<bpmn:endEvent id=\"end1\""));
        assertTrue(bpmnXml.contains("<bpmn:sequenceFlow id=\"flow1\""));
        assertTrue(bpmnXml.contains("sourceRef=\"start1\" targetRef=\"task1\""));
    }

    @Test
    void testRecursiveSubprocess() {
        ProcessElement subTask = new ProcessElement("subtask1", "userTask", "Sub task");
        ProcessFlow subFlow = new ProcessFlow("subflow1", "subtask1", "subtask1");

        ProcessElement subprocess = new ProcessElement("subprocess1", "subProcess", "Sub Process");
        subprocess.setSubElements(Arrays.asList(subTask));
        subprocess.setSubFlows(Arrays.asList(subFlow));

        List<ProcessElement> elements = Arrays.asList(
                new ProcessElement("start1", "start", "Start"),
                subprocess,
                new ProcessElement("end1", "end", "End")
        );

        List<ProcessFlow> flows = Arrays.asList(
                new ProcessFlow("flow1", "start1", "subprocess1"),
                new ProcessFlow("flow2", "subprocess1", "end1")
        );

        ProcessIR processIR = new ProcessIR("subprocess_process", "Subprocess Process", elements, flows);
        String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);

        assertTrue(bpmnXml.contains("<bpmn:subProcess id=\"subprocess1\""));
        assertTrue(bpmnXml.contains("<bpmn:userTask id=\"subtask1\""));
        assertTrue(bpmnXml.contains("</bpmn:subProcess>"));
    }

    @Test
    void testCallActivity() {
        Map<String, Object> config = new HashMap<>();
        config.put("calledElement", "risk_assessment");

        ProcessElement callActivity = new ProcessElement("call1", "callActivity", "Risk Assessment");
        callActivity.setConfig(config);

        List<ProcessElement> elements = Arrays.asList(
                new ProcessElement("start1", "start", "Start"),
                callActivity,
                new ProcessElement("end1", "end", "End")
        );

        List<ProcessFlow> flows = Arrays.asList(
                new ProcessFlow("flow1", "start1", "call1"),
                new ProcessFlow("flow2", "call1", "end1")
        );

        ProcessIR processIR = new ProcessIR("call_process", "Call Process", elements, flows);
        String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);

        assertTrue(bpmnXml.contains("<bpmn:callActivity id=\"call1\""));
        assertTrue(bpmnXml.contains("calledElement=\"risk_assessment\""));
    }

    @Test
    void testServiceTaskWithKnownAPI() {
        Map<String, Object> config = new HashMap<>();
        config.put("method", "POST");
        config.put("url", "https://api.example.com/validate");
        config.put("headers", Map.of("Content-Type", "application/json"));
        config.put("body", "{\"data\": \"test\"}");

        ProcessElement serviceTask = new ProcessElement("service1", "serviceTask", "API Call");
        serviceTask.setConfig(config);

        List<ProcessElement> elements = Arrays.asList(
                new ProcessElement("start1", "start", "Start"),
                serviceTask,
                new ProcessElement("end1", "end", "End")
        );

        List<ProcessFlow> flows = Arrays.asList(
                new ProcessFlow("flow1", "start1", "service1"),
                new ProcessFlow("flow2", "service1", "end1")
        );

        ProcessIR processIR = new ProcessIR("api_process", "API Process", elements, flows);
        String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);

        assertTrue(bpmnXml.contains("<bpmn:serviceTask id=\"service1\""));
        assertTrue(bpmnXml.contains("<bpmn:extensionElements>"));
        assertTrue(bpmnXml.contains("<method>POST</method>"));
        assertTrue(bpmnXml.contains("<url>https://api.example.com/validate</url>"));
    }

    @Test
    void testServiceTaskWithUnknownAPI() {
        Map<String, Object> config = new HashMap<>();
        config.put("requires_mapping", true);

        ProcessElement serviceTask = new ProcessElement("service1", "serviceTask", "Unknown API");
        serviceTask.setConfig(config);

        List<ProcessElement> elements = Arrays.asList(
                new ProcessElement("start1", "start", "Start"),
                serviceTask,
                new ProcessElement("end1", "end", "End")
        );

        List<ProcessFlow> flows = Arrays.asList(
                new ProcessFlow("flow1", "start1", "service1"),
                new ProcessFlow("flow2", "service1", "end1")
        );

        ProcessIR processIR = new ProcessIR("unknown_api_process", "Unknown API Process", elements, flows);
        String bpmnXml = bpmnGeneratorService.generateBpmn(processIR);

        assertTrue(bpmnXml.contains("<bpmn:serviceTask id=\"service1\""));
        assertTrue(bpmnXml.contains("<bpmn:documentation>API mapping required</bpmn:documentation>"));
    }
}
