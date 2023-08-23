package com.github.atiperarecruitment.githubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubBranchDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("commit")
    private Map<String, String> commit;
}
