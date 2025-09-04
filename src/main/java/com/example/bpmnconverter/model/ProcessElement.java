package com.example.bpmnconverter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ProcessElement {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> config;
    
    @JsonProperty("subElements")
    private List<ProcessElement> subElements;
    
    @JsonProperty("subFlows")
    private List<ProcessFlow> subFlows;

    public ProcessElement() {}

    public ProcessElement(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<ProcessElement> getSubElements() {
        return subElements;
    }

    public void setSubElements(List<ProcessElement> subElements) {
        this.subElements = subElements;
    }

    public List<ProcessFlow> getSubFlows() {
        return subFlows;
    }

    public void setSubFlows(List<ProcessFlow> subFlows) {
        this.subFlows = subFlows;
    }
}
