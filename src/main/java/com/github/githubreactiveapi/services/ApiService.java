package com.github.githubreactiveapi.services;

import com.github.githubreactiveapi.exceptions.UserNotFoundException;
import com.github.githubreactiveapi.exceptions.WrongHeaderException;
import com.github.githubreactiveapi.githubdto.GitHubBranchDTO;
import com.github.githubreactiveapi.githubdto.GitHubRepositoryDTO;
import com.github.githubreactiveapi.githubdto.GitHubUserDTO;
import com.github.githubreactiveapi.responsedto.BranchDTO;
import com.github.githubreactiveapi.responsedto.RepositoryDTO;
import com.github.githubreactiveapi.responsedto.RequestDTO;
import com.github.githubreactiveapi.responsedto.ResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApiService {
    private WebClient webClient;

    public Mono<ResponseEntity<ResponseDTO>> getRepositories(String acceptHeader, RequestDTO requestDTO) {
        Set<RepositoryDTO> allRepositories = new HashSet<>();

        return checkAcceptHeader(acceptHeader)
                .then(checkIfUserExists(requestDTO.username()))
                .flatMapMany(username -> fetchRepositories(username, 1, allRepositories))
                .collectList()
                .flatMap(repositoryList -> {
                    if (repositoryList.isEmpty()) {
                        ResponseDTO responseDTO = new ResponseDTO(requestDTO.username(), Set.of());
                        return Mono.just(ResponseEntity.ok(responseDTO));
                    } else {
                        Set<RepositoryDTO> repositorySet = repositoryList.get(0);

                        ResponseDTO responseDTO = new ResponseDTO(requestDTO.username(), repositorySet);
                        return Mono.just(ResponseEntity.ok(responseDTO));
                    }
                });
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

    private Flux<Set<RepositoryDTO>> fetchRepositories(String username, int page, Set<RepositoryDTO> allRepositories) {
        String repositoriesUrl = String.format("/users/%s/repos?per_page=100&page=%d", username, page);
        AtomicInteger filteredRepos = new AtomicInteger();

        return requestToGitHub(repositoriesUrl, GitHubRepositoryDTO.class)
                .filter(repository -> {
                    if (!repository.isFork()) return true;
                    filteredRepos.getAndIncrement();
                    return false;
                })
                .flatMap(repository -> fetchBranches(username, repository, 1, new HashSet<>()))
                .collect(Collectors.toSet())
                .flatMapMany(repositorySet -> {
                    allRepositories.addAll(repositorySet);
                    if (repositorySet.size() != (100 - filteredRepos.get())) {
                        return Flux.just(allRepositories);
                    } else {
                        return fetchRepositories(username, page + 1, allRepositories);
                    }
                });
    }

    private Flux<RepositoryDTO> fetchBranches(String username, GitHubRepositoryDTO repository, int page, Set<BranchDTO> allBranches) {
        String branchesUrl = String.format("repos/%s/%s/branches?per_page=100&page=%d", username, repository.name(), page);

        return requestToGitHub(branchesUrl, GitHubBranchDTO.class)
                .map(branch -> new BranchDTO(branch.name(), branch.commit().get("sha")))
                .collect(Collectors.toSet())
                .flatMapMany(branchSet -> {
                    allBranches.addAll(branchSet);
                    if (branchSet.size() != 100) {
                        return Flux.just(new RepositoryDTO(repository.name(), allBranches));
                    } else {
                        return fetchBranches(username, repository, page + 1, allBranches);
                    }
                });
    }

    private <T> Flux<T> requestToGitHub(String url, Class<T> typeReference) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(typeReference);
    }
}