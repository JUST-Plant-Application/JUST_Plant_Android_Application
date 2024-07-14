package com.example.just_plant;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.just_plant.Adapter.SuggestionsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/*
public class DisplayIdentificationResult extends AppCompatActivity {

    TextView t1;
    TextView t2;
    TextView t3;
    TextView t4;
    ImageView img1;
    ImageView img2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_identification_result);

        t1 = findViewById(R.id.textView1);
        t2 = findViewById(R.id.textView2);
        t3 = findViewById(R.id.textView3);
        t4 = findViewById(R.id.textView4);
        img1 = findViewById(R.id.imageView1);
        img2 = findViewById(R.id.imageView2);

        String responseString = getIntent().getStringExtra("response");

        try {
            JSONObject response = new JSONObject(responseString);
            displayIdentificationResult(response);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayIdentificationResult(JSONObject response) throws JSONException {



        JSONObject result = response.getJSONObject("result");
        JSONArray suggestions = result.getJSONObject("classification").getJSONArray("suggestions");
        JSONObject firstSuggestion = suggestions.getJSONObject(0);

        String name = firstSuggestion.getString("name");

        String commonName = "N/A";
        JSONObject details = firstSuggestion.getJSONObject("details");
        if (details.has("common_names") && !details.isNull("common_names")) {
            commonName = details.getJSONArray("common_names").optString(0, "N/A");
        }

        String description = "No description available";
        if (details.has("description") && !details.isNull("description")) {
            description = details.getJSONObject("description").optString("value", "No description available");
        }

        JSONObject taxonomy = details.optJSONObject("taxonomy");
        String taxonomyText = buildTaxonomyText(taxonomy);

        JSONArray synonyms = details.optJSONArray("synonyms");
        String synonymsText = buildSynonymsText(synonyms);

        t1.setText("Name: " + name + "\nCommon Name: " + commonName);
        t2.setText("Description: " + description);
        t3.setText(taxonomyText);
        t4.setText(synonymsText);

        loadSimilarImages(firstSuggestion.optJSONArray("similar_images"));
    }

    private String buildTaxonomyText(JSONObject taxonomy) {
        if (taxonomy == null) {
            return "Taxonomy: Not available";
        }
        return "Taxonomy:\n" +
                "Class: " + taxonomy.optString("class", "N/A") + "\n" +
                "Genus: " + taxonomy.optString("genus", "N/A") + "\n" +
                "Order: " + taxonomy.optString("order", "N/A") + "\n" +
                "Family: " + taxonomy.optString("family", "N/A") + "\n" +
                "Phylum: " + taxonomy.optString("phylum", "N/A") + "\n" +
                "Kingdom: " + taxonomy.optString("kingdom", "N/A");
    }

    private String buildSynonymsText(JSONArray synonyms) {
        if (synonyms == null || synonyms.length() == 0) {
            return "Synonyms: No synonyms available";
        }
        StringBuilder synonymsText = new StringBuilder("Synonyms:\n");
        for (int i = 0; i < synonyms.length(); i++) {
            synonymsText.append(synonyms.optString(i, "N/A")).append("\n");
        }
        return synonymsText.toString();
    }

    private void loadSimilarImages(JSONArray similarImages) {
        if (similarImages == null || similarImages.length() == 0) {
            return;
        }
        try {
            if (similarImages.length() > 0) {
                String imageUrl1 = similarImages.getJSONObject(0).optString("url", "");
                if (!imageUrl1.isEmpty()) {
                    Picasso.get().load(imageUrl1).into(img1);
                }
            }
            if (similarImages.length() > 1) {
                String imageUrl2 = similarImages.getJSONObject(1).optString("url", "");
                if (!imageUrl2.isEmpty()) {
                    Picasso.get().load(imageUrl2).into(img2);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


*/


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DisplayIdentificationResult extends AppCompatActivity {

    ImageView addToGarden;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_identification_result);

        RecyclerView recyclerViewSuggestions = findViewById(R.id.recycler_view_suggestions);
        addToGarden = findViewById(R.id.AddToGarden_Btn_identify);

        auth = FirebaseAuth.getInstance();

        ImageView mainImage = findViewById(R.id.main_image);
        TextView name = findViewById(R.id.name);
        TextView prob = findViewById(R.id.probability);
        TextView commonNames = findViewById(R.id.common_names);
        TextView description = findViewById(R.id.description);
        TextView showMore = findViewById(R.id.show_more);
        TextView url = findViewById(R.id.url);
        TextView taxonomy = findViewById(R.id.taxonomy);
        TextView synonyms = findViewById(R.id.synonyms);
        TextView showMoreSynonyms = findViewById(R.id.show_more_synonyms);
        TextView edibleParts = findViewById(R.id.edible_parts);
        TextView watering = findViewById(R.id.watering);
        TextView propagationMethods = findViewById(R.id.propagation_methods);
        TextView similarLabel = findViewById(R.id.similar);
        HorizontalScrollView similarImagesScrollview = findViewById(R.id.similar_images_scrollview);
        LinearLayout similarImagesContainer = findViewById(R.id.similar_images_container);
        ImageView similarImage1 = findViewById(R.id.similar_image1);
        ImageView similarImage2 = findViewById(R.id.similar_image2);
       // ImageView similarImage3 = findViewById(R.id.similar_image3);

        ImageView back= findViewById(R.id.backTohome_plant);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayIdentificationResult.this, TheMainActivity.class);
                startActivity(intent);
            }
        });



        String responseString = getIntent().getStringExtra("response");

        try {
            JSONObject response = new JSONObject(responseString);
            JSONObject result = response.getJSONObject("result");
            JSONObject is_plant = result.getJSONObject("is_plant");

            JSONObject classification = result.getJSONObject("classification");
            JSONArray suggestions = classification.getJSONArray("suggestions");
            JSONObject firstSuggestion = suggestions.getJSONObject(0);
            JSONObject details = firstSuggestion.getJSONObject("details");

            SuggestionsAdapter adapter = new SuggestionsAdapter(this, suggestions);
            recyclerViewSuggestions.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerViewSuggestions.setAdapter(adapter);

            // Display main image
            String imageUrl = details.getJSONObject("image").getString("value");
            Glide.with(this).load(imageUrl).into(mainImage);

            // Display name
            String plantName = firstSuggestion.getString("name");
            name.setText("Plant Name: " + plantName);
            prob.setText("Probability = " + firstSuggestion.getString("probability"));

            // Display common names
            JSONArray commonNamesArray = details.getJSONArray("common_names");
            String common = "Common Names: \n";
            SpannableStringBuilder c = new SpannableStringBuilder(common);

            c.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
            c.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
            c.setSpan(new AbsoluteSizeSpan(18, true), 0, common.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
            commonNames.setText(c);
            for (int i = 0; i < commonNamesArray.length(); i++) {
                commonNames.append(commonNamesArray.getString(i));
                if (i != commonNamesArray.length() - 1) {
                    commonNames.append(", ");
                } else {
                    commonNames.append(". ");
                }
            }

            // Display description
            String desc = "Description: \n";
            SpannableStringBuilder d = new SpannableStringBuilder(desc);

            d.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
            d.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
            d.setSpan(new AbsoluteSizeSpan(18, true), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
            description.setText(d);
            description.append(details.getJSONObject("description").getString("value"));

            // Implement the show more/less functionality
            description.post(new Runnable() {
                @Override
                public void run() {
                    if (description.getLineCount() > 9) {
                        showMore.setVisibility(View.VISIBLE);
                        showMore.setOnClickListener(new View.OnClickListener() {
                            boolean isExpanded = false;

                            @Override
                            public void onClick(View v) {
                                if (isExpanded) {
                                    description.setMaxLines(9);
                                    showMore.setText("Show more");
                                } else {
                                    description.setMaxLines(Integer.MAX_VALUE);
                                    showMore.setText("Show less");
                                }
                                isExpanded = !isExpanded;
                            }
                        });
                    } else {
                        showMore.setVisibility(View.GONE);
                    }
                }
            });

            // Display URL
            String urlText = "URL for more info: ";
            SpannableStringBuilder ssb = new SpannableStringBuilder(urlText);
            ssb.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
            ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
            ssb.setSpan(new AbsoluteSizeSpan(18, true), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
            url.setText(ssb);
            url.append("\n");
            url.append(details.getString("url"));

            // Display taxonomy
            JSONObject taxonomyObject = details.getJSONObject("taxonomy");
            String taxonomyTitle = "Taxonomy : \n";
            SpannableStringBuilder taxonomyBuilder = new SpannableStringBuilder(taxonomyTitle);
            taxonomyBuilder.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
            taxonomyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
            taxonomyBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size

            String[] fields = {"Class", "Genus", "Order", "Family", "Phylum", "Kingdom"};
            for (String field : fields) {
                String fieldText = field + ": " + taxonomyObject.getString(field.toLowerCase()) + "\n";
                SpannableStringBuilder fieldBuilder = new SpannableStringBuilder(fieldText);
                fieldBuilder.setSpan(new ForegroundColorSpan(Color.rgb(104, 155, 72)), 0, field.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                fieldBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, field.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                taxonomyBuilder.append(fieldBuilder);
            }

            taxonomy.setText(taxonomyBuilder);

            // Display synonyms
            JSONArray synonymsArray = details.getJSONArray("synonyms");
            String synTitle = "Synonyms: \n";
            SpannableStringBuilder synBuilder = new SpannableStringBuilder(synTitle);
            synBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, synTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
            synBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, synTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
            synBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, synTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size

            for (int i = 0; i < synonymsArray.length(); i++) {
                synBuilder.append(synonymsArray.getString(i));
                if (i != synonymsArray.length() - 1) {
                    synBuilder.append(", ");
                }
            }
            synonyms.setText(synBuilder);

            // Implement the show more/less functionality for synonyms
            synonyms.post(new Runnable() {
                @Override
                public void run() {
                    if (synonyms.getLineCount() > 4) {  // Adjust the number of lines for show more/less as needed
                        showMoreSynonyms.setVisibility(View.VISIBLE);
                        showMoreSynonyms.setOnClickListener(new View.OnClickListener() {
                            boolean isExpanded = false;

                            @Override
                            public void onClick(View v) {
                                if (isExpanded) {
                                    synonyms.setMaxLines(3);  // Show only 3 lines initially
                                    showMoreSynonyms.setText("Show more");
                                } else {
                                    synonyms.setMaxLines(Integer.MAX_VALUE);  // Expand to full text
                                    showMoreSynonyms.setText("Show less");
                                }
                                isExpanded = !isExpanded;
                            }
                        });
                    } else {
                        showMoreSynonyms.setVisibility(View.GONE);
                    }
                }
            });

            // Display edible parts if not null
            if (!details.isNull("edible_parts")) {
                LinearLayout edible = findViewById(R.id.edible);
                edible.setVisibility(View.VISIBLE);
                String ediblePartsText = "Edible parts: \n";
                SpannableStringBuilder edibleBuilder = new SpannableStringBuilder(ediblePartsText);
                edibleBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                edibleBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                edibleBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size

                edibleBuilder.append(details.getString("edible_parts"));
                edibleParts.setText(edibleBuilder);
                edibleParts.setVisibility(View.VISIBLE);
            }

            // Display watering if not null
            if (!details.isNull("watering")) {
                LinearLayout water = findViewById(R.id.water);
                water.setVisibility(View.VISIBLE);
                String wateringText = "Watering: \n";
                SpannableStringBuilder wateringBuilder = new SpannableStringBuilder(wateringText);
                wateringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                wateringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                wateringBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size

                wateringBuilder.append(details.getString("watering"));
                watering.setText(wateringBuilder);
                watering.setVisibility(View.VISIBLE);
            }

            // Display propagation methods if not null
            if (!details.isNull("propagation_methods")) {
                LinearLayout propagation = findViewById(R.id.prop);
                propagation.setVisibility(View.VISIBLE);

                String propagationMethodsText = "Propagation methods: \n";
                SpannableStringBuilder propagationBuilder = new SpannableStringBuilder(propagationMethodsText);
                propagationBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                propagationBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                propagationBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size

                propagationBuilder.append(details.getString("propagation_methods"));
                propagationMethods.setText(propagationBuilder);
                propagationMethods.setVisibility(View.VISIBLE);
            }

            // Display similar images
            JSONArray similarImages = firstSuggestion.getJSONArray("similar_images");
            if (similarImages.length() > 0) {
                similarLabel.setVisibility(View.VISIBLE);
                similarImagesScrollview.setVisibility(View.VISIBLE);
                Glide.with(this).load(similarImages.getJSONObject(0).getString("url")).into(similarImage1);
                similarImage1.setVisibility(View.VISIBLE);
            }
            if (similarImages.length() > 1) {
                Glide.with(this).load(similarImages.getJSONObject(1).getString("url")).into(similarImage2);
                similarImage2.setVisibility(View.VISIBLE);
            }
//            if (similarImages.length() > 2) {
//                Glide.with(this).load(similarImages.getJSONObject(2).getString("url")).into(similarImage3);
//                similarImage3.setVisibility(View.VISIBLE);
//            }

            // Handle add to garden button click
            addToGarden.setOnClickListener(v -> {
                try {
                    String firstCommonName = ""; // Initialize with a default value or handle absence
                    if (details.has("common_names") && !details.isNull("common_names")) {
                        JSONArray names = details.getJSONArray("common_names");
                        firstCommonName = names.getString(0);
                    } else {
                        // Handle case where scientific_name is missing or null
                        // Example: Log an error or provide a default value
                        Log.e("DisplayIdentificationResult", "Scientific name not found or null");
                    }

                    addPlantToGarden(plantName, imageUrl, firstCommonName);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DisplayIdentificationResult.this, "Error retrieving scientific name", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPlantToGarden(String plantName, String imageUrl, String scientificName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create plant data
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("name", plantName);
        plantData.put("image", imageUrl);
        plantData.put("scientificName", scientificName);

        // Add plant data to garden_plants collection
        CollectionReference gardenPlantsRef = db.collection("garden_plants");
        gardenPlantsRef.add(plantData).addOnSuccessListener(documentReference -> {
            String plantId = documentReference.getId();
            addUserGardenPlant(plantId);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to add plant to garden", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error adding plant to garden_plants", e);
        });
    }

    private void addUserGardenPlant(String plantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assume you have a way to get the current user's ID
        String userId = auth.getCurrentUser().getUid();// Replace with actual user ID retrieval method

        // Get reference to the user's garden
        DocumentReference userGardenRef = db.collection("user_garden").document(userId);

        // Add plant ID to the user's garden
        userGardenRef.update("plant_ids", FieldValue.arrayUnion(plantId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Plant added to your garden", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add plant to your garden", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding plant ID to user_garden", e);
                });
    }
}


