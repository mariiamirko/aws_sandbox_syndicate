package com.task10.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetReservationsResponse {
    private List<Reservation> reservations;
}