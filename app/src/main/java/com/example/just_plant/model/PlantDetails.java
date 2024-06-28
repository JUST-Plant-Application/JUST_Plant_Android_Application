package com.example.just_plant.model;

import java.util.List;
public class PlantDetails {
    private String name;
    private String imageUrl;
    private String entityId;

    public PlantDetails(String name, String imageUrl, String entityId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEntityId() {
        return entityId;
    }
}

