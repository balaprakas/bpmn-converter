package com.example.bpmnconverter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProcessIR {
    @JsonProperty("processId")
    private String processId;
    
    @JsonProperty("processName")
    private String processName;
    
    private List<ProcessElement> elements;
    private List<ProcessFlow> flows;

    public ProcessIR() {}

    public ProcessIR(String processId, String processName, List<ProcessElement> elements, List<ProcessFlow> flows) {
        this.processId = processId;
        this.processName = processName;
        this.elements = elements;
        this.flows = flows;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public List<ProcessElement> getElements() {
        return elements;
    }

    public void setElements(List<ProcessElement> elements) {
        this.elements = elements;
    }

    public List<ProcessFlow> getFlows() {
        return flows;
    }

    public void setFlows(List<ProcessFlow> flows) {
        this.flows = flows;
    }
}
