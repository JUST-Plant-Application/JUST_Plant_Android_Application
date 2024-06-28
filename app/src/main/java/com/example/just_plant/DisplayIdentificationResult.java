package com.example.just_plant;
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
        ImageView similarImage3 = findViewById(R.id.similar_image3);

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
            if (similarImages.length() > 2) {
                Glide.with(this).load(similarImages.getJSONObject(2).getString("url")).into(similarImage3);
                similarImage3.setVisibility(View.VISIBLE);
            }

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





//        // Retrieve the JSON response from the intent
//        String responseBody = getIntent().getStringExtra("response");
//
//        // Deserialize the JSON response using Gson
//        Gson gson = new Gson();
//        PlantIdentificationResponse response = gson.fromJson(responseBody, PlantIdentificationResponse.class);
//
//
//        //////
//        //
//
//        if (response != null) {
//            // Update t1 to display name and first common name
//            String name = response.getResult().getClassification().getSuggestions().get(0).getName();
//            List<String> commonNames = response.getResult().getClassification().getSuggestions().get(0).getDetails().getCommon_names();
//            String firstCommonName = "Not available";
//            if (commonNames != null && !commonNames.isEmpty()) {
//                firstCommonName = commonNames.get(0);
//            }
//            t1.setText("Name: " + name + "\nFirst Common Name: " + firstCommonName);
//
//            // Update t2 to display description
//            String description = "Not available";
//            PlantIdentificationResponse.Description descriptionObj = response.getResult().getClassification().getSuggestions().get(0).getDetails().getDescription();
//            if (descriptionObj != null) {
//                description = descriptionObj.getValue();
//            }
//            t2.setText("Description: " + description);
//
//
//            // Update t3 to display full taxonomy
//            PlantIdentificationResponse.Taxonomy taxonomy = response.getResult().getClassification().getSuggestions().get(0).getDetails().getTaxonomy();
//            if (taxonomy != null) {
//                String fullTaxonomy = taxonomy.getKingdom() + ", " + taxonomy.getPhylum() + ", " + taxonomy.getClass_() + ", " + taxonomy.getOrder() + ", " + taxonomy.getFamily() + ", " + taxonomy.getGenus();
//                t3.setText("Full Taxonomy: " + fullTaxonomy);
//            } else {
//                t3.setText("Full Taxonomy: Not available");
//            }
//
//
//            // Update t4 to display synonyms
//            List<String> synonyms = response.getResult().getClassification().getSuggestions().get(0).getDetails().getSynonyms();
//            if (synonyms != null && !synonyms.isEmpty()) {
//                t4.setText("Synonyms: " + TextUtils.join(", ", synonyms));
//            } else {
//                t4.setText("Synonyms: Not available");
//            }
//
//            // Load similar images into img1 and img2
//            List<PlantIdentificationResponse.SimilarImage> similarImages = response.getResult().getClassification().getSuggestions().get(0).getSimilar_images();
//            if (similarImages != null && similarImages.size() >= 2) {
//                // Assuming similarImages.get(0) and similarImages.get(1) are the first two similar images
//                Picasso.get().load(similarImages.get(0).getUrl()).into(img1);
//                Picasso.get().load(similarImages.get(1).getUrl()).into(img2);
//            } else {
//                // Handle case where there are not enough similar images
//            }
//        }
//



        /*
        if (response.getResult() != null && response.getResult().getClassification() != null
                && !response.getResult().getClassification().getSuggestions().isEmpty()) {
            PlantIdentificationResponse.Result.Classification.Suggestion suggestion = response.getResult().getClassification().getSuggestions().get(0);

            // Display the name in t1
            if (suggestion.getDetails() != null) {
                String commonName = suggestion.getDetails().getCommon_names() != null && !suggestion.getDetails().getCommon_names().isEmpty()
                        ? suggestion.getDetails().getCommon_names().get(0)
                        : "No common name found";
                t1.setText(suggestion.getName() + " - " + commonName);
            }

            // Display the description in t2
            if (suggestion.getDetails() != null && suggestion.getDetails().getDescription() != null) {
                String description = suggestion.getDetails().getDescription().getValue();
                Log.d("Description", description); // Log the description
                t2.setText(description);
                Log.d("t2", "Description set to " + description); // Log that t2 has been set
            } else {
                Log.d("Description", "No description found"); // Log if description is null
                t2.setText("No description found"); // Set the text to a default message
            }

            // Display the full taxonomy in t3
            if (suggestion.getDetails() != null && suggestion.getDetails().getTaxonomy() != null) {
                PlantIdentificationResponse.Result.Classification.Suggestion.Details.Taxonomy taxonomy = suggestion.getDetails().getTaxonomy();
                String fullTaxonomy = "Class: " + taxonomy.getClass_() + "\n"
                        + "Genus: " + taxonomy.getGenus() + "\n"
                        + "Order: " + taxonomy.getOrder() + "\n"
                        + "Family: " + taxonomy.getFamily() + "\n"
                        + "Phylum: " + taxonomy.getPhylum() + "\n"
                        + "Kingdom: " + taxonomy.getKingdom();
                Log.d("FullTaxonomy", fullTaxonomy); // Log the full taxonomy
                t3.setText(fullTaxonomy);
                Log.d("t3", "Full taxonomy set to " + fullTaxonomy); // Log that t3 has been set
            } else {
                Log.d("FullTaxonomy", "No taxonomy found"); // Log if taxonomy is null
                t3.setText("No taxonomy found"); // Set the text to a default message
            }

            // Display synonyms in t4
            if (suggestion.getDetails() != null && suggestion.getDetails().getSynonyms() != null) {
                List<String> synonyms = suggestion.getDetails().getSynonyms();
                if (!synonyms.isEmpty()) {
                    StringBuilder synonymsBuilder = new StringBuilder();
                    for (String synonym : synonyms) {
                        synonymsBuilder.append(synonym).append(", ");
                    }
                    // Remove the last comma and space
                    synonymsBuilder.setLength(synonymsBuilder.length() - 2);
                    Log.d("Synonyms", synonymsBuilder.toString()); // Log the synonyms
                    t4.setText(synonymsBuilder.toString());
                    Log.d("t4", "Synonyms set to " + synonymsBuilder.toString()); // Log that t4 has been set
                } else {
                    Log.d("Synonyms", "No synonyms found"); // Log if synonyms list is empty
                    t4.setText("No synonyms found"); // Set the text to a default message
                }
            } else {
                Log.d("Synonyms", "No synonyms found"); // Log if synonyms is null
                t4.setText("No synonyms found"); // Set the text to a default message
            }

            // Display similar images in img1 and img2
            if (suggestion.getSimilar_images() != null && !suggestion.getSimilar_images().isEmpty()) {
                List<PlantIdentificationResponse.Result.Classification.Suggestion.SimilarImage> similarImages = suggestion.getSimilar_images();
                if (similarImages.size() >= 2) {
                    // Display the first two similar images in img1 and img2
                    String imageUrl1 = similarImages.get(0).getUrl();
                    String imageUrl2 = similarImages.get(1).getUrl();


                    // Load images using an image loading library like Picasso or Glide
                    // For example, using Picasso:
                    Picasso.get().load(imageUrl1).into(img1);
                    Picasso.get().load(imageUrl2).into(img2);
                }
            }
        }

        */




//////////////////////////////////////////////////////////////////////


// Assuming 't1' is TextView for Plantt Name, 't2' for Taxonomy, 't3' for Description, 't4' for URL
/*
        Intent intent = getIntent();
        String response = intent.getStringExtra("response");
        if (response == null) {
            Log.e("DisplayIdentificationResult", "No response received.");
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject result = jsonResponse.getJSONObject("result");
            JSONObject classification = result.getJSONObject("classification");
            JSONArray suggestions = classification.getJSONArray("suggestions");
            JSONObject suggestion0 = suggestions.getJSONObject(0);
            JSONObject details = suggestion0.getJSONObject("details");
            JSONArray similarImages = suggestion0.getJSONArray("similar_images");

            // Display Plantt Name
            String plantName = suggestion0.optString("name", "Name not available");
            t1.setText("Plantt Name: " + plantName);

            // Display Common Names
            if (details.has("common_names")) {
                JSONArray commonNames = details.getJSONArray("common_names");
                StringBuilder commonNamesText = new StringBuilder();
                for (int i = 0; i < commonNames.length(); i++) {
                    commonNamesText.append(commonNames.getString(i));
                    if (i < commonNames.length() - 1) {
                        commonNamesText.append(", ");
                    }
                }
                t2.setText("Common Names: " + commonNamesText.toString());
            } else {
                t2.setText("Common Names: Not available");
            }

            // Display Taxonomy Information
            if (details.has("taxonomy")) {
                JSONObject taxonomy = details.getJSONObject("taxonomy");
                String taxonomyText = getTaxonomyText(taxonomy);
                t3.setText("Taxonomy: \n" + taxonomyText);
            } else {
                t3.setText("Taxonomy: Not available");
            }

            // Display Description
            if (details.has("description")) {
                JSONObject descriptionObj = details.getJSONObject("description");
                String description = descriptionObj.optString("value", "Description unavailable");
                t4.setText("Description: " + description);
            } else {
                t4.setText("Description: Not available");
            }

            // Load and display similar images
            if (similarImages.length() > 0) {
                String imageUrl1 = similarImages.getJSONObject(0).optString("url");
                Picasso.get().load(imageUrl1).into(img1);
                if (similarImages.length() > 1) {
                    String imageUrl2 = similarImages.getJSONObject(1).optString("url");
                    Picasso.get().load(imageUrl2).into(img2);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parsing Error", "Error: ", e);
        }
    }

    private String getTaxonomyText(JSONObject taxonomy) {
        if (taxonomy != null) {
            return "Class: " + taxonomy.optString("class") + "\n" +
                    "Genus: " + taxonomy.optString("genus") + "\n" +
                    "Order: " + taxonomy.optString("order") + "\n" +
                    "Family: " + taxonomy.optString("family") + "\n" +
                    "Phylum: " + taxonomy.optString("phylum") + "\n" +
                    "Kingdom: " + taxonomy.optString("kingdom");
        } else {
            return "Taxonomy information not available";
        }



 */






