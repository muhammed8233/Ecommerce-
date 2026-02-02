package com.example.ecommerce.dtos;

import com.example.ecommerce.model.Audit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackInitDataDto {
    @JsonProperty("authorization_url")
    private String authorizationUrl;

    @JsonProperty("access_code")
    private String accessCode;

    private String reference;
}
