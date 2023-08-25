package com.github.atiperarecruitment;

import com.github.atiperarecruitment.controllers.ApiController;
import com.github.atiperarecruitment.responsedto.RequestDTO;
import com.github.atiperarecruitment.responsedto.ResponseDTO;
import com.github.atiperarecruitment.services.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ApiControllerUnitTests {
    private ApiController apiController;
    @Mock
    private ApiService apiService;
    private RequestDTO request;
    private String acceptHeader;

    @BeforeEach
    void setUp() {
        apiController = new ApiController(apiService);
        request = new RequestDTO("user");
        acceptHeader = "application/json";
    }

    @Test
    @DisplayName("Should return OK status")
    public void testGetRepositories() {
        ResponseEntity<ResponseDTO> responseEntity = new ResponseEntity<>(new ResponseDTO(), HttpStatus.OK);
        Mockito.when(apiService.getRepositories(acceptHeader, request))
                .thenReturn(Mono.just(responseEntity));

        StepVerifier.create(apiController.getRepositories(acceptHeader, request))
                .expectNextMatches(responseEntity::equals)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return BAD REQUEST status")
    public void testGetRepositories__emptyBody() {
        ResponseEntity<ResponseDTO> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Mockito.when(apiService.getRepositories(acceptHeader, null))
                .thenReturn(Mono.just(responseEntity));

        StepVerifier.create(apiController.getRepositories(acceptHeader, null))
                .expectNextMatches(responseEntity::equals)
                .verifyComplete();
    }
}