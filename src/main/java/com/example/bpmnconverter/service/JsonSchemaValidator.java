package com.example.bpmnconverter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class JsonSchemaValidator {
    
    private final JsonSchema schema;
    private final ObjectMapper objectMapper;

    public JsonSchemaValidator() throws IOException {
        this.objectMapper = new ObjectMapper();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        
        try (InputStream schemaStream = new ClassPathResource("ir-schema.json").getInputStream()) {
            this.schema = factory.getSchema(schemaStream);
        }
    }

    public boolean isValid(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            return errors.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public Set<ValidationMessage> validate(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return schema.validate(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate JSON", e);
        }
    }
}
