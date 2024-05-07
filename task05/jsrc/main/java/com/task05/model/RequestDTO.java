package com.task05.model;

import lombok.Data;

import java.util.Map;

@Data
public class RequestDTO {
	private final int principalId;
	private final Map<String, String> content;
}
