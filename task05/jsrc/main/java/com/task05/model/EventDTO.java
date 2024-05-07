package com.task05.model;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
public class EventDTO {
	private final String id;
	private final int principalId;
	private final String createdAt;
	private final Map<String, String> body;
}
