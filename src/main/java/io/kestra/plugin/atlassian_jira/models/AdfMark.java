package io.kestra.plugin.atlassian_jira.models;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public class AdfMark {
    @Schema(title = "Type", description = "The type of the mark.", allowableValues = { "border", "code", "em", "link",
            "strike", "strong", "subsup", "textColor", "underline" })
    public String type;

    public Map<String, Object> attrs;
}
