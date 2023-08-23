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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

        Set<RepositoryDTO> repositoryList = getRepositories(username);
        ResponseDTO response = new ResponseDTO(username, repositoryList);
        return ResponseEntity.ok(response);
    }

    private void checkAcceptHeader(String acceptHeader) throws WrongHeaderException {
        if (!acceptHeader.equals("application/json"))
            throw new WrongHeaderException("'Accept' header format is not acceptable. Please, provide 'application/json' instead.");
    }

    private Set<RepositoryDTO> getRepositories(String username) {
        Set<RepositoryDTO> allRepositories = new HashSet<>();

        for (int i = 1; ; i++) {
            AtomicInteger filteredItems = new AtomicInteger(0);
            String repositoriesUrl = String.format("/users/%s/repos?per_page=100&page=%d", username, i);
            List<GitHubRepositoryDTO> repositories = requestToGitHub(repositoriesUrl, GitHubRepositoryDTO.class);
            Set<RepositoryDTO> fetchedRepositories = repositories.stream()
                    .filter(repository -> {
                        if (repository.isFork()) {
                            filteredItems.getAndIncrement();
                            return false;
                        }
                        return true;
                    })
                    .flatMap(repository -> getBranches(username, repository))
                    .collect(Collectors.toSet());

            allRepositories.addAll(fetchedRepositories);
            if (fetchedRepositories.size() != (100 - filteredItems.get())) break;
        }
        return allRepositories;
    }

    private Stream<RepositoryDTO> getBranches(String username, GitHubRepositoryDTO repository) {
        Set<BranchDTO> branchList = new HashSet<>();

        for (int i = 1; ; i++) {
            String branchesUrl = String.format("repos/%s/%s/branches?per_page=100&page=%d", username, repository.getName(), i);
            List<GitHubBranchDTO> branches = requestToGitHub(branchesUrl, GitHubBranchDTO.class);

            Set<BranchDTO> fetchedBranches = branches.stream()
                    .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().get("sha")))
                    .collect(Collectors.toSet());

            branchList.addAll(fetchedBranches);
            if (fetchedBranches.size() != 100) break;
        }
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