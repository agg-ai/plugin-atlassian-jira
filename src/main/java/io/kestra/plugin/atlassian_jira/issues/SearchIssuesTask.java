package io.kestra.plugin.atlassian_jira.issues;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.AbstractTask;
import io.kestra.plugin.atlassian_jira.client.api.IssueSearchApi;
import io.kestra.plugin.atlassian_jira.client.model.SearchAndReconcileResults;
import io.kestra.plugin.atlassian_jira.helpers.PropertyHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Plugin(examples = @Example(full = true, code = """
        id: atlassian_jira_search_issues
        namespace: io.kestra.plugin.jira.issues

        tasks:
          - id: search_issues
            type: io.kestra.plugin.jira.issues.SearchIssuesTask
            jql: "project = PROJ AND status = Open"
            nextPageToken: null
            maxResults: 50
            fields: null
            expand: null
            properties: null
            fieldsByKeys: false
            failFast: false
            reconcileIssues: null
    """))
@Schema(title = "Search for issues in Jira using JQL.")
public class SearchIssuesTask extends AbstractTask implements RunnableTask<SearchAndReconcileResults> {
  @Schema(title = "JQL query to search for issues", description = "Unbounded JQL queries are not allowed here. Please add a search restriction to your query.")
  @NotNull
  protected Property<String> jql;

  @Schema(title = "Next page token", description = "The token for a page to fetch that is not the first page.")
  protected Property<String> nextPageToken;

  @Schema(title = "Maximum number of issues to return", description = "Maximum number of issues to return. Defaults to 50.")
  protected Property<Integer> maxResults;

  @Schema(title = "Fields to return", description = "Fields to return. Defaults to 'key', 'summary', 'project'.")
  protected Property<List<String>> fields;

  @Schema(title = "Expand", description = "Use to include additional information about issues in the response.")
  protected Property<String> expand;

  @Schema(title = "Properties to return", description = "A list of up to 5 issue properties to include in the results.")
  protected Property<List<String>> properties;

  @Schema(title = "Reference fields by their key", description = "Reference fields by their key (rather than ID). The default is false.")
  protected Property<Boolean> fieldsByKeys;

  @Schema(title = "Fail fast", description = "Fail this request early if we can't retrieve all field data.")
  protected Property<Boolean> failFast;

  @Schema(title = "Reconcile issues", description = "Strong consistency issue ids to be reconciled with search results. Accepts max 50 ids.")
  protected Property<List<Long>> reconcileIssues;

  @Override
  public SearchAndReconcileResults run(RunContext runContext) throws Exception {
    var apiClient = getApiClient(runContext);
    var issueSearchApi = new IssueSearchApi(apiClient);

    var renderedJql = runContext.render(jql).as(String.class).orElse(null);
    var renderedNextPageToken = runContext.render(nextPageToken).as(String.class).orElse(null);
    var renderedMaxResults = runContext.render(maxResults).as(Integer.class).orElse(50);
    var renderedFields = PropertyHelper.safeRenderList(runContext, fields, List.of(), String.class);
    var renderedExpand = runContext.render(expand).as(String.class).orElse(null);
    var renderedProperties = PropertyHelper.safeRenderList(runContext, properties, null, String.class);
    var renderedFieldsByKeys = runContext.render(fieldsByKeys).as(Boolean.class).orElse(false);
    var renderedFailFast = runContext.render(failFast).as(Boolean.class).orElse(false);
    var renderedReconcileIssues = PropertyHelper.safeRenderList(runContext, reconcileIssues, null, Long.class);

    if (renderedJql == null || renderedJql.isEmpty()) {
      throw new IllegalArgumentException(
          "Unbounded JQL queries are not allowed here. Please add a search restriction to your query.");
    }

    if (renderedFields.isEmpty()) {
      renderedFields = List.of("key", "summary", "project");
    }

    SearchAndReconcileResults results = issueSearchApi.searchAndReconsileIssuesUsingJql(
        renderedJql,
        renderedNextPageToken,
        renderedMaxResults,
        renderedFields,
        renderedExpand,
        renderedProperties,
        renderedFieldsByKeys,
        renderedFailFast,
        renderedReconcileIssues);

    return results;
  }
}
