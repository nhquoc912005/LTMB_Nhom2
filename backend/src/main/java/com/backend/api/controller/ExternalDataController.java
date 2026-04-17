package com.backend.api.controller;

import com.backend.api.entity.ExternalData;
import com.backend.api.service.ExternalDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class ExternalDataController {

    private final ExternalDataService service;

    public ExternalDataController(ExternalDataService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ExternalData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getAllData(PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public Page<ExternalData> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchByName(keyword, PageRequest.of(page, size));
    }
}
