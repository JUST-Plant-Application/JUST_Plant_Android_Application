package com.example.just_plant.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.just_plant.R;
import com.example.just_plant.model.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> comments;
    private FirebaseFirestore db;

    public CommentAdapter(Context context, List<Comment> comments, FirebaseFirestore db) {
        this.context = context;
        this.comments = comments;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        long commentDate = comment.getCommentDate();
        String timeAgo = getTimeAgo(commentDate);
        holder.commentDate.setText(timeAgo != null ? timeAgo : "just now");
        holder.commentDescription.setText(comment.getCommentContent());

        // Edit and delete options
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                // Show options to edit or delete
                showEditDeleteDialog(comment, position);
            }
            return true;
        });

        publisherInfo(holder.checkExpertComment, holder.profileCommentPic, holder.commentAuthorName, comment.getUserId());
    }

    private void showEditDeleteDialog(Comment comment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit or Delete Comment");
        builder.setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Edit comment
                    showEditCommentDialog(comment, position);
                    break;
                case 1:
                    // Delete comment
                    deleteComment(comment, position);
                    break;
            }
        });
        builder.show();
    }

    private void showEditCommentDialog(Comment comment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Comment");

        final EditText input = new EditText(context);
        input.setText(comment.getCommentContent());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newCommentContent = input.getText().toString();
            updateComment(comment.getId(), newCommentContent, position);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateComment(String commentId, String newContent, int position) {
        db.collection("Comments").document(commentId)
                .update("CommentContent", newContent)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CommentAdapter", "Comment updated successfully");
                    comments.get(position).setCommentContent(newContent);
                    notifyItemChanged(position); // Refresh only the updated comment
                })
                .addOnFailureListener(e -> Log.e("CommentAdapter", "Error updating comment", e));
    }

    private void deleteComment(Comment comment, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("Comments").document(comment.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("CommentAdapter", "Comment deleted successfully");
                                comments.remove(position);
                                notifyItemRemoved(position); // Refresh only the deleted comment
                            })
                            .addOnFailureListener(e -> Log.e("CommentAdapter", "Error deleting comment", e));
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileCommentPic, checkExpertComment;
        public TextView commentAuthorName, commentDate, commentDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            profileCommentPic = itemView.findViewById(R.id.profile_comment_pic);
            checkExpertComment = itemView.findViewById(R.id.checkExpertComment);
            commentAuthorName = itemView.findViewById(R.id.CommentAuthorName);
            commentDate = itemView.findViewById(R.id.commentdate);
            commentDescription = itemView.findViewById(R.id.commentDescription);
        }
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

    private void publisherInfo(final ImageView expertCheck, final ImageView image_profile, final TextView AuthorName, String userId) {
        if (userId == null || userId.isEmpty()) {
            // Handle the case where userId is null or empty
            Toast.makeText(context, "erorrrrr", Toast.LENGTH_SHORT).show();
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
                        String profilePhoto = document.getString("profile photo");
                        if (userName != null) {
                            AuthorName.setText(userName);
                        }
                        if (profilePhoto != null) {
                            Glide.with(context).load(profilePhoto).into(image_profile);
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
