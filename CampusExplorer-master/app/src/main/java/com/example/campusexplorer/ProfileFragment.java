package com.example.campusexplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;

public class ProfileFragment extends Fragment {
    private TextView tvUserName, tvUserEmail, tvUserPoints;
    private Button btnLogout;
    private ImageView ivProfilePicture;
    private ImageButton btnEditPicture;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveProfileImage(imageUri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        mAuth = FirebaseAuth.getInstance();
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserPoints = view.findViewById(R.id.tvUserPoints);//added this to show user points
        btnLogout = view.findViewById(R.id.btnLogout);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnEditPicture = view.findViewById(R.id.btnEditPicture);


        btnEditPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });


        loadUserInfo();
        loadProfileImage();

        return view;
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvUserEmail.setText(user.getEmail());

            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                if (firstName != null && lastName != null) {
                                    tvUserName.setText(firstName + " " + lastName);
                                }

                                Integer points = snapshot.child("points").getValue(Integer.class);
                                if (points != null) {
                                    tvUserPoints.setText("Points: " + points);
                                } else {
                                    tvUserPoints.setText("Points: 0");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void saveProfileImage(Uri imageUri) {
        try {

            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), imageUri);

            // Calculating new dimensions
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float scale = Math.min(300f / width, 300f / height);

            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);


            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap, newWidth, newHeight, true);

            // Save
            String fileName = "profile_" + mAuth.getCurrentUser().getUid() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);

            FileOutputStream out = new FileOutputStream(file);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();

            // Update image view
            ivProfilePicture.setImageBitmap(scaledBitmap);


            if (originalBitmap != scaledBitmap) {
                originalBitmap.recycle();
            }

            Toast.makeText(getContext(), "Profile picture saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error saving image: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage() {
        if (getContext() == null) return;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String fileName = "profile_" + user.getUid() + ".jpg";
        File file = new File(requireContext().getFilesDir(), fileName);

        if (file.exists()) {
            // Using glide to load images cause it works well. and handles memory
            Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfilePicture);
        }
    }
}