package com.task05.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EventDTO {
	private String id;
	private int principalId;
	private String createdAt;
	private Map<String, String> body;
}
