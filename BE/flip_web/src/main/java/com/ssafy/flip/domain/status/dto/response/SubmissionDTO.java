package com.ssafy.flip.domain.status.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionDTO {
    int submissionId;
    int submissionNode;
    float submissionX;
    float submissionY;

    @JsonCreator
    public SubmissionDTO(
            @JsonProperty("submissionId") int submissionId,
            @JsonProperty("submissionNode") int submissionNode,
            @JsonProperty("submissionX") float submissionX,
            @JsonProperty("submissionY") float submissionY
    ) {
        this.submissionId = submissionId;
        this.submissionNode = submissionNode;
        this.submissionX = submissionX;
        this.submissionY = submissionY;
    }
}

