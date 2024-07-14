package com.example.just_plant;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CustomDialog extends AppCompatActivity {

    private Context context;
    private CloseDialogListener closeDialogListener;
    public CustomDialog() {
        // Default constructor logic if needed
        
    }
    public CustomDialog(Context context, CloseDialogListener closeDialogListener) {
        if (closeDialogListener == null) {
            throw new IllegalArgumentException("CloseDialogListener cannot be null");
        }
        this.context = context;
        this.closeDialogListener = closeDialogListener;
    }

    public void showDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(false);

        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (closeDialogListener != null) {
                    closeDialogListener.onClose();
                }
            }
        });

        TextView faqText = dialog.findViewById(R.id.faq_text);
        faqText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ((TextView) v).getText().toString();
                if (url.startsWith("http") || url.startsWith("https")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    public interface CloseDialogListener {
        void onClose();
    }
}
