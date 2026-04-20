package com.example.businessidea.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
public class ForgetPasswordActivity extends BaseActivity {
    Button send;
    EditText Email;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    GoogleSignInClient signInClient;
    SharedPreferences sharedPreferences;
    Intent intent;
//private String mVerificationId;

    TextView google;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        send = findViewById(R.id.Send);
        Email = findViewById(R.id.Email);
        progressBar = findViewById(R.id.ProgressBar);
        google = findViewById(R.id.google);

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
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);

        google.setOnClickListener(view -> SignIn());

        send.setOnClickListener(view -> {
            if (!Email.getText().toString().isEmpty()) {
                String emailToCheck = Email.getText().toString();

                if (Patterns.EMAIL_ADDRESS.matcher(emailToCheck).matches()) {
                    progressBar.setVisibility(View.VISIBLE);
                    send.setVisibility(View.INVISIBLE);

                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                    usersRef.orderByChild("mail").equalTo(emailToCheck).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mAuth.sendPasswordResetEmail(emailToCheck).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(ForgetPasswordActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Toast.makeText(ForgetPasswordActivity.this, "Email is sent. Create a new password.", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        send.setVisibility(View.VISIBLE);
                                        Toast.makeText(ForgetPasswordActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                send.setVisibility(View.VISIBLE);
                                Toast.makeText(ForgetPasswordActivity.this, "Email Doesn't Exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBar.setVisibility(View.INVISIBLE);
                            send.setVisibility(View.VISIBLE);
                            Toast.makeText(ForgetPasswordActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Enter a valid email address.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ForgetPasswordActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
            }
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
                                        intent = new Intent(ForgetPasswordActivity.this, EditProfileActivity.class);
                                    } else {
                                        // All fields are present, navigate to HomeActivity
                                        intent = new Intent(ForgetPasswordActivity.this, HomeActivity.class);
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
                                    Intent intent = new Intent(ForgetPasswordActivity.this, EditProfileActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("TAG", "Database error: " + error.getMessage());
                                Toast.makeText(ForgetPasswordActivity.this, "Error fetching user data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Sign-in failure
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(ForgetPasswordActivity.this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}