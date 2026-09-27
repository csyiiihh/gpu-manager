package com.bingo.gpumanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;
    private Integer page;
    private Integer size;
    private List<T> records;
}