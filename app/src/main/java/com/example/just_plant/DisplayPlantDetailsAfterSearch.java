package com.example.just_plant;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DisplayPlantDetailsAfterSearch extends AppCompatActivity {
    ImageView addToGarden;
    FirebaseAuth auth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_plant_details_after_search);
        ImageView plantImage = findViewById(R.id.plant_image);
        TextView plantName = findViewById(R.id.plant_name);
        TextView commonNames = findViewById(R.id.plant_common_names);
        TextView rank = findViewById(R.id.rank);
        TextView description = findViewById(R.id.description);
        TextView showMoreDescription = findViewById(R.id.show_more);
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
        ImageView similarImage4 =findViewById(R.id.similar_image4);

        ////////////////////
        addToGarden = findViewById(R.id.AddToGarden_Btn_search);
        auth = FirebaseAuth.getInstance();
        String responseString = getIntent().getStringExtra("response");
        try {
            JSONObject response = new JSONObject(responseString);
            // Display main image
            if (response.has("image") && response.getJSONObject("image").has("value")) {
                String imageUrl = response.getJSONObject("image").getString("value");
                Picasso.get().load(imageUrl).into(plantImage);
            } else {
                Log.e("JSON Parsing", "Image URL not found");
            }
            // Display plant name
            if (response.has("name")) {
                plantName.setText("Plant Name: " + response.getString("name"));
            } else {
                Log.e("JSON Parsing", "Plant name not found");
            }
            // Display rank
            if (response.has("rank")) {
                rank.setText("Rank: " + response.getString("rank"));
            } else {
                Log.e("JSON Parsing", "Rank not found");
            }
            // Display common names
            if (response.has("common_names")) {
                JSONArray commonNamesArray = response.getJSONArray("common_names");
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
            } else {
                Log.e("JSON Parsing", "Common names not found");
            }
            // Display description
            if (response.has("description") && response.getJSONObject("description").has("value")) {
                String desc = "Description: \n";
                SpannableStringBuilder d = new SpannableStringBuilder(desc);
                d.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                d.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                d.setSpan(new AbsoluteSizeSpan(18, true), 0, desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                description.setText(d);
                description.append(response.getJSONObject("description").getString("value"));
                // Implement show more/less functionality
                description.post(() -> {
                    if (description.getLineCount() > 9) {
                        showMoreDescription.setVisibility(View.VISIBLE);
                        showMoreDescription.setOnClickListener(new View.OnClickListener() {
                            boolean isExpanded = false;
                            @Override
                            public void onClick(View v) {
                                if (isExpanded) {
                                    description.setMaxLines(9);
                                    showMoreDescription.setText("Show more");
                                } else {
                                    description.setMaxLines(Integer.MAX_VALUE);
                                    showMoreDescription.setText("Show less");
                                }
                                isExpanded = !isExpanded;
                            }
                        });
                    } else {
                        showMoreDescription.setVisibility(View.GONE);
                    }
                });
            } else {
                Log.e("JSON Parsing", "Description not found");
            }
            // Display URL
            if (response.has("url")) {
                String urlText = "URL for more info: ";
                SpannableStringBuilder ssb = new SpannableStringBuilder(urlText);
                ssb.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                ssb.setSpan(new AbsoluteSizeSpan(18, true), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                url.setText(ssb);
                url.append("\n");
                url.append(response.getString("url"));
            } else {
                Log.e("JSON Parsing", "URL not found");
            }
            // Display taxonomy
            if (response.has("taxonomy")) {
                JSONObject taxonomyObject = response.getJSONObject("taxonomy");
                String taxonomyTitle = "Taxonomy : \n";
                SpannableStringBuilder taxonomyBuilder = new SpannableStringBuilder(taxonomyTitle);
                taxonomyBuilder.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                taxonomyBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                taxonomyBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, taxonomyTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                String[] fields = {"class", "genus", "order", "family", "phylum", "kingdom"};
                for (String field : fields) {
                    if (taxonomyObject.has(field)) {
                        String fieldText = field.substring(0, 1).toUpperCase() + field.substring(1) + ": " + taxonomyObject.getString(field) + "\n";
                        SpannableStringBuilder fieldBuilder = new SpannableStringBuilder(fieldText);
                        fieldBuilder.setSpan(new ForegroundColorSpan(Color.rgb(104, 155, 72)), 0, field.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                        fieldBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, field.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                        taxonomyBuilder.append(fieldBuilder);
                    } else {
                        Log.e("JSON Parsing", "Taxonomy field " + field + " not found");
                    }
                }
                taxonomy.setText(taxonomyBuilder);
            } else {
                Log.e("JSON Parsing", "Taxonomy not found");
            }
            // Display synonyms
            if (response.has("synonyms")) {
                JSONArray synonymsArray = response.getJSONArray("synonyms");
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
                // Implement show more/less functionality for synonyms
                synonyms.post(() -> {
                    if (synonyms.getLineCount() > 4) {
                        showMoreSynonyms.setVisibility(View.VISIBLE);
                        showMoreSynonyms.setOnClickListener(new View.OnClickListener() {
                            boolean isExpanded = false;
                            @Override
                            public void onClick(View v) {
                                if (isExpanded) {
                                    synonyms.setMaxLines(3);
                                    showMoreSynonyms.setText("Show more");
                                } else {
                                    synonyms.setMaxLines(Integer.MAX_VALUE);
                                    showMoreSynonyms.setText("Show less");
                                }
                                isExpanded = !isExpanded;
                            }
                        });
                    } else {
                        showMoreSynonyms.setVisibility(View.GONE);
                    }
                });
            } else {
                Log.e("JSON Parsing", "Synonyms not found");
            }
            // Display edible parts if not null
            if (!response.isNull("edible_parts")) {
                String ediblePartsText = "Edible parts: \n";
                SpannableStringBuilder edibleBuilder = new SpannableStringBuilder(ediblePartsText);
                edibleBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                edibleBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                edibleBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, ediblePartsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                edibleBuilder.append(response.getString("edible_parts"));
                edibleParts.setText(edibleBuilder);
                edibleParts.setVisibility(View.VISIBLE);
            } else {
                Log.e("JSON Parsing", "Edible parts not found");
            }
            // Display watering if not null
            if (!response.isNull("watering")) {
                String wateringText = "Watering: \n";
                SpannableStringBuilder wateringBuilder = new SpannableStringBuilder(wateringText);
                wateringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                wateringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                wateringBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, wateringText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                wateringBuilder.append(response.getString("watering"));
                watering.setText(wateringBuilder);
                watering.setVisibility(View.VISIBLE);
            } else {
                Log.e("JSON Parsing", "Watering not found");
            }
            // Display propagation methods if not null
            if (!response.isNull("propagation_methods")) {
                String propagationMethodsText = "Propagation methods: \n";
                SpannableStringBuilder propagationBuilder = new SpannableStringBuilder(propagationMethodsText);
                propagationBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Color
                propagationBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold
                propagationBuilder.setSpan(new AbsoluteSizeSpan(18, true), 0, propagationMethodsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Size
                propagationBuilder.append(response.getString("propagation_methods"));
                propagationMethods.setText(propagationBuilder);
                propagationMethods.setVisibility(View.VISIBLE);
            } else {
                Log.e("JSON Parsing", "Propagation methods not found");
            }
            // Display similar images
            if (response.has("images")) {
                JSONArray imagesArray = response.getJSONArray("images");
                if (imagesArray.length() > 0) {
                    similarLabel.setVisibility(View.VISIBLE);
                    similarImagesScrollview.setVisibility(View.VISIBLE);
                    Picasso.get().load(imagesArray.getJSONObject(0).getString("value")).into(similarImage1);
                    similarImage1.setVisibility(View.VISIBLE);
                }
                if (imagesArray.length() > 1) {
                    Picasso.get().load(imagesArray.getJSONObject(1).getString("value")).into(similarImage2);
                    similarImage2.setVisibility(View.VISIBLE);
                }
                if (imagesArray.length() > 2) {
                    Picasso.get().load(imagesArray.getJSONObject(2).getString("value")).into(similarImage3);
                    similarImage3.setVisibility(View.VISIBLE);
                }
                if (imagesArray.length() > 3) {
                    Picasso.get().load(imagesArray.getJSONObject(4).getString("value")).into(similarImage4);
                    similarImage4.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e("JSON Parsing", "Similar images not found");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse plant details", Toast.LENGTH_SHORT).show();
        }
        ///////////////////////////
        addToGarden.setOnClickListener(v -> {
            try {
                JSONObject response = new JSONObject(responseString);
                String imageUrl = response.getJSONObject("image").getString("value");
                JSONArray commonNamesArray = response.getJSONArray("common_names");
                String firstCommonName= commonNamesArray.getString(0);
                String plant_name = response.getString("name");
                addPlantToGarden(plant_name, imageUrl, firstCommonName);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(DisplayPlantDetailsAfterSearch.this, "Error retrieving scientific name", Toast.LENGTH_SHORT).show();
            }
        });
    }
///////////////////////

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
