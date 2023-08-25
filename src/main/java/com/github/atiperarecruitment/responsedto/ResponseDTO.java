package com.github.atiperarecruitment.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResponseDTO {
    @JsonProperty("login")
    private String ownerLogin;
    @JsonProperty("repositories")
    private Set<RepositoryDTO> repositoryList;
}
