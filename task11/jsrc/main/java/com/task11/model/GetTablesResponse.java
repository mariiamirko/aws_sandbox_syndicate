package com.task11.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetTablesResponse {
    private List<Table> tables;
}