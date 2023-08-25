package com.github.atiperarecruitment.controllers;

import com.github.atiperarecruitment.exceptions.WrongHeaderException;
import com.github.atiperarecruitment.responsedto.RequestDTO;
import com.github.atiperarecruitment.responsedto.ResponseDTO;
import com.github.atiperarecruitment.services.ApiService;
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
