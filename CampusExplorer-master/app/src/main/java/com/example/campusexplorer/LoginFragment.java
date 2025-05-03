package com.example.campusexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//import com.example.campusexplorer.CampusBuildingsActivity;
import com.example.campusexplorer.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextInputEditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Firebase Authen
        mAuth = FirebaseAuth.getInstance();

        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        progressBar = view.findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginButtonClicked());
        tvForgotPassword.setOnClickListener(v -> forgotPasswordClicked());

        return view;
    }

    private void loginButtonClicked() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // user Validation
        if (email.isEmpty()) {
            etLoginEmail.setError("Email is required");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // If Sign in is success
                        Log.d("LoginFragment", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // verifiying email check
                        if (user.isEmailVerified()) {
                            // If yes then go to main activity dashboard
                            startActivity(new Intent(getActivity(), MainDashboardActivity.class));
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Please verify your email first", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, show failure message to the user
                        Log.w("LoginFragment", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //incase user forgets password
    private void forgotPasswordClicked() {
        String email = etLoginEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}