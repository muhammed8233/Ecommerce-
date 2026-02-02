package com.example.ecommerce.dtos;

import com.example.ecommerce.model.PaystackInitData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackInitResponse {
    private boolean status;
    private String message;
    private PaystackInitData data;
}