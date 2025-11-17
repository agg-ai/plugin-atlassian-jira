package io.kestra.plugin.atlassian_jira.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "ADF Document", description = "The Atlassian Document Format document.")
public class AdfDocument {

    @Schema(title = "Version", description = "The version of the ADF document.")
    public Integer version;

    @Schema(title = "Type", description = "The type of the ADF document.")
    public String type;

    @Schema(title = "Content", description = "The content of the ADF document.")
    public List<AdfNode> content;
}
