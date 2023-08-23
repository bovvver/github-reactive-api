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
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class ApiService {
    private WebClient webClient;

    public ResponseEntity<ResponseDTO> getRepositories(String acceptHeader, RequestDTO requestDTO) throws WrongHeaderException {
        checkAcceptHeader(acceptHeader);

        String username = requestDTO.getUsername();

        checkIfUserExists(username);

        List<RepositoryDTO> repositoryList = getRepositories(username);
        ResponseDTO response = new ResponseDTO(username, new HashSet<>(repositoryList));
        return ResponseEntity.ok(response);
    }

    private void checkAcceptHeader(String acceptHeader) throws WrongHeaderException {
        if (!acceptHeader.equals("application/json"))
            throw new WrongHeaderException("'Accept' header format is not acceptable. Please, provide 'application/json' instead.");
    }

    private List<RepositoryDTO> getRepositories(String username) {
        List<RepositoryDTO> allRepositories = new ArrayList<>();
        List<RepositoryDTO> fetchedRepositories;
        int page = 1;

        do {
            String repositoriesUrl = String.format("/users/%s/repos?per_page=100&page=%d", username, page);
            List<GitHubRepositoryDTO> repositories = requestToGitHub(repositoriesUrl, GitHubRepositoryDTO.class);

            fetchedRepositories = repositories.stream()
                    .filter(repository -> !repository.isFork())
                    .flatMap(repository -> getBranches(username, repository))
                    .toList();

            allRepositories.addAll(fetchedRepositories);
            page++;
        } while (fetchedRepositories.size() == 100);
        return allRepositories;
    }

    private Stream<RepositoryDTO> getBranches(String username, GitHubRepositoryDTO repository) {
        String branchesUrl = String.format("repos/%s/%s/branches", username, repository.getName());
        List<GitHubBranchDTO> branches = requestToGitHub(branchesUrl, GitHubBranchDTO.class);

        Set<BranchDTO> branchList = branches.stream()
                .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().get("sha")))
                .collect(Collectors.toSet());

        return Stream.of(new RepositoryDTO(repository.getName(), branchList));
    }

    private void checkIfUserExists(String username) {
        String url = String.format("/users/%s", username);

        webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GitHubUserDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND)
                        return Mono.error(new UserNotFoundException("User not found."));
                    return Mono.error(ex);
                }).block();
    }

    private <T> List<T> requestToGitHub(String url, Class<T> typeReference) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(typeReference)
                .collectList()
                .block();
    }
}