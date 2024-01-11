package com.github.githubreactiveapi.githubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepositoryDTO (String name, boolean isFork) {
}
