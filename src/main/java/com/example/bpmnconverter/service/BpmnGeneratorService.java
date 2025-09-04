package com.example.bpmnconverter.service;

import com.example.bpmnconverter.model.ProcessElement;
import com.example.bpmnconverter.model.ProcessFlow;
import com.example.bpmnconverter.model.ProcessIR;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BpmnGeneratorService {

    public String generateBpmn(ProcessIR processIR) {
        StringBuilder bpmn = new StringBuilder();
        
        bpmn.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        bpmn.append("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ");
        bpmn.append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" ");
        bpmn.append("xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" ");
        bpmn.append("xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" ");
        bpmn.append("id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n");
        
        bpmn.append("  <bpmn:process id=\"").append(processIR.getProcessId()).append("\" ");
        bpmn.append("name=\"").append(processIR.getProcessName()).append("\" isExecutable=\"true\">\n");
        
        for (ProcessElement element : processIR.getElements()) {
            generateElement(bpmn, element, "    ");
        }
        
        for (ProcessFlow flow : processIR.getFlows()) {
            generateFlow(bpmn, flow, "    ");
        }
        
        bpmn.append("  </bpmn:process>\n");
        bpmn.append("</bpmn:definitions>");
        
        return bpmn.toString();
    }

    private void generateElement(StringBuilder bpmn, ProcessElement element, String indent) {
        switch (element.getType()) {
            case "start":
                bpmn.append(indent).append("<bpmn:startEvent id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "end":
                bpmn.append(indent).append("<bpmn:endEvent id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "task":
            case "userTask":
                bpmn.append(indent).append("<bpmn:userTask id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "serviceTask":
                generateServiceTask(bpmn, element, indent);
                break;
                
            case "scriptTask":
                bpmn.append(indent).append("<bpmn:scriptTask id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "exclusiveGateway":
                bpmn.append(indent).append("<bpmn:exclusiveGateway id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "parallelGateway":
                bpmn.append(indent).append("<bpmn:parallelGateway id=\"").append(element.getId()).append("\" ");
                bpmn.append("name=\"").append(element.getName()).append("\"/>\n");
                break;
                
            case "callActivity":
                generateCallActivity(bpmn, element, indent);
                break;
                
            case "subProcess":
                generateSubProcess(bpmn, element, indent);
                break;
        }
    }

    private void generateServiceTask(StringBuilder bpmn, ProcessElement element, String indent) {
        bpmn.append(indent).append("<bpmn:serviceTask id=\"").append(element.getId()).append("\" ");
        bpmn.append("name=\"").append(element.getName()).append("\">\n");
        
        Map<String, Object> config = element.getConfig();
        if (config != null) {
            Boolean requiresMapping = (Boolean) config.get("requires_mapping");
            if (requiresMapping != null && requiresMapping) {
                bpmn.append(indent).append("  <bpmn:documentation>API mapping required</bpmn:documentation>\n");
            } else {
                bpmn.append(indent).append("  <bpmn:extensionElements>\n");
                if (config.get("method") != null) {
                    bpmn.append(indent).append("    <method>").append(config.get("method")).append("</method>\n");
                }
                if (config.get("url") != null) {
                    bpmn.append(indent).append("    <url>").append(config.get("url")).append("</url>\n");
                }
                if (config.get("headers") != null) {
                    bpmn.append(indent).append("    <headers>").append(config.get("headers")).append("</headers>\n");
                }
                if (config.get("body") != null) {
                    bpmn.append(indent).append("    <body>").append(config.get("body")).append("</body>\n");
                }
                bpmn.append(indent).append("  </bpmn:extensionElements>\n");
            }
        }
        
        bpmn.append(indent).append("</bpmn:serviceTask>\n");
    }

    private void generateCallActivity(StringBuilder bpmn, ProcessElement element, String indent) {
        bpmn.append(indent).append("<bpmn:callActivity id=\"").append(element.getId()).append("\" ");
        bpmn.append("name=\"").append(element.getName()).append("\"");
        
        Map<String, Object> config = element.getConfig();
        if (config != null && config.get("calledElement") != null) {
            bpmn.append(" calledElement=\"").append(config.get("calledElement")).append("\"");
        }
        
        bpmn.append(">\n");
        
        if (config != null) {
            Boolean requiresMapping = (Boolean) config.get("requires_mapping");
            if (requiresMapping != null && requiresMapping) {
                bpmn.append(indent).append("  <bpmn:documentation>Process mapping required</bpmn:documentation>\n");
            }
        }
        
        bpmn.append(indent).append("</bpmn:callActivity>\n");
    }

    private void generateSubProcess(StringBuilder bpmn, ProcessElement element, String indent) {
        bpmn.append(indent).append("<bpmn:subProcess id=\"").append(element.getId()).append("\" ");
        bpmn.append("name=\"").append(element.getName()).append("\">\n");
        
        if (element.getSubElements() != null) {
            for (ProcessElement subElement : element.getSubElements()) {
                generateElement(bpmn, subElement, indent + "  ");
            }
        }
        
        if (element.getSubFlows() != null) {
            for (ProcessFlow subFlow : element.getSubFlows()) {
                generateFlow(bpmn, subFlow, indent + "  ");
            }
        }
        
        bpmn.append(indent).append("</bpmn:subProcess>\n");
    }

    private void generateFlow(StringBuilder bpmn, ProcessFlow flow, String indent) {
        bpmn.append(indent).append("<bpmn:sequenceFlow id=\"").append(flow.getId()).append("\" ");
        bpmn.append("sourceRef=\"").append(flow.getSourceRef()).append("\" ");
        bpmn.append("targetRef=\"").append(flow.getTargetRef()).append("\"");
        
        if (flow.getCondition() != null && !flow.getCondition().isEmpty()) {
            bpmn.append(">\n");
            bpmn.append(indent).append("  <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">");
            bpmn.append(flow.getCondition()).append("</bpmn:conditionExpression>\n");
            bpmn.append(indent).append("</bpmn:sequenceFlow>\n");
        } else {
            bpmn.append("/>\n");
        }
    }
}
