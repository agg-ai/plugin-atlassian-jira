package io.kestra.plugin.atlassian_jira.users;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.AbstractTask;
import io.kestra.plugin.atlassian_jira.client.api.UserSearchApi;
import io.kestra.plugin.atlassian_jira.client.model.User;
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
public class SearchUsers extends AbstractTask implements RunnableTask<SearchUsers.Output> {

  @Schema(title = "Query", description = "The query to filter the results by. Users with a matching 'displayName' or 'emailAddress' are returned (case insensitive).")
  @NotNull
  protected Property<String> query;

  @Schema(title = "Account ID", description = "The account ID to filter the results by.")
  protected Property<String> accountId;

  @Schema(title = "Start at", description = "The index of the first item to return in a page of results (page offset).")
  protected Property<Integer> startAt;

  @Schema(title = "Max results", description = "The maximum number of items to return per page. Must be less than or equal to 100.")
  protected Property<Integer> maxResults;

  @Schema(title = "Property", description = "A query string used to search properties. Property keys are specified by path, so property keys containing dot (.) or equals (=) characters cannot be used. The query string cannot be specified using a JSON object. Example: To search for the value of 'nested' from '{\"something\":{\"nested\":1,\"other\":2}}' use 'thepropertykey.something.nested=1'.")
  protected Property<String> property;

  @Override
  public Output run(RunContext runContext) throws Exception {
    var apiClient = getApiClient(runContext);
    var userSearchApi = new UserSearchApi(apiClient);

    var renderedQuery = runContext.render(query).as(String.class).orElseThrow();
    var renderedAccountId = PropertyHelper.safeRenderString(runContext, accountId, null);
    var renderedStartAt = runContext.render(startAt).as(Integer.class).orElse(null);
    var renderedMaxResults = runContext.render(maxResults).as(Integer.class).orElse(50);
    var renderedProperty = PropertyHelper.safeRenderString(runContext, property, null);

    if (renderedQuery == null) {
      throw new IllegalArgumentException("Query must be provided.");
    }

    var results = userSearchApi.findUsers(
        renderedQuery,
        null, // restricted because of GDPR
        renderedAccountId,
        renderedStartAt,
        renderedMaxResults,
        renderedProperty);

    return Output.builder().users(results).build();
  }

  @SuperBuilder
  @ToString
  @EqualsAndHashCode
  @Getter
  @NoArgsConstructor
  public static class Output implements io.kestra.core.models.tasks.Output {
    @Schema(title = "Users", description = "The list of users.")
    public List<User> users;
  }
}
