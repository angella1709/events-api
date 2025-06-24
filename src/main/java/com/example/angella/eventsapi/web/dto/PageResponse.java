package com.example.angella.eventsapi.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long totalElements;

    private Integer totalPages;

    private List<T> data;

}
