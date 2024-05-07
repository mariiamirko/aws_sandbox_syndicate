package com.task05.model;

import lombok.ToString;

@ToString
public class ResponseDTO {
	private final int statusCode;
	private final EventDTO event;

	public ResponseDTO(int statusCode, EventDTO event) {
		this.statusCode = statusCode;
		this.event = event;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public EventDTO getEvent() {
		return event;
	}
}
