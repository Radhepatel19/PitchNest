package com.example.businessidea.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.businessidea.BaseActivity;

import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RegisterActivity extends BaseActivity {
    private AppCompatButton Register;
    private TextView LoginNow;
    private ImageView ArrowBack2, GoogleR;
    private EditText UserName, Email, Password, ConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient signInClient;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        Register = findViewById(R.id.RegisterToLogin);
        LoginNow = findViewById(R.id.LoginNow);
        ArrowBack2 = findViewById(R.id.ArrowBack2);
        UserName = findViewById(R.id.UserName);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        ConfirmPassword = findViewById(R.id.ConfirmPassword);
        GoogleR = findViewById(R.id.GoogleR);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

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
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);

        // Register button logic
        // Register button logic
        Register.setOnClickListener(view -> {
            if (Email.getText().toString().isEmpty() || UserName.getText().toString().isEmpty() || Password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter Credentials", Toast.LENGTH_SHORT).show();
            } else {
                String email = Email.getText().toString();
                String username = UserName.getText().toString();
                String password = Password.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();

                                                // Create user object with default values for other fields
                                                Users user = new Users(
                                                        username, // Username
                                                        email,    // Email
                                                        password, // Password
                                                        "",       // ProfilePic
                                                        "",       // Phone number
                                                        "",       // Pronouns
                                                        "",       // Headline
                                                        "",       // Country
                                                        "",       // City
                                                        "",       // Address
                                                        "",       //Birthday
                                                        "0",       // Follow
                                                        "0",       //Following
                                                        ""        // About

                                                );

                                                // Save user to Firebase
                                                String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                                database.getReference().child("Users").child(id).setValue(user);
                                                database.getReference().child("Users").child(id).child("userId").setValue(id);

                                                // Redirect to main activity
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                            } else {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        // Google Sign-In button logic
        // Navigation buttons
        LoginNow.setOnClickListener(v -> navigateToLogin());
        ArrowBack2.setOnClickListener(v -> navigateToLogin());
        GoogleR.setOnClickListener(v -> SignIn());
    }

    private static final int REQ_ONE_TAP = 8001;
    private void SignIn(){
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent,REQ_ONE_TAP);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Handle exception
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithCredential:success");
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        FirebaseUser user = mAuth.getCurrentUser();

                        String userEmail = user.getEmail();
                        String userId = user.getUid();

                        // Query to check if the email is already stored
                        Query emailQuery = databaseReference.orderByChild("mail").equalTo(userEmail);
                        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Email exists, check additional fields for the current user ID
                                    DataSnapshot userSnapshot = snapshot.child(userId);
                                    String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                                    String headline = userSnapshot.child("headline").getValue(String.class);
                                    String pronouns = userSnapshot.child("pronouns").getValue(String.class);

                                    if (Objects.equals(phoneNumber, "") || Objects.equals(headline, "") || Objects.equals(pronouns, "")) {
                                        // If any required field is null, go to EditProfileActivity
                                        intent = new Intent(RegisterActivity.this, EditProfileActivity.class);
                                    } else {
                                        // All fields are present, navigate to HomeActivity
                                        intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    }
                                    startActivity(intent);
                                } else {
                                    // First-time login: store user data
                                    Users newUser = new Users();
                                    newUser.setMail(user.getEmail());
                                    newUser.setUsername(user.getDisplayName());
                                    newUser.setProfilePic(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                                    newUser.setPhoneNumber("");
                                    newUser.setPronouns("");
                                    newUser.setHeadline("");
                                    newUser.setCountry("");
                                    newUser.setCity("");
                                    newUser.setAddress("");
                                    newUser.setBirthday("");
                                    newUser.setAbout("");
                                    newUser.setFollow("0");
                                    newUser.setFollowing("0");
                                    newUser.setUserId(userId);

                                    // Save new user to the database
                                    databaseReference.child(userId).setValue(newUser);

                                    // Navigate to EditProfileActivity
                                    Intent intent = new Intent(RegisterActivity.this, EditProfileActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("TAG", "Database error: " + error.getMessage());
                                Toast.makeText(RegisterActivity.this, "Error fetching user data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Sign-in failure
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
