package io.kestra.plugin.jira;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.jira.client.invoker.ApiClient;
import io.kestra.plugin.jira.client.invoker.Configuration;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractTask extends Task {
    protected Property<String> accessToken;

    protected Property<String> cloudId;

    protected ApiClient getApiClient(RunContext runContext) throws IllegalVariableEvaluationException {
        var renderedAccessToken = runContext.render(this.accessToken).as(String.class).orElseThrow();
        var renderedCloudId = runContext.render(this.cloudId).as(String.class).orElseThrow();

        var apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("https://api.atlassian.com/ex/jira/" + renderedCloudId);
        apiClient.setAccessToken(renderedAccessToken);

        return apiClient;
    }
}
