package com.example.businessidea.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
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

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;


public class LoginActivity extends BaseActivity {
EditText etPassword,loginEmail;
AppCompatButton LoginHome;
TextView RegisterNow,ForgetPassword;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    GoogleSignInClient signInClient;
    Intent intent;
    SharedPreferences sharedPreferences;
ImageView ivTogglePassword,GoogleL;
private boolean isPasswordVisible = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.loginemail);
        etPassword = findViewById(R.id.loginPassword);
        LoginHome = findViewById(R.id.LoginHome);
        RegisterNow = findViewById(R.id.RegisterNow);
        ivTogglePassword = findViewById(R.id.visiblePasswordOrNot);
        GoogleL = findViewById(R.id.GoogleL);
        ForgetPassword= findViewById(R.id.ForgetPassowrd);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Fetch and store FCM token
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM token failed", task.getException());
                            return;
                        }

                        // Get the FCM token
                        String token = task.getResult();
                        Log.d("FCM", "Token: " + token);

                        // Save the token to Firebase Database under the user's profile
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUid())
                                .child("token").setValue(token);
                    });
        }

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

        ForgetPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });
        LoginHome.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, EditProfileActivity.class))
        );
        RegisterNow.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this,RegisterActivity.class)));
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                // Show password
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
            }
            // Move cursor to the end of the text
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);

        GoogleL.setOnClickListener(view -> SignIn());
// Login Button Click Listener
        LoginHome.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress bar


            // Sign in with Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if (currentUser != null && currentUser.isEmailVerified()) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                                editor1.putBoolean("flag", true);
                                editor1.apply();

                                // Retrieve user data from Firebase
                                databaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Users existingUser = snapshot.getValue(Users.class);
                                            if (existingUser != null &&
                                                    (existingUser.getPhoneNumber() == null || existingUser.getPhoneNumber().isEmpty() ||
                                                            existingUser.getPronouns() == null || existingUser.getPronouns().isEmpty() ||
                                                            existingUser.getHeadline() == null || existingUser.getHeadline().isEmpty() ||
                                                            existingUser.getBirthday() == null || existingUser.getBirthday().isEmpty())) {
                                                // If any mandatory fields are missing, navigate to EditProfileActivity
                                                intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                                            } else {
                                                // If all fields are filled, navigate to HomeActivity
                                                intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                finish();
                                            }
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // User does not exist in the database, show an error message or handle accordingly
                                            Toast.makeText(LoginActivity.this, "User not found in the database.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("TAG", "Database error: " + error.getMessage());
                                        Toast.makeText(LoginActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Email not verified
                                Toast.makeText(LoginActivity.this, "Please verify your email.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Login failed
                            Toast.makeText(LoginActivity.this, "Incorrect Password or Email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private static final int REQ_ONE_TAP = 8001;

    private void SignIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_ONE_TAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google sign-in failed", e);
                Toast.makeText(LoginActivity.this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = sharedPreferences.edit();
                        editor1.putBoolean("flag", true);
                        editor1.apply();
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
                                        intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                                    } else {
                                        // All fields are present, navigate to HomeActivity
                                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        finish();
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
                                    Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("TAG", "Database error: " + error.getMessage());
                                Toast.makeText(LoginActivity.this, "Error fetching user data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Sign-in failure
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}