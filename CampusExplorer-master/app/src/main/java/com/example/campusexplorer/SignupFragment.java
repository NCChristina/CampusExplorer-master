package com.example.campusexplorer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campusexplorer.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextInputEditText etFirstName, etLastName, etStudentId, etDepartment,
            etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Firebase Authen
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etStudentId = view.findViewById(R.id.etStudentId);
        etDepartment = view.findViewById(R.id.etDepartment);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        progressBar = view.findViewById(R.id.progressBar);

        btnSignUp.setOnClickListener(v -> signupButtonClicked());

        return view;
    }

    private void signupButtonClicked() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Basic validation
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign up successful then show success messge
                        Log.d("SignupFragment", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user to database
                        saveUserToDatabase(user.getUid(), firstName, lastName, email, studentId, department);
                    } else {
                        // in case sign up fails, display failure message to the user
                        progressBar.setVisibility(View.GONE);
                        Log.w("SignupFragment", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String firstName, String lastName,
                                    String email, String studentId, String department) {
        // Create user object with all required data
        User newUser = new User(userId, firstName, lastName, studentId, department, email);
        // The User constructor will initialize points to 0 and challenges to all false

        mDatabase.child("users").child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignupFragment", "User profile saved to database");

                        // Send verification email
                        FirebaseUser user1 = mAuth.getCurrentUser();
                        sendVerificationEmail(user1);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.w("SignupFragment", "Error saving user data", task.getException());
                        Toast.makeText(getContext(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Registration successful! Please check your email for verification.",
                                Toast.LENGTH_LONG).show();

                        // now go to login tab
                        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                        viewPager.setCurrentItem(0, true);
                    } else {
                        Toast.makeText(getContext(),
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}