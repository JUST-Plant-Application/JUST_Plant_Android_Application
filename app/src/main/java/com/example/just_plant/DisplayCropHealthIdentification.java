package com.example.just_plant;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class DisplayCropHealthIdentification extends AppCompatActivity {

    private static final int MAX_DISEASE_SUGGESTIONS_DISPLAY = 6;
    private static final String TAG = "DisplayHealthAssResult";
    private LinearLayout cropContainer;
    private LinearLayout diseaseContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_crop_health_identification);

        cropContainer = findViewById(R.id.crop_container);
        diseaseContainer = findViewById(R.id.disease_container);

        String responseString = getIntent().getStringExtra("crop_response");

        try {
            JSONObject response = new JSONObject(responseString);
            handleResponse(response);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse crop health details", e);
            Toast.makeText(this, "Failed to parse crop health details", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResponse(JSONObject response) throws JSONException {
        JSONObject result = response.optJSONObject("result");
        if (result == null) {
            showErrorMessage("Invalid crop health data");
            return;
        }

        // Handle Crop Suggestions
        JSONArray cropSuggestions = result.optJSONObject("crop").optJSONArray("suggestions");
        if (cropSuggestions != null && cropSuggestions.length() > 0) {
            JSONObject cropSuggestion = cropSuggestions.optJSONObject(0);
            if (cropSuggestion != null) {
                addCropView(cropSuggestion);
            }
        }

        // Handle Disease Suggestions
        TextView suggestionsTitle = new TextView(this);
        suggestionsTitle.setText("\n** Crop Diseases Suggestions: **");
        suggestionsTitle.setTextSize(20);
        suggestionsTitle.setGravity(Gravity.CENTER);
        suggestionsTitle.setTextColor(Color.	rgb(205,92,92));
        suggestionsTitle.setTypeface(null, Typeface.BOLD);
        diseaseContainer.addView(suggestionsTitle);

        JSONArray diseaseSuggestions = result.optJSONObject("disease").optJSONArray("suggestions");
        if (diseaseSuggestions != null) {
            for (int i = 0; i < Math.min(diseaseSuggestions.length(), MAX_DISEASE_SUGGESTIONS_DISPLAY); i++) {
                JSONObject disease = diseaseSuggestions.optJSONObject(i);
                if (disease != null) {
                    addDiseaseView(disease, i);
                }
            }
        }
    }

    private void showErrorMessage(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void addCropView(JSONObject crop) throws JSONException {
        View cropView = LayoutInflater.from(this).inflate(R.layout.crop_item_template, cropContainer, false);

        ImageView cropImage = cropView.findViewById(R.id.crop_image);
        TextView cropName = cropView.findViewById(R.id.crop_name);
        TextView cropProbability = cropView.findViewById(R.id.crop_probability);
        TextView cropScientificName = cropView.findViewById(R.id.crop_scientific_name);
        TextView cropImagesLabel = cropView.findViewById(R.id.crop_similar_images);
        LinearLayout similarImagesContainer = cropView.findViewById(R.id.similar_images_container);
        LinearLayout cropImagesContainer = cropView.findViewById(R.id.crop_images_container);

        // Set crop details
        setTextViewText(cropName, "Crop Name: ", crop.optString("name", null), Color.RED, true);
        setTextViewText(cropProbability, "Probability: ", String.valueOf(crop.optDouble("probability", 0.0)), Color.BLACK, false);
        setTextViewText(cropScientificName, "Scientific Name: ", crop.optString("scientific_name", null), Color.BLACK, true);

        // Load image
        JSONObject details = crop.optJSONObject("details");
        if (details != null) {
            JSONObject image = details.optJSONObject("image");
            if (image != null) {
                String imageUrl = image.optString("value", "");
                if (!imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).resize(600, 600).centerCrop().into(cropImage);
                }
            }

            // Load similar images
            JSONArray similarImages = crop.optJSONArray("similar_images");
            if (similarImages != null) {
                for (int i = 0; i < similarImages.length(); i++) {
                    JSONObject similarImage = similarImages.optJSONObject(i);
                    if (similarImage != null) {
                        String url = similarImage.optString("url", "");
                        double similarity = similarImage.optDouble("similarity", 0.0);
                        addSimilarImage(similarImagesContainer, url, similarity);
                    }
                }
            }

            // Load additional images
            JSONArray images = details.optJSONArray("images");
            if (images != null) {
                for (int i = 0; i < images.length(); i++) {
                    JSONObject imagee = images.optJSONObject(i);
                    if (imagee != null) {
                        String url = imagee.optString("value", "");
                        addImage(cropImagesContainer, url);
                    }
                }
                cropImagesLabel.setText("Crop Images:");
            }
        }

        cropContainer.addView(cropView);

        // Add black line after the crop item
        View blackLine = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 8, 0, 8);
        blackLine.setLayoutParams(params);
        blackLine.setBackgroundColor(Color.BLACK);
        cropContainer.addView(blackLine);
    }


    private void addDiseaseView(JSONObject disease, int index) throws JSONException {
        View diseaseView = LayoutInflater.from(this).inflate(R.layout.crop_disease_item_template, diseaseContainer, false);

        TextView diseaseIndex = diseaseView.findViewById(R.id.disease_index);
        TextView diseaseName = diseaseView.findViewById(R.id.disease_name);
        TextView diseaseProbability = diseaseView.findViewById(R.id.disease_probability);
        TextView diseaseType = diseaseView.findViewById(R.id.disease_type);
        TextView diseaseScientificName = diseaseView.findViewById(R.id.disease_scientific_name);
        TextView diseaseCommon = diseaseView.findViewById(R.id.disease_common_names);
        TextView diseaseDesc = diseaseView.findViewById(R.id.disease_description);
        TextView showMoreD = diseaseView.findViewById(R.id.show_more1);
        TextView diseaseUrl = diseaseView.findViewById(R.id.disease_url);
        TextView diseaseTaxonomy = diseaseView.findViewById(R.id.disease_taxonomy);
        TextView diseaseSymp = diseaseView.findViewById(R.id.disease_symptoms);
        TextView diseaseSeverity = diseaseView.findViewById(R.id.disease_severity);
        TextView showMoreS = diseaseView.findViewById(R.id.show_more2);
        TextView diseaseSpreading = diseaseView.findViewById(R.id.disease_spreading);
        TextView showMoreP = diseaseView.findViewById(R.id.show_more3);
        TextView diseaseTreatment = diseaseView.findViewById(R.id.treatment_container);
        LinearLayout similarImagesContainer = diseaseView.findViewById(R.id.similar_images_container);
        LinearLayout diseaseImagesContainer = diseaseView.findViewById(R.id.disease_images_container);

        int j = index + 1;
        diseaseIndex.setText("[" + j + "] ");
        setTextViewText(diseaseName, "", disease.optString("name", "Unknown"), Color.BLACK, true);
        setTextViewText(diseaseProbability, "Probability: ", String.valueOf(disease.optDouble("probability", 0.0)), Color.BLACK, false);

        // Load similar images
        JSONArray similarImages = disease.optJSONArray("similar_images");
        if (similarImages != null) {
            for (int i = 0; i < similarImages.length(); i++) {
                JSONObject similarImage = similarImages.optJSONObject(i);
                if (similarImage != null) {
                    String url = similarImage.optString("url", "");
                    double similarity = similarImage.optDouble("similarity", 0.0);
                    addSimilarImage(similarImagesContainer, url, similarity);
                }
            }
        }

        JSONObject details = disease.optJSONObject("details");
        if (details != null) {
            // Load additional images
            JSONArray images = details.optJSONArray("images");
            if (images != null) {
                for (int i = 0; i < images.length(); i++) {
                    JSONObject imageee = images.optJSONObject(i);
                    if (imageee != null) {
                        String url = imageee.optString("value", "");
                        addImage(diseaseImagesContainer, url);
                    }
                }
            }
// Display type   disease.optString("scientific_name")
            if (disease.has("scientific_name") && !disease.isNull("scientific_name")) {
                String Sname = disease.optString("scientific_name", "");
                if (!Sname.isEmpty()) {
                    diseaseScientificName.setVisibility(View.VISIBLE);
                    String typeTitle = "Scientific name: ";
                    SpannableStringBuilder typeBuilder = new SpannableStringBuilder(typeTitle);
                    typeBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    typeBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    typeBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseScientificName.setText(typeBuilder);
                    diseaseScientificName.append(Sname);
                    diseaseScientificName.setTextSize(16);
                } else {
                    diseaseScientificName.setVisibility(View.GONE);
                }
            } else {
                diseaseScientificName.setVisibility(View.GONE);
            }

// scientific name
            if (details.has("type") && !details.isNull("type")) {
                String type = details.optString("type", "");
                if (!type.isEmpty()) {
                    diseaseType.setVisibility(View.VISIBLE);
                    String typeTitle = "Type: ";
                    SpannableStringBuilder typeBuilder = new SpannableStringBuilder(typeTitle);
                    typeBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    typeBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    typeBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, typeTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseType.setText(typeBuilder);
                    diseaseType.append(type);
                    diseaseType.setTextSize(16);
                } else {
                    diseaseType.setVisibility(View.GONE);
                }
            } else {
                diseaseType.setVisibility(View.GONE);
            }


            // Display common names
            if (details.has("common_names") && !details.isNull("common_names")) {
                JSONArray commonNamesArray = details.optJSONArray("common_names");
                if (commonNamesArray != null && commonNamesArray.length() > 0) {
                    String common = "Common Names: \n";
                    SpannableStringBuilder c = new SpannableStringBuilder(common);

                    c.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                    c.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                    c.setSpan(new AbsoluteSizeSpan(18, true), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                    diseaseCommon.setText(c);
                    diseaseCommon.setTextSize(16);

                    for (int i = 0; i < commonNamesArray.length(); i++) {
                        String name = commonNamesArray.optString(i, "null");
                        if (!"null".equals(name) && !name.isEmpty()) {
                            diseaseCommon.append(name);
                            if (i != commonNamesArray.length() - 1) {
                                diseaseCommon.append(", ");
                            } else {
                                diseaseCommon.append(". ");
                            }
                        }
                    }

                    // Hide the common names section if it's empty
                    if (diseaseCommon.length() == common.length()) {
                        diseaseCommon.setVisibility(View.GONE);
                    } else {
                        diseaseCommon.setVisibility(View.VISIBLE);
                    }
                } else {
                    diseaseCommon.setVisibility(View.GONE);
                }
            } else {
                diseaseCommon.setVisibility(View.GONE);
            }


            // Display description
            if (details.has("description") && !details.isNull("description")) {
                String description = details.optString("description", "");
                if (!description.isEmpty() && !"null".equals(description)) {
                    diseaseDesc.setVisibility(View.VISIBLE);
                    String descTitle = "Description: \n";
                    SpannableStringBuilder descBuilder = new SpannableStringBuilder(descTitle);
                    descBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, descTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    descBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, descTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    descBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, descTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseDesc.setText(descBuilder);
                    diseaseDesc.append(description);

                    diseaseDesc.setTextSize(15);

                    // Implement the show more/less functionality
                    diseaseDesc.post(() -> {
                        if (diseaseDesc.getLineCount() > 5) {
                            diseaseDesc.setMaxLines(5);
                            showMoreD.setVisibility(View.VISIBLE);
                            showMoreD.setOnClickListener(new View.OnClickListener() {
                                boolean isExpanded = false;

                                @Override
                                public void onClick(View v) {
                                    if (isExpanded) {
                                        diseaseDesc.setMaxLines(5);
                                        showMoreD.setText("Show more");
                                    } else {
                                        diseaseDesc.setMaxLines(Integer.MAX_VALUE);
                                        showMoreD.setText("Show less");
                                    }
                                    isExpanded = !isExpanded;
                                }
                            });
                        } else {
                            showMoreD.setVisibility(View.GONE);
                        }
                    });
                } else {
                    diseaseDesc.setVisibility(View.GONE);
                    showMoreD.setVisibility(View.GONE);
                }
            } else {
                diseaseDesc.setVisibility(View.GONE);
                showMoreD.setVisibility(View.GONE);
            }

// Display URL
            if (details.has("wiki_url") && !details.isNull("wiki_url")) {
                String wikiUrl = details.optString("wiki_url", "");
                if (!wikiUrl.isEmpty() && !"null".equals(wikiUrl)) {
                    diseaseUrl.setVisibility(View.VISIBLE);
                    String urlText = "\nURL for more info: \n";
                    SpannableStringBuilder urlBuilder = new SpannableStringBuilder(urlText);
                    urlBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    urlBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    urlBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseUrl.setText(urlBuilder);
                    diseaseUrl.append("\n");
                    diseaseUrl.append(wikiUrl);
                    diseaseUrl.setTextSize(16);

                    // Make the URL clickable
                    diseaseUrl.setMovementMethod(LinkMovementMethod.getInstance());
                    diseaseUrl.setText(urlBuilder.append(wikiUrl), TextView.BufferType.SPANNABLE);
                    Spannable spannable = (Spannable) diseaseUrl.getText();
                    Linkify.addLinks(spannable, Linkify.WEB_URLS);
                } else {
                    diseaseUrl.setVisibility(View.GONE);
                }
            } else {
                diseaseUrl.setVisibility(View.GONE);
            }

            //////////////////////////////////////////////////////

//////Taxonomy
// Display taxonomy
            if (details.has("taxonomy") && !details.isNull("taxonomy")) {
                JSONObject taxonomyObject = details.optJSONObject("taxonomy");
                if (taxonomyObject != null) {
                    diseaseTaxonomy.setVisibility(View.VISIBLE);
                    String taxonomyTitle = "\nTaxonomy : \n";
                    SpannableStringBuilder taxonomyBuilder = new SpannableStringBuilder(taxonomyTitle);
                    taxonomyBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    taxonomyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    taxonomyBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    String[] fields = {"class", "genus", "order", "family", "phylum", "kingdom"};
                    for (String field : fields) {
                        if (taxonomyObject.has(field) && !taxonomyObject.isNull(field)) {
                            String fieldValue = taxonomyObject.optString(field, "Unknown");

                            // Create SpannableStringBuilder for the key
                            String keyText = field.substring(0, 1).toUpperCase() + field.substring(1) + ": ";
                            SpannableStringBuilder keyBuilder = new SpannableStringBuilder(keyText);
                            keyBuilder.setSpan(new ForegroundColorSpan(Color.rgb(104, 155, 72)), 0, keyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            keyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, keyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            keyBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, keyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Create SpannableStringBuilder for the value
                            SpannableStringBuilder valueBuilder = new SpannableStringBuilder(fieldValue + "\n");
                            valueBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, fieldValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            valueBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, fieldValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Append key and value to the main builder
                            taxonomyBuilder.append(keyBuilder);
                            taxonomyBuilder.append(valueBuilder);
                        }
                    }

                    diseaseTaxonomy.setText(taxonomyBuilder);
                } else {
                    diseaseTaxonomy.setVisibility(View.GONE);
                }
            } else {
                diseaseTaxonomy.setVisibility(View.GONE);
            }

// Display symptoms
            // Display symptoms
            // Display symptoms
            // Display symptoms
            if (details.has("symptoms") && !details.isNull("symptoms")) {
                JSONObject symObject = details.optJSONObject("symptoms");
                Log.d("Symppppp", "Symptoms Object: " + symObject.toString());
                if (symObject != null && symObject.length() > 0) {
                    diseaseSymp.setVisibility(View.VISIBLE);
                    String symTitle = "Symptoms: \n";
                    SpannableStringBuilder symBuilder = new SpannableStringBuilder(symTitle);
                    symBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, symTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    symBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, symTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    symBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, symTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    Iterator<String> keys = symObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (symObject.has(key) && !symObject.isNull(key)) {
                            String symptomDescription = symObject.optString(key, "");
                            Log.d("Symppppp", "Key: " + key + ", Description: " + symptomDescription);
                            if (!symptomDescription.isEmpty()) {
                                String fieldText = " -" + key + ": " + symptomDescription + "\n";

                                // Create SpannableStringBuilder for the key
                                SpannableStringBuilder keyBuilder = new SpannableStringBuilder(key + ": ");
                                keyBuilder.setSpan(new ForegroundColorSpan(Color.rgb(104, 155, 72)), 0, key.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                keyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, key.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                keyBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, key.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                // Create SpannableStringBuilder for the value
                                SpannableStringBuilder valueBuilder = new SpannableStringBuilder(symptomDescription + "\n");
                                valueBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, symptomDescription.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                valueBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, key.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                // Append key and value to the main builder
                                symBuilder.append(keyBuilder);
                                symBuilder.append(valueBuilder);
                            }
                        }
                    }

                    diseaseSymp.setText(symBuilder);
                } else {
                    diseaseSymp.setVisibility(View.GONE);
                }
            } else {
                diseaseSymp.setVisibility(View.GONE);
            }

/////////// Severity
            if (details.has("severity") && !details.isNull("severity")) {
                String severity = details.optString("severity", "");
                if (!severity.isEmpty() && !"null".equals(severity)) {
                    diseaseSeverity.setVisibility(View.VISIBLE);
                    String sevTitle = "Severity: \n";
                    SpannableStringBuilder sevBuilder = new SpannableStringBuilder(sevTitle);
                    sevBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, sevTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sevBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sevTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sevBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, sevTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseSeverity.setText(sevBuilder);
                    diseaseSeverity.append(severity);

                    diseaseSeverity.setTextSize(15);

                    // Implement the show more/less functionality
                    diseaseSeverity.post(() -> {
                        if (diseaseSeverity.getLineCount() > 5) {
                            diseaseSeverity.setMaxLines(5);
                            showMoreS.setVisibility(View.VISIBLE);
                            showMoreS.setOnClickListener(new View.OnClickListener() {
                                boolean isExpanded = false;

                                @Override
                                public void onClick(View v) {
                                    if (isExpanded) {
                                        diseaseSeverity.setMaxLines(5);
                                        showMoreS.setText("Show more");
                                    } else {
                                        diseaseSeverity.setMaxLines(Integer.MAX_VALUE);
                                        showMoreS.setText("Show less");
                                    }
                                    isExpanded = !isExpanded;
                                }
                            });
                        } else {
                            showMoreS.setVisibility(View.GONE);
                        }
                    });
                } else {
                    diseaseSeverity.setVisibility(View.GONE);
                    showMoreS.setVisibility(View.GONE);
                }
            } else {
                diseaseSeverity.setVisibility(View.GONE);
                showMoreS.setVisibility(View.GONE);
            }

//////// display spreading


            if (details.has("spreading") && !details.isNull("spreading")) {
                String spreading = details.optString("spreading", "");
                if (!spreading.isEmpty() && !"null".equals(spreading)) {
                    diseaseSpreading.setVisibility(View.VISIBLE);
                    String spTitle = "\nSpreading: \n";
                    SpannableStringBuilder spBuilder = new SpannableStringBuilder(spTitle);
                    spBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, spTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, spTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    diseaseSpreading.setText(spBuilder);
                    diseaseSpreading.append(spreading);

                    diseaseSpreading.setTextSize(15);

                    // Implement the show more/less functionality
                    diseaseSpreading.post(() -> {
                        if (diseaseSpreading.getLineCount() > 5) {
                            diseaseSpreading.setMaxLines(5);
                            showMoreP.setVisibility(View.VISIBLE);
                            showMoreP.setOnClickListener(new View.OnClickListener() {
                                boolean isExpanded = false;

                                @Override
                                public void onClick(View v) {
                                    if (isExpanded) {
                                        diseaseSpreading.setMaxLines(5);
                                        showMoreP.setText("Show more");
                                    } else {
                                        diseaseSpreading.setMaxLines(Integer.MAX_VALUE);
                                        showMoreP.setText("Show less");
                                    }
                                    isExpanded = !isExpanded;
                                }
                            });
                        } else {
                            showMoreP.setVisibility(View.GONE);
                        }
                    });
                } else {
                    diseaseSpreading.setVisibility(View.GONE);
                    showMoreP.setVisibility(View.GONE);
                }
            } else {
                diseaseSpreading.setVisibility(View.GONE);
                showMoreP.setVisibility(View.GONE);
            }


////////////////// display treatment


// Display treatment
            // Display treatment
            if (details.has("treatment") && !details.isNull("treatment")) {
                JSONObject treatmentObject = details.optJSONObject("treatment");
                if (treatmentObject != null) {
                    diseaseTreatment.setVisibility(View.VISIBLE);
                    String treatmentTitle = "\nTreatment: \n";
                    SpannableStringBuilder treatmentBuilder = new SpannableStringBuilder(treatmentTitle);
                    treatmentBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, treatmentTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    treatmentBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, treatmentTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    treatmentBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, treatmentTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    Iterator<String> keys = treatmentObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (treatmentObject.has(key) && !treatmentObject.isNull(key)) {
                            JSONArray treatmentsArray = treatmentObject.optJSONArray(key);
                            if (treatmentsArray != null && treatmentsArray.length() > 0) {
                                String keyTitle = key.substring(0, 1).toUpperCase() + key.substring(1) + ":\n";
                                SpannableStringBuilder keyBuilder = new SpannableStringBuilder(keyTitle);
                                keyBuilder.setSpan(new ForegroundColorSpan(Color.rgb(205, 92, 92)), 0, keyTitle.indexOf(":") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                keyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, keyTitle.indexOf(":") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                keyBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, keyTitle.indexOf(":") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                keyBuilder.setSpan(new UnderlineSpan(), 0, keyTitle.indexOf(":") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                treatmentBuilder.append(keyBuilder);

                                for (int i = 0; i < treatmentsArray.length(); i++) {
                                    String treatmentPoint = "- " + treatmentsArray.optString(i, "") + "\n";
                                    SpannableStringBuilder treatmentPointBuilder = new SpannableStringBuilder(treatmentPoint);
                                    int indexx = treatmentPoint.indexOf(":");
                                    if (indexx > -1) {
                                        treatmentPointBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, indexx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        treatmentPointBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, indexx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                    treatmentPointBuilder.setSpan(new AbsoluteSizeSpan(16, true), 0, treatmentPoint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    treatmentBuilder.append(treatmentPointBuilder);
                                }
                                treatmentBuilder.append("\n");
                            }
                        }
                    }

                    diseaseTreatment.setText(treatmentBuilder);
                } else {
                    diseaseTreatment.setVisibility(View.GONE);
                }
            } else {
                diseaseTreatment.setVisibility(View.GONE);
            }


            // You can continue with other details similarly
        }
// Add black line after the disease item
        View blackLine = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 8, 0, 8);
        blackLine.setLayoutParams(params);
        blackLine.setBackgroundColor(Color.rgb(211,211,211));
        diseaseContainer.addView(blackLine);
        diseaseContainer.addView(diseaseView);
    }

    private void addImage(LinearLayout container, String url) {
        if (!url.isEmpty()) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400,500
            );
            params.setMargins(12, 2, 12, 2);
            imageView.setLayoutParams(params);
            Picasso.get()
                    .load(url)
                    .resize(params.width, params.height)
                    .centerCrop()
                    .into(imageView);
            container.addView(imageView);
        }
    }

    private void addSimilarImage(LinearLayout container, String url, double similarity) {
        if (!url.isEmpty()) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            400,500);
            params.setMargins(14, 2, 14, 2);
            imageView.setLayoutParams(params);
            Picasso.get()
                    .load(url)
                    .resize(params.width, params.height)
                    .centerCrop()
                    .into(imageView);
            item.addView(imageView);

            TextView similarityView = new TextView(this);
            similarityView.setText("Similarity: " + similarity);
            similarityView.setTextSize(12);
            similarityView.setGravity(Gravity.CENTER);
            similarityView.setPadding(0, 0, 0, 6);
            item.addView(similarityView);

            container.addView(item);
        }
    }

    private void setTextViewText(TextView textView, String label, String text, int color, boolean isBold) {
        if (text == null || "null".equals(text)) {
            textView.setVisibility(View.GONE);
        } else {
            SpannableStringBuilder builder = new SpannableStringBuilder(label + text);
            if (isBold) {
                builder.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            builder.setSpan(new ForegroundColorSpan(color), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(builder);
        }
    }
}
