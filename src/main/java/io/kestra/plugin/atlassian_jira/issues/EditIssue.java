package io.kestra.plugin.atlassian_jira.issues;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.AbstractTask;
import io.kestra.plugin.atlassian_jira.client.api.IssuesApi;
import io.kestra.plugin.atlassian_jira.client.model.FieldUpdateOperation;
import io.kestra.plugin.atlassian_jira.client.model.IssueUpdateDetails;
import io.kestra.plugin.atlassian_jira.helpers.PropertyHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Plugin(examples = @Example(full = true, code = """
        id: atlassian_jira_edit_issue
        namespace: io.kestra.plugin.jira.issues

        tasks:
          - id: edit_issue
            type: io.kestra.plugin.jira.issues.EditIssue
            issueIdOrKey: "PROJ-123"
            issueType: "Bug"
            summary: "Critical bug in payment processing"
            description: "Detailed description of the bug:\n- Steps to reproduce\n- Expected behavior\n- Actual behavior"
    """))
@Schema(title = "Search for issues in Jira using JQL.")
public class EditIssue extends AbstractTask implements RunnableTask<EditIssue.Output> {
  @Schema(title = "Issue ID or Key", description = "The ID or key of the issue to edit.")
  @NotNull
  protected Property<String> issueIdOrKey;

  @Schema(title = "Issue Type", description = "The type of the issue to create.")
  @NotNull
  protected Property<String> issueType;

  @Schema(title = "Summary", description = "The summary of the issue to create.")
  @NotNull
  protected Property<String> summary;

  @Schema(title = "Description (ADF formatted)", description = "The Atlassian Document Format description of the issue to create. For ADF formatted details, refer to: [Atlassian Document Format](https://developer.atlassian.com/cloud/jira/platform/apis/document/structure)")
  protected Property<String> isueDescription;

  @Schema(title = "Priority", description = "The priority of the issue to create.", allowableValues = { "Highest",
      "High", "Medium", "Low", "Lowest" })
  protected Property<String> priority;

  @Schema(title = "Assignee Account ID", description = "The account ID of the assignee of the issue to create.")
  protected Property<String> assigneeAccountId;

  @Schema(title = "Reporter Account ID", description = "The account ID of the reporter of the issue to create.")
  protected Property<String> reporterAccountId;

  @Schema(title = "Labels to Add", description = "The labels to add to the issue.")
  protected Property<List<String>> labelsToAdd;

  @Schema(title = "Labels to Remove", description = "The labels to remove from the issue.")
  protected Property<List<String>> labelsToRemove;

  @Override
  public Output run(RunContext runContext) throws Exception {
    var apiClient = getApiClient(runContext);
    var issuesApi = new IssuesApi(apiClient);

    var renderedIssueIdOrKey = runContext.render(issueIdOrKey).as(String.class).orElseThrow();
    var renderedIssueType = runContext.render(issueType).as(String.class).orElseThrow();
    var renderedSummary = runContext.render(summary).as(String.class).orElseThrow();
    var renderedDescription = runContext.render(isueDescription).as(String.class).orElse(null);
    var renderedPriority = runContext.render(priority).as(String.class).orElse(null);
    var renderedAssigneeAccountId = runContext.render(assigneeAccountId).as(String.class).orElse(null);
    var renderedReporterAccountId = runContext.render(reporterAccountId).as(String.class).orElse(null);
    var renderedLabelsToAdd = PropertyHelper.safeRenderList(runContext, labelsToAdd, List.of(), String.class);
    var renderedLabelsToRemove = PropertyHelper.safeRenderList(runContext, labelsToRemove, List.of(), String.class);

    var issueUpdateDetails = new IssueUpdateDetails();
    Map<String, Object> fields = new HashMap<>();

    // === REQUIRED FIELDS ===
    Map<String, Object> issueType = new HashMap<>();
    issueType.put("name", renderedIssueType);
    fields.put("issuetype", issueType);

    fields.put("summary", renderedSummary);

    // === OPTIONAL FIELDS ===
    if (renderedDescription != null && !renderedDescription.isEmpty()) {
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

    Map<String, List<FieldUpdateOperation>> update = new HashMap<>();
    List<FieldUpdateOperation> updateLabelsOperations = new ArrayList<>();

    if (renderedLabelsToAdd != null && !renderedLabelsToAdd.isEmpty()) {
      for (String label : renderedLabelsToAdd) {
        FieldUpdateOperation addLabel = new FieldUpdateOperation();
        addLabel.setAdd(label);
        updateLabelsOperations.add(addLabel);
      }

      update.put("labels", updateLabelsOperations);
      issueUpdateDetails.setUpdate(update);
    }

    if (renderedLabelsToRemove != null && !renderedLabelsToRemove.isEmpty()) {
      for (String label : renderedLabelsToRemove) {
        FieldUpdateOperation removeLabel = new FieldUpdateOperation();
        removeLabel.setRemove(label);
        updateLabelsOperations.add(removeLabel);
      }

      update.put("labels", updateLabelsOperations);
      issueUpdateDetails.setUpdate(update);
    }

    issueUpdateDetails.setFields(fields);

    var result = issuesApi.editIssue(
        renderedIssueIdOrKey,
        issueUpdateDetails,
        null,
        null,
        null,
        true,
        null);

    return Output.builder().result(result).build();
  }

  @SuperBuilder
  @ToString
  @EqualsAndHashCode
  @Getter
  @NoArgsConstructor
  public static class Output implements io.kestra.core.models.tasks.Output {
    @Schema(title = "Result", description = "The result of the issue edit.")
    public Object result;
  }
}
