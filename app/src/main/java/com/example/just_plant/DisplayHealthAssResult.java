package com.example.just_plant;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.method.LinkMovementMethod;
import android.util.Log;

public class DisplayHealthAssResult extends AppCompatActivity {

    private static final int MAX_DISEASES_DISPLAY = 9; // Limit the number of diseases displayed
    private static final String TAG = "DisplayHealthAssResult"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_health_ass_result);

        TextView healthStatus = findViewById(R.id.health_status);
        ImageView Healthy = findViewById(R.id.isHealthy);
        LinearLayout diseaseContainer = findViewById(R.id.disease_container);

        String responseString = getIntent().getStringExtra("HealthResponse");

        try {
            JSONObject response = new JSONObject(responseString);
            JSONObject result = response.optJSONObject("result");
            if (result == null) {
                Log.e(TAG, "Result object is null");
                Toast.makeText(this, "Invalid health assessment data", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject isPlant = result.optJSONObject("is_plant");
            JSONObject isHealthy = result.optJSONObject("is_healthy");

            if (isPlant == null || isHealthy == null) {
                Log.e(TAG, "is_plant or is_healthy object is null");
                Toast.makeText(this, "Invalid health assessment data", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isPlantBinary = isPlant.getBoolean("binary");
            boolean isHealthyBinary = isHealthy.getBoolean("binary");
            double healthProbability = isHealthy.getDouble("probability");
            String formattedProbability = String.format("%.6f", healthProbability);

            if (isHealthyBinary) {
                Log.d("binary: ", "health binary = " + isHealthyBinary);
                String healthy = "The plant is healthy!\n";
                SpannableStringBuilder c = new SpannableStringBuilder(healthy);
                c.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, healthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                c.setSpan(new StyleSpan(Typeface.BOLD), 0, healthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                c.setSpan(new AbsoluteSizeSpan(20, true), 0, healthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                String po = "Probability: ";

                healthStatus.setText(c);
                healthStatus.append(po);
                healthStatus.append(formattedProbability);

                Healthy.setVisibility(View.VISIBLE);
            } else {
                Log.d("binary: ", "not health binary = " + isHealthyBinary);
                String notHealthy = "The plant is not healthy!\n";
                SpannableStringBuilder c = new SpannableStringBuilder(notHealthy);
                c.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, notHealthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                c.setSpan(new StyleSpan(Typeface.BOLD), 0, notHealthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                c.setSpan(new AbsoluteSizeSpan(20, true), 0, notHealthy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                String po = "Probability: ";

                healthStatus.setText(c);
                healthStatus.append(po);
                healthStatus.append(formattedProbability);

                diseaseContainer.setVisibility(View.VISIBLE);

                JSONArray diseaseSuggestions = result.optJSONObject("disease").optJSONArray("suggestions");
                if (diseaseSuggestions == null) {
                    Log.e(TAG, "Disease suggestions array is null");
                    Toast.makeText(this, "No disease suggestions available", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i = 0; i < Math.min(diseaseSuggestions.length(), MAX_DISEASES_DISPLAY); i++) {
                    JSONObject disease = diseaseSuggestions.optJSONObject(i);
                    if (disease == null) {
                        Log.e(TAG, "Disease object at index " + i + " is null");
                        continue;
                    }

                    // Inflate the disease item template
                    View diseaseView = LayoutInflater.from(this).inflate(R.layout.disease_item_template, diseaseContainer, false);

                    TextView diseaseName = diseaseView.findViewById(R.id.disease_name);
                    TextView diseaseProbability = diseaseView.findViewById(R.id.disease_probability);
                    LinearLayout similarImagesContainer = diseaseView.findViewById(R.id.similar_images_container);
                    TextView diseaseDetails = diseaseView.findViewById(R.id.disease_details);

                    // Set disease name and probability
                    diseaseName.setText("[" + (i + 1) + "] " + disease.optString("name", "Unknown"));
                    diseaseProbability.setText("Probability: " + disease.optDouble("probability", 0.0));

                    // Set similar images
                    JSONArray similarImages = disease.optJSONArray("similar_images");
                    if (similarImages != null) {
                        for (int j = 0; j < similarImages.length(); j++) {
                            String imageUrl = similarImages.optJSONObject(j).optString("url", "");
                            if (!imageUrl.isEmpty()) {
                                ImageView imageView = new ImageView(this);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400,500
                                );
                                params.setMargins(12, 2, 12, 2);
                                imageView.setLayoutParams(params);

                                Picasso.get()
                                        .load(imageUrl)
                                        .resize(400,500)
                                        .centerCrop()
                                        .into(imageView);

                                similarImagesContainer.addView(imageView);
                            }
                        }
                    }

                    // Set disease details
                    JSONObject details = disease.optJSONObject("details");
                    if (details != null) {
                        SpannableStringBuilder detailsBuilder = new SpannableStringBuilder();

                        // Append details with consistent style
                        appendStyledTextWithNewline(detailsBuilder, "Local Name: ", details.optString("local_name"));
                        appendStyledTextWithNewline(detailsBuilder, "Description: \n", details.optString("description"));
                        appendStyledTextWithNewline(detailsBuilder, "More Info: \n", details.optString("url"), true);
                        appendStyledTextWithNewline(detailsBuilder, "Common Names: ", jsonArrayToString(details.optJSONArray("common_names")));
                        appendStyledTextWithNewline(detailsBuilder, "Classification: \n", jsonArrayToString(details.optJSONArray("classification")));

                        // Append "Suggestion Treatment:" under "Classification:"
                        appendStyledText(detailsBuilder, "Suggestion Treatment(s)", ":", Color.rgb(0, 0, 0), 16);

                        // Append treatment suggestions with specific styles
                        appendStyledTextWithNewline(detailsBuilder, "Prevention: ", jsonArrayToString(details.optJSONObject("treatment").optJSONArray("prevention")), Color.rgb(104, 155, 72), 14);
                        appendStyledTextWithNewline(detailsBuilder, "Biological: ", jsonArrayToString(details.optJSONObject("treatment").optJSONArray("biological")), Color.rgb(104, 155, 72), 14);
                        appendStyledTextWithNewline(detailsBuilder, "Chemical: ", jsonArrayToString(details.optJSONObject("treatment").optJSONArray("chemical")), Color.rgb(104, 155, 72), 14);

                        diseaseDetails.setText(detailsBuilder);
                        diseaseDetails.setMovementMethod(LinkMovementMethod.getInstance()); // Make links clickable

                        // Set padding for all fields inside the details
                        int paddingInPx = 3;
                        diseaseDetails.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

                        // Add disease view to container
                        diseaseContainer.addView(diseaseView);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse health assessment details", Toast.LENGTH_SHORT).show();
        }
    }

    private void appendStyledText(SpannableStringBuilder builder, String label, String text, int color, int textSize) {
        appendStyledText(builder, label, text, Color.rgb(0, 0, 0), 16, false);
    }

    private void appendStyledTextWithNewline(SpannableStringBuilder builder, String label, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        appendStyledText(builder, label, text + "\n", Color.rgb(0, 0, 0), 16, false);
    }

    private void appendStyledTextWithNewline(SpannableStringBuilder builder, String label, String text, boolean isUrl) {
        if (text == null || text.isEmpty()) {
            return;
        }
        appendStyledText(builder, label, text + "\n", Color.rgb(0, 0, 0), 16, isUrl);
    }

    private void appendStyledTextWithNewline(SpannableStringBuilder builder, String label, String text, int color, int textSize) {
        if (text == null || text.isEmpty()) {
            return;
        }
        appendStyledText(builder, label, text + "\n", color, textSize, false);
    }

    private void appendStyledText(SpannableStringBuilder builder, String label, String text, int color, int textSize, boolean isUrl) {
        if (text == null || text.isEmpty()) {
            return; // Skip adding the text if it is null or empty
        }

        SpannableStringBuilder styledText = new SpannableStringBuilder(label + text + "\n");
        styledText.setSpan(new ForegroundColorSpan(color), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledText.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledText.setSpan(new AbsoluteSizeSpan(textSize, true), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (isUrl) {
            int start = label.length();
            int end = start + text.length();
            styledText.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
                    startActivity(browserIntent);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        builder.append(styledText);
    }

    private String jsonArrayToString(JSONArray jsonArray) {
        if (jsonArray == null) {
            return ""; // Return empty string if jsonArray is null
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            String value = jsonArray.optString(i, "");
            if (!value.isEmpty()) {
                result.append(value);
                if (i != jsonArray.length() - 1) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }
}
