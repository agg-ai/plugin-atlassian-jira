package io.kestra.plugin.atlassian_jira.projects;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.AbstractTask;
import io.kestra.plugin.atlassian_jira.client.api.ProjectsApi;
import io.kestra.plugin.atlassian_jira.client.model.PageBeanProject;
import io.kestra.plugin.atlassian_jira.helpers.PropertyHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Plugin(examples = @Example(full = true, code = """
        id: atlassian_jira_search_projects
        namespace: io.kestra.plugin.jira.projects

        tasks:
          - id: search_projects
            type: io.kestra.plugin.jira.projects.SearchProjects
            startAt: 0
            maxResults: 50
            orderBy: "key"
            projectIds: [10000, 10001]
            keys: ["PA", "PB"]
            query: "project"
            typeKey: "business"
            categoryId: 10000
            action: "view"
            expand: "description,projectKeys,lead,issueTypes,url,insight"
            status: ["live", "archived"]
            properties: ["all", "archived", "deleted", "live", "name", "projectKeys", "url", "insight"]
            propertyQuery: "[thepropertykey].something.nested=1"
    """))
@Schema(title = "Search for projects in Jira.")
public class SearchProjects extends AbstractTask implements RunnableTask<PageBeanProject> {
  @Schema(title = "Start at", description = "The index of the first item to return in a page of results (page offset).")
  protected Property<Long> startAt;

  @Schema(title = "Max results", description = "The maximum number of items to return per page. Must be less than or equal to 100.")
  protected Property<Integer> maxResults;

  @Schema(title = "Order by", description = "The field to order the results by.")
  protected Property<String> orderBy;

  @Schema(title = "Project IDs", description = "The project IDs to filter the results by.")
  protected Property<List<Long>> ids;

  @Schema(title = "Project keys", description = "The project keys to filter the results by.")
  protected Property<List<String>> keys;

  @Schema(title = "Query", description = "The query to filter the results by. Projects with a matching 'key' or 'name' are returned (case insensitive).")
  protected Property<String> query;

  @Schema(title = "Project type key", description = "The project type to filter the results by. This parameter accepts a comma-separated list. Valid values are: business, service_desk and software.")
  protected Property<String> typeKey;

  @Schema(title = "Category id", description = "The project category ID to filter the results by.")
  protected Property<Long> categoryId;

  @Schema(title = "Action", description = "The action to filter the results by.")
  protected Property<String> action;

  @Schema(title = "Expand", description = "The expand to filter the results by. This parameter accepts a comma-separated list. Valid values are: description, projectKeys, lead, issueTypes, url and insight.")
  protected Property<String> expand;

  @Schema(title = "Status", description = "The status to filter the results by. This parameter accepts a comma-separated list. Valid values are: live, archived and deleted.")
  protected Property<List<String>> status;

  @Schema(title = "Properties", description = "The properties to filter the results by. This parameter accepts a comma-separated list. Valid values are: all, archived, deleted, live, name, projectKeys, url and insight.")
  protected Property<List<Object>> properties;

  @Schema(title = "Property query", description = "The property query to filter the results by. The query string cannot be specified using a JSON object. For example, to search for the value of 'nested' from '{\"something\":{\"nested\":1,\"other\":2}}' use '[thepropertykey].something.nested=1'.")
  protected Property<String> propertyQuery;

  @Override
  public PageBeanProject run(RunContext runContext) throws Exception {
    var apiClient = getApiClient(runContext);
    var projectsApi = new ProjectsApi(apiClient);

    var renderedStartAt = runContext.render(startAt).as(Long.class).orElse(null);
    var renderedMaxResults = runContext.render(maxResults).as(Integer.class).orElse(50);
    var renderedOrderBy = PropertyHelper.safeRenderString(runContext, orderBy, null);
    var renderedIdList = PropertyHelper.safeRenderList(runContext, ids, null, Long.class);
    var renderedKeysList = PropertyHelper.safeRenderList(runContext, keys, null, String.class);
    var renderedQuery = PropertyHelper.safeRenderString(runContext, query, null);
    var renderedTypeKey = PropertyHelper.safeRenderString(runContext, typeKey, null);
    var renderedCategoryId = runContext.render(categoryId).as(Long.class).orElse(null);
    var renderedAction = PropertyHelper.safeRenderString(runContext, action, null);
    var renderedExpand = PropertyHelper.safeRenderString(runContext, expand, null);
    var renderedStatus = PropertyHelper.safeRenderList(runContext, status, null, String.class);
    var renderedProperties = PropertyHelper.safeRenderList(runContext, properties, null, Object.class);
    var renderedPropertyQuery = PropertyHelper.safeRenderString(runContext, propertyQuery, null);

    Set<Long> renderedId = renderedIdList != null ? new HashSet<>(renderedIdList) : null;
    Set<String> renderedKeys = renderedKeysList != null ? new HashSet<>(renderedKeysList) : null;

    var results = projectsApi.searchProjects(
        renderedStartAt,
        renderedMaxResults,
        renderedOrderBy,
        renderedId,
        renderedKeys,
        renderedQuery,
        renderedTypeKey,
        renderedCategoryId,
        renderedAction,
        renderedExpand,
        renderedStatus,
        renderedProperties,
        renderedPropertyQuery);

    return results;
  }
}
