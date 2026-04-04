package com.project_mobile.service;

public class ServiceModel {
    private String id;
    private String name;
    private String price; // <-- Đổi từ int sang String

    public ServiceModel(String id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
}