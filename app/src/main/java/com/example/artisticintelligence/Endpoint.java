package com.example.artisticintelligence;

import java.util.ArrayList;
import java.util.List;

public class Endpoint {
    private String id;
    private String name;
    private String description;
    private List<EndpointParameter> parameters;

    public Endpoint(String id, String name, String description, List<EndpointParameter> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    // Add getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<EndpointParameter> getParameters() {
        return parameters;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParameters(List<EndpointParameter> parameters) {
        this.parameters = parameters;
    }

    // Add toString method
    @Override
    public String toString() {
        return "Endpoint{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    // Add equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return id.equals(endpoint.id) &&
                name.equals(endpoint.name) &&
                description.equals(endpoint.description) &&
                parameters.equals(endpoint.parameters);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    // Add copy constructor
    public Endpoint(Endpoint endpoint) {
        this.id = endpoint.id;
        this.name = endpoint.name;
        this.description = endpoint.description;
        this.parameters = endpoint.parameters;
    }

    // Add copy method
    public Endpoint copy() {
        return new Endpoint(this);
    }

    // Add builder pattern
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private List<EndpointParameter> parameters;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setParameters(List<EndpointParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Endpoint build() {
            return new Endpoint(id, name, description, parameters);
        }
    }

    // Add static factory method
    public static Endpoint create(String id, String name, String description, List<EndpointParameter> parameters) {
        return new Endpoint(id, name, description, parameters);
    }

    // Add method to add a parameter
    public void addParameter(EndpointParameter parameter) {
        parameters.add(parameter);
    }

    // Add method to remove a parameter
    public void removeParameter(EndpointParameter parameter) {
        parameters.remove(parameter);
    }

    // Add method to get a parameter by name
    public EndpointParameter getParameterByName(String name) {
        for (EndpointParameter parameter : parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    // Add method to check if a parameter exists
    public boolean hasParameter(String name) {
        for (EndpointParameter parameter : parameters) {
            if (parameter.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Add method to clear all parameters
    public void clearParameters() {
        parameters.clear();
    }

    // Add method to get the number of parameters
    public int getParameterCount() {
        return parameters.size();
    }

    // Add method to check if the endpoint has parameters
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    // Add method to check if the endpoint has required parameters
    public boolean hasRequiredParameters() {
        for (EndpointParameter parameter : parameters) {
            if (parameter.isRequired()) {
                return true;
            }
        }
        return false;
    }

    // Add method to get the names of all parameters
    public List<String> getParameterNames() {
        List<String> names = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            names.add(parameter.getName());
        }
        return names;
    }

    // Add method to get the types of all parameters
    public List<String> getParameterTypes() {
        List<String> types = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            types.add(parameter.getType());
        }
        return types;
    }

    // Add method to get the descriptions of all parameters
    public List<String> getParameterDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            descriptions.add(parameter.getDescription());
        }
        return descriptions;
    }

    // Add method to get the default values of all parameters
    public List<String> getParameterDefaultValues() {
        List<String> defaultValues = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            defaultValues.add(parameter.getDefaultValue());
        }
        return defaultValues;
    }

    // Add method to get the minimum values of all parameters
    public List<Double> getParameterMinValues() {
        List<Double> minValues = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            minValues.add(parameter.getMinValue());
        }
        return minValues;
    }

    // Add method to get the maximum values of all parameters
    public List<Double> getParameterMaxValues() {
        List<Double> maxValues = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            maxValues.add(parameter.getMaxValue());
        }
        return maxValues;
    }

    // Add method to get the options of all parameters
    public List<String[]> getParameterOptions() {
        List<String[]> optionsList = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            optionsList.add(parameter.getOptions());
        }
        return optionsList;
    }

    // Add method to get the required status of all parameters
    public List<Boolean> getParameterRequiredStatus() {
        List<Boolean> requiredStatuses = new ArrayList<>();
        for (EndpointParameter parameter : parameters) {
            requiredStatuses.add(parameter.isRequired());
        }
        return requiredStatuses;
    }

    // Add method to get the parameter at a specific index
    public EndpointParameter getParameter(int index) {
        return parameters.get(index);
    }

    // Add method to set the parameter at a specific index
    public void setParameter(int index, EndpointParameter parameter) {
        parameters.set(index, parameter);
    }

    // Add method to remove the parameter at a specific index
    public void removeParameter(int index) {
        parameters.remove(index);
    }


}