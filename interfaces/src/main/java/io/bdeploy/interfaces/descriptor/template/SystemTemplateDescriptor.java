package io.bdeploy.interfaces.descriptor.template;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.bdeploy.interfaces.descriptor.variable.VariableDescriptor;

@JsonClassDescription("Defines a template which can be used to create a process systems consisting of many instances.")
public class SystemTemplateDescriptor {

    @JsonPropertyDescription("The human readable name of the template.")
    @JsonProperty(required = true)
    public String name;

    @JsonPropertyDescription("A short but thorough description of what the template will create.")
    public String description;

    @JsonPropertyDescription("A list of user-provided variables which can be used in the template. All variables are queried when applying the template.")
    public List<TemplateVariable> templateVariables = new ArrayList<>();

    @JsonPropertyDescription("A set of system variable definitions included in this template.")
    public List<VariableDescriptor> systemVariables = new ArrayList<>();

    @JsonPropertyDescription("The list of instances to create when this template is applied.")
    public List<InstanceTemplateReferenceDescriptor> instances = new ArrayList<>();

}
