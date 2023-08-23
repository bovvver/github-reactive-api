package com.github.atiperarecruitment.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorDTO {
    @JsonProperty("status")
    private int status;
    @JsonProperty("Message")
    private String message;
}
