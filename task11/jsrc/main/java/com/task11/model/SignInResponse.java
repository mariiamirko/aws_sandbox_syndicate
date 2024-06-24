package com.task11.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SignInResponse implements Serializable {
    private String accessToken;
}