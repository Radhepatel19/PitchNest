package com.example.businessidea.Activity;



import android.annotation.SuppressLint;

import android.app.Dialog;
import android.content.Intent;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.businessidea.BaseActivity;


import com.example.businessidea.R;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileActivity extends BaseActivity {
    AppCompatButton btn_save;
    private EditText etUsername, etCountry, etCity, etPhoneNumber, etAddress, etBirthday, etPronouns, etHeadline;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    ImageView ivProfilePic;
    FirebaseDatabase database;
    FirebaseUser firebaseUser;
    String UserId;
    ProgressBar ProgressBar;
    String profilePicUrl;
    private boolean isImageAdded = false;
    private Uri imageUri;
    private Cloudinary cloudinary;
    Button btnChangePic;
    LinearLayout LinearEdit;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        ProgressBar = findViewById(R.id.ProgressBar);
        btn_save = findViewById(R.id.btn_save);
        etHeadline = findViewById(R.id.et_headline);
        etUsername = findViewById(R.id.et_username);
        etCountry = findViewById(R.id.et_country);
        etCity = findViewById(R.id.et_city);
        etPronouns = findViewById(R.id.et_pronouns);
        etPhoneNumber = findViewById(R.id.et_mobile);
        etAddress = findViewById(R.id.et_address);
        etBirthday = findViewById(R.id.et_birthday);
        btnChangePic = findViewById(R.id.btn_change_pic);
        ivProfilePic = findViewById(R.id.iv_profile_pic);
        LinearEdit = findViewById(R.id.LinearEdit);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        UserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
        LinearEdit.setVisibility(View.VISIBLE);
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "Your Cloud Name",
                "api_key", "Your API Key",
                "api_secret", "Your API Secret"
        ));
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(UserId);

        EditText etBirthday = findViewById(R.id.et_birthday);
        ImageView ivBirthdayIcon = findViewById(R.id.birthday_icon);

        ivBirthdayIcon.setOnClickListener(view -> openBirthdayDialog(etBirthday));
        btnChangePic.setOnClickListener(v -> openGallery());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    String Name = snapshot.child("username").getValue(String.class);
                    String Birthday = snapshot.child("birthday").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String country = snapshot.child("country").getValue(String.class);
                    String headline = snapshot.child("headline").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String pronouns = snapshot.child("pronouns").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    if (profilePicUrl != null){
                        uploadImageToCloudinary(profilePicUrl);
                        loadImage(profilePicUrl);
                    }
                    etUsername.setText(Name);
                    etBirthday.setText(Birthday);
                    etAddress.setText(address);
                    etCity.setText(city);
                    etCountry.setText(country);
                    etHeadline.setText(headline);
                    etPhoneNumber.setText(phoneNumber);
                    etPronouns.setText(pronouns);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_save.setOnClickListener(v -> {
            if (isImageAdded) {
                LinearEdit.setVisibility(View.GONE);
                ProgressBar.setVisibility(View.VISIBLE);
                uploadImageToCloudinary(String.valueOf(imageUri));
            }else {
                LinearEdit.setVisibility(View.GONE);
                ProgressBar.setVisibility(View.VISIBLE);
                saveProfileData(null);
            }
        });
    }
    private static final int PICK_IMAGE_REQUEST = 870;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            loadImage(String.valueOf(imageUri));
            isImageAdded = true;
        }
    }
    private void uploadImageToCloudinary(String url) {
        AsyncTask.execute(() -> {
            try {
                // Open InputStream directly from the URI
                InputStream inputStream = getContentResolver().openInputStream(Uri.parse(url));
                File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Upload the temporary file to Cloudinary
                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");

                // Store the URL in Firebase
                mDatabase.child("profilePic").setValue(imageUrl);

                saveProfileData(imageUrl);

                runOnUiThread(() -> {

                    Picasso.get().load(url).into(ivProfilePic);
                });
            } catch (Exception e) {
                Log.e("CloudinaryError", "Upload failed", e);
            }
        });
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .transform(new CircleCrop())
                    .error(R.drawable.baseline_person_24)
                    .into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void saveProfileData(String imageUrl) {
        // Retrieve the input data
        String username = etUsername.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String headline = etHeadline.getText().toString().trim();
        String pronouns = etPronouns.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();

        // Validation: Check if all fields are filled
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(headline)) {
            Toast.makeText(this, "Headline is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pronouns)) {
            Toast.makeText(this, "Pronouns are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Country is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, "City is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Phone Number is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(birthday)) {
            Toast.makeText(this, "Birthday is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Users object
        mDatabase.child("username").setValue(username);
        mDatabase.child("phoneNumber").setValue(phoneNumber);
        mDatabase.child("pronouns").setValue(pronouns);
        mDatabase.child("headline").setValue(headline);
        mDatabase.child("city").setValue(city);
        mDatabase.child("country").setValue(country);
        mDatabase.child("address").setValue(address);
        mDatabase.child("birthday").setValue(birthday);
        updateAllIdeasUserImage(UserId,imageUrl);
        Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        ProgressBar.setVisibility(View.GONE);
        LinearEdit.setVisibility(View.VISIBLE);
        finish();

    }

    private void openBirthdayDialog(EditText etBirthday) {
        // Create a dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_birthday_picker);

        // Find NumberPickers in the dialog
        NumberPicker monthPicker = dialog.findViewById(R.id.np_month);
        NumberPicker dayPicker = dialog.findViewById(R.id.np_day);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        // Define months
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        // Set up month picker
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(months.length - 1);
        monthPicker.setDisplayedValues(months);
        monthPicker.setWrapSelectorWheel(false);

        // Set up day picker (always 1-31)
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setWrapSelectorWheel(false);

        // Handle confirm button click
        btnConfirm.setOnClickListener(v -> {
            String selectedDate = months[monthPicker.getValue()] + " " + dayPicker.getValue();
            etBirthday.setText(selectedDate);
            dialog.dismiss();
        });

        // Show dialog
        dialog.show();
    }

    private void updateAllIdeasUserImage(String userId, String newImageUrl) {
        DatabaseReference ideasRef = FirebaseDatabase.getInstance().getReference("PublicIdeas");
        DatabaseReference UsersIdeasRef = FirebaseDatabase.getInstance().getReference("Users").child(UserId).child("Ideas");

        if (newImageUrl != null) {
            // Query to find all ideas with the matching userId
            ideasRef.orderByChild("UserId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                            // Update the userImage field for each matching idea
                            ideaSnapshot.getRef().child("UserImage").setValue(newImageUrl)
                                    .addOnSuccessListener(aVoid -> Log.d("UpdateSuccess", "User image updated for idea: " + ideaSnapshot.getKey()))
                                    .addOnFailureListener(e -> Log.e("UpdateFailure", "Failed to update image for idea: " + ideaSnapshot.getKey(), e));
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DatabaseError", "Error fetching ideas: " + error.getMessage());
                }
            });
            UsersIdeasRef.orderByChild("UserId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().child("UserImage").setValue(newImageUrl).addOnSuccessListener(unused -> {
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private List<String> generateMonthsAndDays() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        List<String> dates = new ArrayList<>();

        // Loop through months and days
        for (String month : months) {
            for (int day = 1; day <= 31; day++) {
                dates.add(month + " " + day);
            }
        }
        return dates;
    }
}