package com.task05.model;

import lombok.Data;

import java.util.Map;

@Data
public class RequestDTO {
	private int principalId;
	private Map<String, String> content;
}
