package com.example.just_plant.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.just_plant.CommentActivity;
import com.example.just_plant.EditPostActivity;
import com.example.just_plant.Feed_page;
import com.example.just_plant.Login;
import com.example.just_plant.R;
import com.example.just_plant.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    public static final int EDIT_POST_REQUEST_CODE = 100;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        return new ViewHolder(view);
    }
    public void refreshPostList() {
        db.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mPost.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            mPost.add(post);
                        }
                        notifyDataSetChanged();
                    }
                });
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(i);
        Log.d("PostAdapter", "Post: " + post.toString());
        Glide.with(mContext).load(post.getPostImage()).into(viewHolder.post_image);

        long postDate = post.getPostTime();

        // Set the formatted time ago text
        String timeAgo = getTimeAgo(postDate);
        if (timeAgo != null) {
            viewHolder.postDate.setText(timeAgo);
        } else {
            viewHolder.postDate.setText("just now");
        }

        // Check if post content is null or empty
        if (post.getPostContent() == null || post.getPostContent().isEmpty()) {
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(post.getPostContent());
        }

        // Load publisher info
        publisherInfo(viewHolder.checkExpertPost, viewHolder.image_profile, viewHolder.location, viewHolder.authorName, post.getPostAuthor());

        // Check if the logged-in user is the author of the post
        if (post.getPostAuthor().equals(firebaseUser.getUid())) {
            viewHolder.edit_post_btn.setVisibility(View.VISIBLE);
            viewHolder.edit_post_btn.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, EditPostActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("postImage", post.getPostImage());
                intent.putExtra("postDescription", post.getPostContent());
                ((Activity) mContext).startActivityForResult(intent, EDIT_POST_REQUEST_CODE);
            });

            viewHolder.delete_post_btn.setVisibility(View.VISIBLE);
            viewHolder.delete_post_btn.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", (dialog, id) -> {
                            int position = viewHolder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                deletePost(post.getPostId(), position);
                            }
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            });
        } else {
            viewHolder.edit_post_btn.setVisibility(View.GONE);
            viewHolder.delete_post_btn.setVisibility(View.GONE);
        }
        // Set like button status and click listener
        isLiked(post.getPostId(), viewHolder.like);
        viewHolder.like.setOnClickListener(v -> {
            Log.d("PostAdapter", "Like button clicked");
            if (viewHolder.like.getTag().equals("like")) {
                unlikePost(post.getPostId());
            } else {
                likePost(post.getPostId());
            }
        });

        // Get and display the number of likes for the post
        nrLikes(viewHolder.like_count, post.getPostId());

        // Set comment button click listener
        viewHolder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("postAuthor", post.getPostAuthor());
            mContext.startActivity(intent);
        });

        // Get and display the number of comments for the post
        nrComments(viewHolder.comment_count, post.getPostId());
    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == EDIT_POST_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            refreshPostList();
//        }
//    }

    private void deletePost(String postId, int position) {
        db.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("PostAdapter", "Post successfully deleted!");
                        removePost(position); // Remove the post from the dataset and notify the adapter
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("PostAdapter", "Error deleting document", e);
                    }
                });
    }

    public void removePost(int position) {
        mPost.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public void setPosts(List<Post> posts) {
        this.mPost = posts;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_profile, post_image, like, comment, checkExpertPost, delete_post_btn, edit_post_btn;
        public TextView authorName, description, like_count, comment_count, postDate, location;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like_btn);
            comment = itemView.findViewById(R.id.comment_btn);
            authorName = itemView.findViewById(R.id.postAuthorName);
            description = itemView.findViewById(R.id.post_description);
            like_count = itemView.findViewById(R.id.like_count);
            comment_count = itemView.findViewById(R.id.comment_count);
            postDate = itemView.findViewById(R.id.postdate);
            location = itemView.findViewById(R.id.location);
            checkExpertPost = itemView.findViewById(R.id.checkExpertPost);
            delete_post_btn = itemView.findViewById(R.id.delete_post_btn);
            edit_post_btn = itemView.findViewById(R.id.edit_post_btn);

            like.setTag("unlike");
        }
    }

    private void isLiked(String postId, ImageView imageView) {
        db.collection("Likes")
                .whereEqualTo("userId", firebaseUser.getUid())
                .whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // User has liked the post
                            imageView.setImageResource(R.drawable.like);
                            imageView.setTag("like");
                        } else {
                            // User has not liked the post
                            imageView.setImageResource(R.drawable.unlike);
                            imageView.setTag("unlike");
                        }
                    } else {
                        Log.d("PostAdapter", "Error getting likes: ", task.getException());
                    }
                });
    }

    private void nrLikes(final TextView likes, String postId) {
        db.collection("Likes")
                .whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        likes.setText(String.valueOf(task.getResult().size()));
                    } else {
                        Log.d("PostAdapter", "Error getting like count: ", task.getException());
                    }
                });
    }

    private void nrComments(final TextView comments, String postId) {
        db.collection("Comments").whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        comments.setText(String.valueOf(task.getResult().size()));
                    } else {
                        Log.e("PostAdapter", "Error getting comments count: ", task.getException());
                    }
                });
    }

    private void likePost(String postId) {
        Map<String, Object> like = new HashMap<>();
        like.put("userId", firebaseUser.getUid());
        like.put("postId", postId);

        db.collection("Likes")
                .add(like)
                .addOnSuccessListener(documentReference -> {
                    Log.d("PostAdapter", "Successfully liked the post");
                    // Refresh the like status
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("PostAdapter", "Error liking the post", e);
                });
    }

    private void unlikePost(String postId) {
        db.collection("Likes")
                .whereEqualTo("userId", firebaseUser.getUid())
                .whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("Likes")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("PostAdapter", "Successfully unliked the post");
                                        // Refresh the like status
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("PostAdapter", "Error unliking the post", e);
                                    });
                        }
                    } else {
                        Log.e("PostAdapter", "Error getting documents: ", task.getException());
                    }
                });
    }

    public static String getTimeAgo(long time) {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + "s ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + "d ago";
        }
    }

    private void publisherInfo(final ImageView expertCheck, final ImageView image_profile, final TextView location, final TextView authorName, String userId) {
        if (userId == null || userId.isEmpty()) {
            // Handle the case where userId is null or empty
            Toast.makeText(mContext, "Error: User ID is null or empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        boolean expert = document.getBoolean("Expert user");
                        String userName = document.getString("user name");
                        String userLocation = document.getString("location");
                        String profilePhoto = document.getString("profile photo");
                        if (userName != null) {
                            location.setText(userLocation);
                            authorName.setText(userName);
                        }
                        if (profilePhoto != null) {
                            Glide.with(mContext).load(profilePhoto).into(image_profile);
                        }
                        if (expert) {
                            expertCheck.setImageResource(R.drawable.expert);
                        }
                    }
                }
            }
        });
    }
}

