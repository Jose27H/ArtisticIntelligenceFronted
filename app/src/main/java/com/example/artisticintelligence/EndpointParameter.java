package com.example.artisticintelligence;

public class EndpointParameter {
    private String name;
    private String type;
    private String description;
    private String defaultValue;
    private Double minValue;
    private Double maxValue;
    private String[] options;
    private boolean required;

    public EndpointParameter(String name, String type, String description, String defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public EndpointParameter(String name, String type, String description, String defaultValue, Double minValue, Double maxValue, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.required = required;
    }

    public EndpointParameter(String name, String type, String description, String defaultValue, String[] options, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.options = options;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public String[] getOptions() {
        return options;
    }

    public boolean isRequired() {
        return required;
    }
}