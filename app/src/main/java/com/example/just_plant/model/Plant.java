package com.example.just_plant.model;
public class Plant {
    private String id;
    private String name;
    private String image;

    private String sName;


    public Plant() {
    }

    public Plant(String id, String name, String image,String sname) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.sName=sname;
    }

    public String getsName() {
        return sName;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getImage() {
        return image;
    }
}
