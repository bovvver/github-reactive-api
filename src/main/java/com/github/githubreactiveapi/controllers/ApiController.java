package com.github.githubreactiveapi.controllers;

import com.github.githubreactiveapi.exceptions.WrongHeaderException;
import com.github.githubreactiveapi.responsedto.RequestDTO;
import com.github.githubreactiveapi.responsedto.ResponseDTO;
import com.github.githubreactiveapi.services.ApiService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    private ApiService apiService;

    @GetMapping("/repositories")
    public Mono<ResponseEntity<ResponseDTO>> getRepositories(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader, @RequestBody RequestDTO request) throws WrongHeaderException {
        return apiService.getRepositories(acceptHeader, request);
    }
}
