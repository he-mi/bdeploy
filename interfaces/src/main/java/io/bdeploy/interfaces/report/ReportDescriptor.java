package io.bdeploy.interfaces.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * General report description
 */
public class ReportDescriptor {

    /**
     * report type which also serves as permission scope to view this report
     */
    public ReportType type;

    /**
     * human readable name of the report
     */
    public String name;

    /**
     * short description explaining what report is about
     */
    public String description;

    /**
     * input parameters used to generate report
     */
    public List<ReportParameterDescriptor> parameters;

    /**
     * report column definitions
     */
    public List<ReportColumnDescriptor> columns;

    @JsonCreator
    public ReportDescriptor(@JsonProperty("type") ReportType type, @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("parameters") List<ReportParameterDescriptor> parameters,
            @JsonProperty("columns") List<ReportColumnDescriptor> columns) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.columns = columns;
    }
}
