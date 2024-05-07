package com.task05.model;

import java.util.Map;

public class RequestDTO {
	private final int principalId;
	private final Map<String, String> content;

	public RequestDTO(int principalId, Map<String, String> content) {
		this.principalId = principalId;
		this.content = content;
	}

	public int getPrincipalId() {
		return principalId;
	}

	public Map<String, String> getContent() {
		return content;
	}
}
