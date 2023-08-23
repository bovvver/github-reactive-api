package com.github.atiperarecruitment.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class ResponseDTO {
    @JsonProperty("login")
    private String ownerLogin;
    @JsonProperty("repositories")
    private Set<RepositoryDTO> repositoryList;
}
