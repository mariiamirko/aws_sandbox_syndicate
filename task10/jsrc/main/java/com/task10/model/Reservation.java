package com.task10.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Reservation {
    private int tableNumber;
    private String clientName;
    private String phoneNumber;
    private String date;
    private String slotTimeStart;
    private String slotTimeEnd;
}
