package com.example.just_plant.model;

public class Comment {
    private String userId;
    private String postId;
    private String commentContent;
    private long commentDate;
    private String id;
    // Empty constructor for Firebase
    public Comment() {}

    public Comment(String userId, String postId, String commentContent, long commentDate , String id) {
        this.userId = userId;
        this.postId = postId;
        this.commentContent = commentContent;
        this.commentDate = commentDate;
        this.id =id;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    // Getters
    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public long getCommentDate() {
        return commentDate;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setCommentDate(long commentDate) {
        this.commentDate = commentDate;
    }

}
