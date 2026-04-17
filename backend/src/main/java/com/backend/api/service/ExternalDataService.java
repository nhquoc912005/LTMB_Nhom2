package com.backend.api.service;

import com.backend.api.entity.ExternalData;
import com.backend.api.repository.ExternalDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalDataService {

    private final ExternalDataRepository repository;

    public ExternalDataService(ExternalDataRepository repository) {
        this.repository = repository;
    }

    public Page<ExternalData> getAllData(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ExternalData> searchByName(String keyword, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(keyword, pageable);
    }
}
