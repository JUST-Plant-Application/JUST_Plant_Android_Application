package com.example.just_plant.model;

public class PlantEntity {
    private String matchedIn;
    private String matchedInType;
    private String accessToken;
    private int matchPosition;
    private int matchLength;
    private String entityName;

    public PlantEntity(String matchedIn, String matchedInType, String accessToken, int matchPosition, int matchLength, String entityName) {
        this.matchedIn = matchedIn;
        this.matchedInType = matchedInType;
        this.accessToken = accessToken;
        this.matchPosition = matchPosition;
        this.matchLength = matchLength;
        this.entityName = entityName;
    }

    public String getMatchedIn() {
        return matchedIn;
    }

    public void setMatchedIn(String matchedIn) {
        this.matchedIn = matchedIn;
    }

    public String getMatchedInType() {
        return matchedInType;
    }

    public void setMatchedInType(String matchedInType) {
        this.matchedInType = matchedInType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getMatchPosition() {
        return matchPosition;
    }

    public void setMatchPosition(int matchPosition) {
        this.matchPosition = matchPosition;
    }

    public int getMatchLength() {
        return matchLength;
    }

    public void setMatchLength(int matchLength) {
        this.matchLength = matchLength;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getName() {
        return entityName;
    }



    // Getters and setters
}
