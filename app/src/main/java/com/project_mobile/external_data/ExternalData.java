package com.project_mobile.external_data;

import com.google.gson.annotations.SerializedName;

public class ExternalData {
    private Long id;
    private String name;
    private String description;

    public ExternalData(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
