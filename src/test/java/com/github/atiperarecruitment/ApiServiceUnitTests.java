package com.github.atiperarecruitment;

import com.github.atiperarecruitment.exceptions.UserNotFoundException;
import com.github.atiperarecruitment.exceptions.WrongHeaderException;
import com.github.atiperarecruitment.responsedto.RepositoryDTO;
import com.github.atiperarecruitment.responsedto.RequestDTO;
import com.github.atiperarecruitment.responsedto.ResponseDTO;
import com.github.atiperarecruitment.services.ApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class ApiServiceUnitTests {
    @Mock
    private WebClient webClient;
    @Mock
    private ApiService apiService;
    RequestDTO requestDTO;
    String acceptHeader;

    @BeforeEach
    void setUp() {
        requestDTO = new RequestDTO("user");
        acceptHeader = "application/json";
    }

    @Test
    @DisplayName("Should throw WrongHeaderException")
    public void testGetRepositories__wrongHeaders() throws WrongHeaderException {
        acceptHeader = "application/xml";

        Mockito.when(apiService.getRepositories(acceptHeader, requestDTO)).thenThrow(new WrongHeaderException("Wrong header."));

        WrongHeaderException exception = Assertions.assertThrows(WrongHeaderException.class, () -> {
            apiService.getRepositories(acceptHeader, requestDTO);
        });

        assertEquals(exception.getMessage(), "Wrong header.");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException")
    public void testGetRepositories__userNotFound() throws WrongHeaderException {
        Mockito.when(apiService.getRepositories(acceptHeader, requestDTO)).thenThrow(new UserNotFoundException("User not found."));

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            apiService.getRepositories(acceptHeader, requestDTO);
        });

        assertEquals(exception.getMessage(), "User not found.");
    }

    @Test
    @DisplayName("Should return user with repositories")
    public void testGetRepositories__userWithRepo() {
        RepositoryDTO repositoryDTO = new RepositoryDTO("repository", Set.of());
        ResponseDTO responseDTO = new ResponseDTO("user", Set.of(repositoryDTO));

        Mockito.when(apiService.getRepositories(acceptHeader, requestDTO)).thenReturn(Mono.just(ResponseEntity.ok(responseDTO)));

        StepVerifier.create(apiService.getRepositories(acceptHeader, requestDTO))
                .expectNextMatches(response -> Objects.requireNonNull(response.getBody()).getOwnerLogin().equals("user") && response.getBody().getRepositoryList().size() == 1)
                .verifyComplete();
    }
}
