package io.kestra.plugin.atlassian_jira.issues;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.AbstractTask;
import io.kestra.plugin.atlassian_jira.client.api.IssuesApi;
import io.kestra.plugin.atlassian_jira.client.model.CreatedIssue;
import io.kestra.plugin.atlassian_jira.client.model.IssueUpdateDetails;
import io.kestra.plugin.atlassian_jira.helpers.PropertyHelper;
import io.kestra.plugin.atlassian_jira.models.AdfDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Plugin(examples = @Example(full = true, code = """
        id: atlassian_jira_create_issue
        namespace: io.kestra.plugin.jira.issues

        tasks:
          - id: create_issue
            type: io.kestra.plugin.jira.issues.CreateIssue
            projectId: "PROJ"
            issueType: "Bug"
            summary: "Critical bug in payment processing"
            description: "Detailed description of the bug:\n- Steps to reproduce\n- Expected behavior\n- Actual behavior"
            priority: "High"
            assigneeAccountId: "557058:abc123def456"
            reporterAccountId: "557058:abc123def456"
            labels: ["bug", "critical", "payment"]
    """))
@Schema(title = "Search for issues in Jira using JQL.")
public class CreateIssue extends AbstractTask implements RunnableTask<CreatedIssue> {
  @Schema(title = "Project Key", description = "The key of the project to create the issue in.")
  @NotNull
  protected Property<String> projectKey;

  @Schema(title = "Issue Type", description = "The type of the issue to create.")
  @NotNull
  protected Property<String> issueType;

  @Schema(title = "Summary", description = "The summary of the issue to create.")
  @NotNull
  protected Property<String> summary;

  @Schema(title = "Description (ADF formatted)", description = "The Atlassian Document Format description of the issue to create. For ADF formatted details, refer to: [Atlassian Document Format](https://developer.atlassian.com/cloud/jira/platform/apis/document/structure)")
  protected Property<AdfDocument> adfDescription;

  @Schema(title = "Priority", description = "The priority of the issue to create.", allowableValues = { "Highest",
      "High", "Medium", "Low", "Lowest" })
  protected Property<String> priority;

  @Schema(title = "Assignee Account ID", description = "The account ID of the assignee of the issue to create.")
  protected Property<String> assigneeAccountId;

  @Schema(title = "Reporter Account ID", description = "The account ID of the reporter of the issue to create.")
  protected Property<String> reporterAccountId;

  @Schema(title = "Labels", description = "The labels of the issue to create.")
  protected Property<List<String>> labels;

  @Override
  public CreatedIssue run(RunContext runContext) throws Exception {
    var apiClient = getApiClient(runContext);
    var issuesApi = new IssuesApi(apiClient);

    var renderedProjectKey = runContext.render(projectKey).as(String.class).orElseThrow();
    var renderedIssueType = runContext.render(issueType).as(String.class).orElseThrow();
    var renderedSummary = runContext.render(summary).as(String.class).orElseThrow();
    var renderedDescription = PropertyHelper.safeRender(runContext, adfDescription, null, AdfDocument.class);
    var renderedPriority = runContext.render(priority).as(String.class).orElse(null);
    var renderedAssigneeAccountId = runContext.render(assigneeAccountId).as(String.class).orElse(null);
    var renderedReporterAccountId = runContext.render(reporterAccountId).as(String.class).orElse(null);
    var renderedLabels = PropertyHelper.safeRenderList(runContext, labels, List.of(), String.class);

    var issueUpdateDetails = new IssueUpdateDetails();
    Map<String, Object> fields = new HashMap<>();

    // === REQUIRED FIELDS ===
    Map<String, Object> project = new HashMap<>();
    project.put("key", renderedProjectKey);
    fields.put("project", project);

    Map<String, Object> issueType = new HashMap<>();
    issueType.put("name", renderedIssueType);
    fields.put("issuetype", issueType);

    fields.put("summary", renderedSummary);

    // === OPTIONAL FIELDS ===
    if (renderedDescription != null) {
      fields.put("description", renderedDescription);
    }

    if (renderedPriority != null && !renderedPriority.isEmpty()) {
      Map<String, Object> priority = new HashMap<>();
      priority.put("name", renderedPriority);
      fields.put("priority", priority);
    }

    if (renderedAssigneeAccountId != null && !renderedAssigneeAccountId.isEmpty()) {
      Map<String, Object> assignee = new HashMap<>();
      assignee.put("accountId", renderedAssigneeAccountId);
      fields.put("assignee", assignee);
    }

    if (renderedReporterAccountId != null && !renderedReporterAccountId.isEmpty()) {
      Map<String, Object> reporter = new HashMap<>();
      reporter.put("accountId", renderedReporterAccountId);
      fields.put("reporter", reporter);
    }

    if (renderedLabels != null && !renderedLabels.isEmpty()) {
      fields.put("labels", renderedLabels);
    }

    issueUpdateDetails.setFields(fields);

    var result = issuesApi.createIssue(issueUpdateDetails, false);

    return result;
  }
}
