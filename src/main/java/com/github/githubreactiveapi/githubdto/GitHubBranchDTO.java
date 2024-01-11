package com.github.githubreactiveapi.githubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubBranchDTO(String name, Map<String,String> commit) {
}
