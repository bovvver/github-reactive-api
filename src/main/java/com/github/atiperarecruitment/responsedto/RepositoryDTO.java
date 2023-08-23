package com.github.atiperarecruitment.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class RepositoryDTO {
    @JsonProperty("name")
    private String repositoryName;
    @JsonProperty("branches")
    private Set<BranchDTO> branchList;
}
