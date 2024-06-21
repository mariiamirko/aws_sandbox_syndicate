package com.task10.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Table {
    private int id;
    private int number;
    private int places;
    private boolean isVip;
    private int minOrder;
}
