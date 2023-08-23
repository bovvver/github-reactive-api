package com.github.atiperarecruitment.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class BranchDTO {
    @JsonProperty("name")
    private String branchName;
    @JsonProperty("lastCommitSha")
    private String lastCommitSha;
}
