package com.github.githubreactiveapi.responsedto;

import java.util.Set;

public record ResponseDTO(String login, Set<RepositoryDTO> repositories) {
}
