package com.task10.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SignInResponse implements Serializable {
    private String accessToken;
}