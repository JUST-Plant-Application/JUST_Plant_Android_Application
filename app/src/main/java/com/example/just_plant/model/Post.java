package com.example.just_plant.model;

import com.google.firebase.firestore.FirebaseFirestore;


public class Post {
    private String postId;
    private String postImage;
    private String PostContent;
    private String PostAuthor;
    private String location;
    private long postTime;


    FirebaseFirestore db;

    public Post(String postId, String postImage, String postContent, String postAuthor, long postDate) {
        this.postId = postId;
        this.postImage = postImage;
        this.PostContent = postContent;
        this.PostAuthor = postAuthor;
        this.postTime=postDate;

    }

    public Post(String postId, String postImage, String postContent) {
        this.postId = postId;
        this.postImage = postImage;
        this.PostContent = postContent;

    }
    public Post() {
    }

    public String getPostId() {
        return postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public String getLocation() {
        return location;
    }

    public void setPostAuthor(String postAuthor) {
        PostAuthor = postAuthor;
    }

    public void setPostContent(String postContent) {
        PostContent = postContent;
    }

    public long getPostTime() {
        return postTime;
    }

    public String getPostContent() {
        return PostContent;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public String getPostAuthor() {
        return PostAuthor;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", postImage='" + postImage + '\'' +
                ", postContent='" + PostContent + '\'' +
                ", postAuthor='" + PostAuthor + '\'' +
                '}';
    }
}

