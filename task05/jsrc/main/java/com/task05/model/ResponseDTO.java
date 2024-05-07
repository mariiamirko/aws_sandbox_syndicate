package com.task05.model;

import lombok.Data;
import lombok.ToString;

@Data
public class ResponseDTO {
	private final int statusCode;
	private final EventDTO event;
}
