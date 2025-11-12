package io.kestra.plugin.atlassian_jira;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.atlassian_jira.client.invoker.ApiClient;
import io.kestra.plugin.atlassian_jira.client.invoker.Configuration;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractTask extends Task {
    @NotNull
    protected Property<String> accessToken;

    @NotNull
    protected Property<String> cloudId;

    protected ApiClient getApiClient(RunContext runContext) throws IllegalVariableEvaluationException {
        var renderedAccessToken = runContext.render(this.accessToken).as(String.class).orElseThrow();
        var renderedCloudId = runContext.render(this.cloudId).as(String.class).orElseThrow();

        if (renderedAccessToken == null || renderedAccessToken.isEmpty()) {
            throw new IllegalArgumentException("Access Token is required to use this task.");
        }

        if (renderedCloudId == null || renderedCloudId.isEmpty()) {
            throw new IllegalArgumentException("Cloud ID is required to use this task.");
        }

        var apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("https://api.atlassian.com/ex/jira/" + renderedCloudId);
        apiClient.setAccessToken(renderedAccessToken);

        return apiClient;
    }
}
