package com.github.githubreactiveapi.responsedto;

import java.util.Set;


public record RepositoryDTO(String name, Set<BranchDTO> branches) {
}
