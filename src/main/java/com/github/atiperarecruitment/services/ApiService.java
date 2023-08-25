package com.github.atiperarecruitment.services;

import com.github.atiperarecruitment.exceptions.UserNotFoundException;
import com.github.atiperarecruitment.exceptions.WrongHeaderException;
import com.github.atiperarecruitment.githubdto.GitHubBranchDTO;
import com.github.atiperarecruitment.githubdto.GitHubRepositoryDTO;
import com.github.atiperarecruitment.githubdto.GitHubUserDTO;
import com.github.atiperarecruitment.responsedto.BranchDTO;
import com.github.atiperarecruitment.responsedto.RepositoryDTO;
import com.github.atiperarecruitment.responsedto.RequestDTO;
import com.github.atiperarecruitment.responsedto.ResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApiService {
    private WebClient webClient;

    public Mono<ResponseEntity<ResponseDTO>> getRepositories(String acceptHeader, RequestDTO requestDTO) {
        return checkAcceptHeader(acceptHeader)
                .then(checkIfUserExists(requestDTO.getUsername()))
                .flatMap(this::fetchRepositories)
                .map(repositoryList -> new ResponseDTO(requestDTO.getUsername(), repositoryList))
                .map(ResponseEntity::ok);
    }

    private Mono<Void> checkAcceptHeader(String acceptHeader) {
        if (!acceptHeader.equals("application/json"))
            return Mono.error(new WrongHeaderException("'Accept' header format is not acceptable. Please, provide 'application/json' instead."));

        return Mono.empty();
    }

    private Mono<String> checkIfUserExists(String username) {
        if (username == null)
            return Mono.error(new UserNotFoundException("User not found."));

        String url = String.format("/users/%s", username);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GitHubUserDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UserNotFoundException("User not found."));
                    }
                    return Mono.error(ex);
                })
                .thenReturn(username);
    }

    private Mono<Set<RepositoryDTO>> fetchRepositories(String username) {
        String repositoriesUrl = String.format("/users/%s/repos?per_page=100", username);

        return requestToGitHub(repositoriesUrl, GitHubRepositoryDTO.class)
                .filter(repository -> !repository.isFork())
                .flatMap(repository -> fetchBranches(username, repository))
                .collect(Collectors.toSet());
    }

    private Flux<RepositoryDTO> fetchBranches(String username, GitHubRepositoryDTO repository) {
        String branchesUrl = String.format("repos/%s/%s/branches?per_page=100", username, repository.getName());

        return requestToGitHub(branchesUrl, GitHubBranchDTO.class)
                .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().get("sha")))
                .collect(Collectors.toSet())
                .map(branchList -> new RepositoryDTO(repository.getName(), branchList))
                .flux();
    }

    private <T> Flux<T> requestToGitHub(String url, Class<T> typeReference) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(typeReference);
    }
}