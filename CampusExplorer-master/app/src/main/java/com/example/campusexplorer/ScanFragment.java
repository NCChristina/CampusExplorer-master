//used a bit of AI to get rid of errors

package com.example.campusexplorer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

//// Not using this cause idk why it's not working even after adding edependency and synching and building
//import com.journeyapps.barcodescanner.IntentIntegrator;
//import com.journeyapps.barcodescanner.IntentResult;

// using this from google zxing
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRScanner();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        view.findViewById(R.id.btnStartScan).setOnClickListener(v -> checkCameraPermission());
        return view;
    }

//    private void checkCameraPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//                getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
//        } else {
//            startQRScanner();
//        }
//    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startQRScanner();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQRScanner();
        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a campus QR code");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                handleQRResult(result.getContents());
            } else {
                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//    private void handleQRResult(String qrContent) {
//        String message = "";
//        String title = "";
//        String locationId;
//
//        if (qrContent.contains("GLOBAL_LOUNGE")) {
//            title = "Global Lounge";
//            message = "Welcome to Global Lounge! Make yourself at home, have a fun time and don't forget your coffee and sweets.";
//            locationId = "GLOBAL_LOUNGE";
//        } else if (qrContent.contains("CLIFTON_TRAIL")) {
//            title = "Clifton Trail";
//            message = "Welcome to the wonderful nature trail at Clifton! Take a long walk and re-energize.";
//            locationId = "CLIFTON_TRAIL";
//        } else if (qrContent.contains("CAREER_FAIR")) {
//            title = "Careers Fair";
//            message = "Welcome! Glad you made it. Explore your future recruiters and don't be shy to ask the important questions.";
//            locationId = "CAREER_FAIR";
//        } else {
//            locationId = "";
//            Toast.makeText(getContext(), "Unknown QR code", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        new AlertDialog.Builder(getContext())
//                .setTitle(title)
//                .setMessage(message + "\n\nChallenge completed!")
//                .setPositiveButton("Great!", (dialog, which) -> markChallengeCompleted(locationId))
//                .setCancelable(false)
//                .show();
//    }
//
    private void handleQRResult(String qrContent) {
        String title;
        String message;
        String locationId;
        String gifName;
        int points;

        if (qrContent.contains("GLOBAL_LOUNGE")) {
            title = "Global Lounge";
            message = "Welcome to Global Lounge! Make yourself at home, have a fun time and don't forget your coffee and sweets.";
            locationId = "GLOBAL_LOUNGE";
            gifName = "global_lounge.gif";
            points = 10;
        } else if (qrContent.contains("CLIFTON_TRAIL")) {
            title = "Clifton Trail";
            message = "Welcome to the wonderful nature trail at Clifton! Take a long walk and re-energize.";
            locationId = "CLIFTON_TRAIL";
            gifName = "adventurer.gif";
            points = 10;
        } else if (qrContent.contains("CAREER_FAIR")) {
            title = "Careers Fair";
            message = "Welcome! Glad you made it. Explore your future recruiters and don't be shy to ask the important questions.";
            locationId = "CAREER_FAIR";
            gifName = "congratulations.gif";
            points = 10;
        } else if (qrContent.contains("HONEYPOT")) {
            title = "Honeypot Found!";
            message = "You've discovered a hidden honeypot! You've earned extra points for your exploration skills.";
            locationId = "HONEYPOT_" + System.currentTimeMillis();
            gifName = "honeypot.gif";
            points = 60;
        } else {
            Toast.makeText(getContext(), "Unknown QR code", Toast.LENGTH_LONG).show();
            return;
        }

        showCustomChallengeDialog(locationId, title, message, gifName, points);
    }

// //Mark challenge completed without the honeypot
//    private void markChallengeCompleted(String challengeId) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) return;
//
//        FirebaseDatabase.getInstance().getReference()
//                .child("users").child(user.getUid()).child("challenges").child(challengeId)
//                .setValue(true)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(getContext(), "Challenge completed!", Toast.LENGTH_SHORT).show();
//                    awardPoints(10);
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getContext(), "Failed to update challenge", Toast.LENGTH_SHORT).show();
//                });
//    }

  private void markChallengeCompleted(String challengeId, int pointsToAward) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("challenges").child(challengeId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Challenge completed!", Toast.LENGTH_SHORT).show();
                    awardPoints(pointsToAward);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update challenge", Toast.LENGTH_SHORT).show();
                });
    }
    private void showCustomChallengeDialog(String challengeId, String title, String message, String gifName, int pointsToAward) {
        // dialog view thing
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_honeypot, null);

        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        ImageView challengeImage = dialogView.findViewById(R.id.honeypotImage);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        TextView messageText = dialogView.findViewById(R.id.dialogMessage);
        TextView pointsText = dialogView.findViewById(R.id.pointsText);
        Button collectButton = dialogView.findViewById(R.id.btnCollect);

        titleText.setText(title);
        messageText.setText(message);
        pointsText.setText("+" + pointsToAward + " points");

        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/" + gifName))
                .into(challengeImage);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        collectButton.setOnClickListener(v -> {
            // to award points and mark as completed
            markChallengeCompleted(challengeId, pointsToAward);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void awardPoints(int points) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("points")
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer currentPoints = mutableData.getValue(Integer.class);
                        mutableData.setValue(currentPoints == null ? points : currentPoints + points);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean committed,
                                           @Nullable DataSnapshot dataSnapshot) {
                        if (committed) {
                            Toast.makeText(getContext(), points + " points earned!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
