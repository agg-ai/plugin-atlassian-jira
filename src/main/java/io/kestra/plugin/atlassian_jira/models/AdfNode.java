package io.kestra.plugin.atlassian_jira.models;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public class AdfNode {
    @Schema(title = "Type", description = "The type of the ADF node.")
    public String type;

    @Schema(title = "Content", description = "The content of the ADF node.")
    public List<AdfNode> content;

    @Schema(title = "Text", description = "The text of the ADF node.")
    public String text;

    @Schema(title = "Marks", description = "The marks of the ADF node.")
    public List<AdfMark> marks;

    @Schema(title = "Attrs", description = "The attributes of the ADF node.")
    public Map<String, Object> attrs;
}
