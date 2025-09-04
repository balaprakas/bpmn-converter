package com.example.bpmnconverter.service;

import com.example.bpmnconverter.model.ProcessElement;
import com.example.bpmnconverter.model.ProcessFlow;
import com.example.bpmnconverter.model.ProcessIR;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BpmnParserService {

    public ProcessIR parseBpmnToIR(String bpmnXml) {
        String processId = extractAttribute(bpmnXml, "bpmn:process", "id");
        String processName = extractAttribute(bpmnXml, "bpmn:process", "name");
        
        List<ProcessElement> elements = extractElements(bpmnXml);
        List<ProcessFlow> flows = extractFlows(bpmnXml);
        
        return new ProcessIR(processId, processName, elements, flows);
    }

    public String generateTextSummary(ProcessIR processIR) {
        StringBuilder summary = new StringBuilder();
        summary.append(processIR.getProcessName()).append(" starts ");
        
        ProcessElement startElement = findElementByType(processIR.getElements(), "start");
        if (startElement != null) {
            summary.append("when ").append(startElement.getName().toLowerCase()).append(". ");
        }
        
        List<ProcessElement> tasks = findElementsByType(processIR.getElements(), Arrays.asList("task", "userTask", "serviceTask", "callActivity"));
        for (int i = 0; i < tasks.size(); i++) {
            ProcessElement task = tasks.get(i);
            if (i > 0) {
                summary.append("Then ");
            }
            
            if ("callActivity".equals(task.getType())) {
                summary.append("system runs ").append(task.getName()).append(". ");
            } else {
                summary.append(task.getName().toLowerCase()).append(". ");
            }
        }
        
        ProcessElement endElement = findElementByType(processIR.getElements(), "end");
        if (endElement != null) {
            summary.append("Ends when ").append(endElement.getName().toLowerCase()).append(".");
        }
        
        return summary.toString();
    }

    private String extractAttribute(String xml, String elementName, String attributeName) {
        Pattern pattern = Pattern.compile("<" + elementName + "\\s+[^>]*" + attributeName + "=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(xml);
        return matcher.find() ? matcher.group(1) : "";
    }

    private List<ProcessElement> extractElements(String bpmnXml) {
        List<ProcessElement> elements = new ArrayList<>();
        
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:startEvent", "start"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:endEvent", "end"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:userTask", "userTask"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:serviceTask", "serviceTask"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:scriptTask", "scriptTask"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:exclusiveGateway", "exclusiveGateway"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:parallelGateway", "parallelGateway"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:callActivity", "callActivity"));
        elements.addAll(extractElementsByPattern(bpmnXml, "bpmn:subProcess", "subProcess"));
        
        return elements;
    }

    private List<ProcessElement> extractElementsByPattern(String xml, String elementName, String type) {
        List<ProcessElement> elements = new ArrayList<>();
        Pattern pattern = Pattern.compile("<" + elementName + "\\s+([^>]+)/?>");
        Matcher matcher = pattern.matcher(xml);
        
        while (matcher.find()) {
            String attributes = matcher.group(1);
            String id = extractAttributeFromString(attributes, "id");
            String name = extractAttributeFromString(attributes, "name");
            
            ProcessElement element = new ProcessElement(id, type, name);
            
            if ("callActivity".equals(type)) {
                String calledElement = extractAttributeFromString(attributes, "calledElement");
                if (calledElement != null && !calledElement.isEmpty()) {
                    Map<String, Object> config = new HashMap<>();
                    config.put("calledElement", calledElement);
                    element.setConfig(config);
                }
            }
            
            elements.add(element);
        }
        
        return elements;
    }

    private String extractAttributeFromString(String attributes, String attributeName) {
        Pattern pattern = Pattern.compile(attributeName + "=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(attributes);
        return matcher.find() ? matcher.group(1) : "";
    }

    private List<ProcessFlow> extractFlows(String bpmnXml) {
        List<ProcessFlow> flows = new ArrayList<>();
        Pattern pattern = Pattern.compile("<bpmn:sequenceFlow\\s+([^>]+)/?>");
        Matcher matcher = pattern.matcher(bpmnXml);
        
        while (matcher.find()) {
            String attributes = matcher.group(1);
            String id = extractAttributeFromString(attributes, "id");
            String sourceRef = extractAttributeFromString(attributes, "sourceRef");
            String targetRef = extractAttributeFromString(attributes, "targetRef");
            
            flows.add(new ProcessFlow(id, sourceRef, targetRef));
        }
        
        return flows;
    }

    private ProcessElement findElementByType(List<ProcessElement> elements, String type) {
        return elements.stream()
                .filter(e -> type.equals(e.getType()))
                .findFirst()
                .orElse(null);
    }

    private List<ProcessElement> findElementsByType(List<ProcessElement> elements, List<String> types) {
        return elements.stream()
                .filter(e -> types.contains(e.getType()))
                .toList();
    }
}
