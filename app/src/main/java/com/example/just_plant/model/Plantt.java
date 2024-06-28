package com.example.just_plant.model;

public class Plantt {
    private String plantId;
    private String plantName;
    private String scientificName;
    private String imageUrl;
    private String addedDate;

    public Plantt() {
        // Empty constructor needed for Firestore to automatically convert documents to POJOs
    }

    public Plantt(String plantId, String plantName, String scientificName, String imageUrl, String addedDate) {
        this.plantId = plantId;
        this.plantName = plantName;
        this.scientificName = scientificName;
        this.imageUrl = imageUrl;
        this.addedDate = addedDate;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
}
