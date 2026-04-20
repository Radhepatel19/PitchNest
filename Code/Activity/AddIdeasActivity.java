package com.example.businessidea.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.businessidea.BaseActivity;
import com.example.businessidea.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddIdeasActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userIdeasRef;
    private DatabaseReference publicIdeasRef;
    private String userId;

    private ImageView iv_close, iv_profile, iv_post_image_preview, btn_add_image, iv_cancel_image;
    private TextView tv_user_name;
    private EditText et_post_content;
    private Button btn_post;
    private FrameLayout iv_post_image_preview_frame;

    private Uri imageUri;
    private Cloudinary cloudinary;
    private boolean isImageAdded = false;
    private AutoCompleteTextView typeDropdown;
    private String[] ideaTypes = {"Startup", "Technology","Finance","Health","Education", "E-commerce", "Freelance Service", "Side Hustle"};

    private String profilePicUrl;
    private String userName;
    private String userHeadline;

    LinearLayout LinearAdd;
    private static final int PICK_IMAGE_REQUEST = 850;
    ProgressBar ProgressBar;
    String selectedType;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ideas);

        // Set status bar color
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0); // Default light icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.back)); // Black status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.back)); // Black navigation bar
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Dark icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // White status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white)); // Black navigation bar
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        assert firebaseUser != null;
        userId = firebaseUser.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userIdeasRef = database.getReference("Users").child(userId).child("Ideas");
        publicIdeasRef = database.getReference("PublicIdeas");
        ProgressBar = findViewById(R.id.ProgressBar);
        LinearAdd = findViewById(R.id.LinearAdd);

        ImageView ideaGenerationLayout = findViewById(R.id.ll_idea_generation);
        ideaGenerationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddIdeasActivity.this, AiActivity.class);
                startActivity(intent);
            }
        });

        // Initialize views
        iv_post_image_preview_frame = findViewById(R.id.iv_post_image_preview_frame);
        iv_close = findViewById(R.id.iv_close);
        iv_profile = findViewById(R.id.Image_profile);
        iv_post_image_preview = findViewById(R.id.iv_post_image_preview);
        btn_add_image = findViewById(R.id.btn_add_image);
        iv_cancel_image = findViewById(R.id.iv_cancel_image);
        tv_user_name = findViewById(R.id.tv_user_name);
        typeDropdown = findViewById(R.id.typeDropdown);
        et_post_content = findViewById(R.id.et_post_content);
        btn_post = findViewById(R.id.post_btn);

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "Your Cloud Name",
                "api_key", "your api key",
                "api_secret", "your api secret"
        ));
        LinearAdd.setVisibility(View.VISIBLE);
        // Load user data
        loadUserData();
        String ideaDescription = getIntent().getStringExtra("ideaDescription");
        String  ideaId = getIntent().getStringExtra("ideaId"); // Optional, if you need the idea ID for saving

        // Populate the EditText with the current description
        if (ideaDescription != null && ideaId != null) {
            // Populate the EditText with the current description
            et_post_content.setText(ideaDescription);
            btn_add_image.setVisibility(View.GONE);
            typeDropdown.setVisibility(View.GONE);

            // Handle post button click for updating existing idea
            btn_post.setOnClickListener(v -> {
                String updatedDescription = et_post_content.getText().toString().trim();

                if (!updatedDescription.isEmpty()) {
                    // Update in "PublicIdeas" node
                    DatabaseReference ideasRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                            .child(ideaId).child("content");

                    // Update in the user's "Ideas" node
                    DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(userId).child("Ideas").child(ideaId).child("content");

                    ideasRef.setValue(updatedDescription).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userIdeasRef.setValue(updatedDescription).addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    startActivity(new Intent(AddIdeasActivity.this,HomeActivity.class));
                                } else {
                                    Toast.makeText(AddIdeasActivity.this, "Failed to update user idea.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(AddIdeasActivity.this, "Failed to update public idea.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AddIdeasActivity.this, "Description cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btn_add_image.setVisibility(View.VISIBLE);
            typeDropdown.setVisibility(View.VISIBLE);

            // Handle post button click for adding a new idea
            btn_post.setOnClickListener(v -> {
                if (isImageAdded) {
                    String ideaText = et_post_content.getText().toString().trim();
                    if (ideaText.isEmpty()) {
                        et_post_content.setError("Idea cannot be empty");
                        et_post_content.requestFocus();
                        LinearAdd.setVisibility(View.VISIBLE);
                        ProgressBar.setVisibility(View.GONE);
                        return;
                    }
                    LinearAdd.setVisibility(View.GONE);
                    ProgressBar.setVisibility(View.VISIBLE);

                    uploadImageToCloudinary(String.valueOf(imageUri));

                } else {
                    LinearAdd.setVisibility(View.GONE);
                    ProgressBar.setVisibility(View.VISIBLE);
                    saveIdeaToFirebase(null);
                }
            });

            // Add image button click
            btn_add_image.setOnClickListener(v -> openGallery());

            // Cancel image preview
            iv_cancel_image.setOnClickListener(v -> clearImagePreview());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ideaTypes);
        typeDropdown.setAdapter(adapter);

        // Prevent manual typing
        typeDropdown.setOnClickListener(v -> typeDropdown.showDropDown());
        typeDropdown.setOnTouchListener((v, event) -> {
            typeDropdown.requestFocus();
            typeDropdown.showDropDown();
            return false;
        });
        // Handle selection
        typeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = adapter.getItem(position);

        });

// Handle close button click
        iv_close.setOnClickListener(v -> {
            startActivity(new Intent(AddIdeasActivity.this, HomeActivity.class));
            finish(); // Ensure activity is closed to prevent back navigation
        });
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                userName = snapshot.child("username").getValue(String.class);
                userHeadline = snapshot.child("headline").getValue(String.class);

                tv_user_name.setText(userName != null ? userName : "User");
                if (profilePicUrl != null) {
                    Glide.with(getApplicationContext()) // Replace 'context' with your Activity or Fragment context
                            .load(profilePicUrl) // URL or drawable resource
                            .placeholder(R.drawable.baseline_person_24) // Placeholder image
                            .transform(new CircleCrop()) // Apply CircleCrop transformation
                            .into(iv_profile); // Target ImageView
                } else {
                    iv_profile.setImageResource(R.drawable.baseline_person_24);
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            loadImagePreview(imageUri);
            isImageAdded = true;
        }
    }

    private void uploadImageToCloudinary(String url) {
        AsyncTask.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.parse(url));
                File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");
                saveIdeaToFirebase(imageUrl);

                runOnUiThread(() -> {
                    clearImagePreview();
                    et_post_content.setText("");
                });
            } catch (Exception e) {

            }
        });
    }

    private void saveIdeaToFirebase(String imageUrl) {
        String ideaText = et_post_content.getText().toString().trim();
        if (ideaText.isEmpty()) {
            et_post_content.setError("Idea cannot be empty");
            et_post_content.requestFocus();
            LinearAdd.setVisibility(View.VISIBLE);
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        if (selectedType == null || selectedType.trim().isEmpty()) {
            Toast.makeText(this, "Idea title cannot be empty", Toast.LENGTH_SHORT).show();
            LinearAdd.setVisibility(View.VISIBLE);
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        String ideaId = publicIdeasRef.push().getKey();

        Map<String, Object> ideaData = new HashMap<>();
        ideaData.put("content", ideaText);
        ideaData.put("UserId", userId);
        ideaData.put("imageUrl", imageUrl != null ? imageUrl : "");
        ideaData.put("UserImage", profilePicUrl != null ? profilePicUrl : R.drawable.baseline_person_24);
        ideaData.put("UserName", userName != null ? userName : "User");
        ideaData.put("Headline", userHeadline != null ? userHeadline : "");
        ideaData.put("timestamp", System.currentTimeMillis());
        ideaData.put("titleIdeas",selectedType);

        // Save to User's Ideas
        assert ideaId != null;
        userIdeasRef.child(ideaId).setValue(ideaData)
                .addOnSuccessListener(unused -> {})
                        .addOnFailureListener(e -> {
                        });
        // Save to PublicIdeas
        publicIdeasRef.child(ideaId).setValue(ideaData);
        startActivity(new Intent(AddIdeasActivity.this, HomeActivity.class));
        ProgressBar.setVisibility(View.GONE);
        LinearAdd.setVisibility(View.VISIBLE);
    }

    private void loadImagePreview(Uri uri) {
        iv_post_image_preview_frame.setVisibility(View.VISIBLE);
        iv_post_image_preview.setVisibility(View.VISIBLE);
        iv_cancel_image.setVisibility(View.VISIBLE);
        Picasso.get().load(uri).into(iv_post_image_preview);
    }

    private void clearImagePreview() {
        iv_post_image_preview_frame.setVisibility(View.GONE);
        iv_post_image_preview.setVisibility(View.GONE);
        iv_cancel_image.setVisibility(View.GONE);
        isImageAdded = false;
    }
}
