package com.task05.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDTO {
	private int statusCode;
	private EventDTO event;
}
