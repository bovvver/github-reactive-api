package com.github.githubreactiveapi.githubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubUserDTO (String login) {
}
