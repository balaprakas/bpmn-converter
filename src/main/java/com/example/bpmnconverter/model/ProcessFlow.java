package com.example.bpmnconverter.model;

public class ProcessFlow {
    private String id;
    private String sourceRef;
    private String targetRef;
    private String condition;

    public ProcessFlow() {}

    public ProcessFlow(String id, String sourceRef, String targetRef) {
        this.id = id;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
