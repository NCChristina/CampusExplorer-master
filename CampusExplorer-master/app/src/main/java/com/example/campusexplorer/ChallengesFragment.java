package com.example.campusexplorer;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChallengesFragment extends Fragment {
    private ListView challengesListView;
    private List<String> challengeTitles;
    private List<String> challengeDescriptions;
    private List<String> challengeIds;
    private List<Boolean> challengeCompleted;

    private List<Double> challengeLatitudes; //added these for location
    private List<Double> challengeLongitudes;
    private ChallengeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenges, container, false);

        // Initialize ListView
        challengesListView = view.findViewById(R.id.challengesListView);

        challengeTitles = new ArrayList<>();
        challengeDescriptions = new ArrayList<>();
        challengeIds = new ArrayList<>();
        challengeCompleted = new ArrayList<>();

         adapter = new ChallengeAdapter();
        challengesListView.setAdapter(adapter);

        loadChallenges();// laoding from firebase

        return view;
    }

    private class ChallengeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return challengeTitles.size();
        }

        @Override
        public Object getItem(int position) {
            return challengeTitles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.item_challenge_card, parent, false);
                holder = new ViewHolder();
                holder.title = view.findViewById(R.id.tvChallengeTitle);
                holder.description = view.findViewById(R.id.tvChallengeDescription);
                holder.statusIcon = view.findViewById(R.id.ivChallengeStatus);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            // Setting data
            holder.title.setText(challengeTitles.get(position));
            holder.description.setText(challengeDescriptions.get(position));


            String title = challengeTitles.get(position).trim();

            // Add tick emoji if challenge is completed
            if (challengeCompleted.get(position)) {
                title += " ✅";
            }

            holder.title.setText(title);
            holder.description.setText(challengeDescriptions.get(position));

            view.setOnClickListener(v -> showChallengeDialog(position));

            return view;
        }

        class ViewHolder {
            TextView title;
            TextView description;
            ImageView statusIcon;
        }
    }

    // Show dialog with challenge details and buttons
    private void showChallengeDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(challengeTitles.get(position));
        builder.setMessage(challengeDescriptions.get(position));

        builder.setNegativeButton("Exit", null);

        // Add Get Directions button with actual map navigation
        builder.setPositiveButton("Get Directions", (dialog, which) -> {
            openDirections(position);
        });

        builder.show();
    }

    private void openDirections(int position) {
        // Make sure we have the coordinates
        if (challengeLatitudes == null || challengeLongitudes == null ||
                position >= challengeLatitudes.size() || position >= challengeLongitudes.size()) {
            Toast.makeText(getContext(), "Location information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the destination coordinates
        double latitude = challengeLatitudes.get(position);
        double longitude = challengeLongitudes.get(position);

        Uri mapUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w");

        // Intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps is installed
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // In case Google Maps is not installed then open in a web browser
            Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                    + latitude + "," + longitude + "&travelmode=walking");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            startActivity(browserIntent);
        }
    }

    private void loadChallenges() {
        // Initialize the location lists
        challengeLatitudes = new ArrayList<>();
        challengeLongitudes = new ArrayList<>();

        // Fetch challenges from Firebase
        DatabaseReference challengesRef = FirebaseDatabase.getInstance().getReference().child("challenges");

        challengesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data if we're reloading
                challengeTitles.clear();
                challengeDescriptions.clear();
                challengeIds.clear();
                challengeCompleted.clear();
                challengeLatitudes.clear();
                challengeLongitudes.clear();

                // Load all challenges from Firebase
                for (DataSnapshot challengeSnapshot : dataSnapshot.getChildren()) {
                    String id = challengeSnapshot.getKey();
                    String title = challengeSnapshot.child("title").getValue(String.class);
                    String description = challengeSnapshot.child("description").getValue(String.class);
                    Double latitude = challengeSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = challengeSnapshot.child("longitude").getValue(Double.class);

                    if (id != null && title != null && description != null) {
                        challengeIds.add(id);
                        challengeTitles.add(title);
                        challengeDescriptions.add(description);
                        challengeLatitudes.add(latitude != null ? latitude : 0.0);
                        challengeLongitudes.add(longitude != null ? longitude : 0.0);
                        challengeCompleted.add(false);
                    }
                }

                // Update the adapter
                adapter.notifyDataSetChanged();

                // Now load completion status
                loadCompletionStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChallengesFragment", "Error loading challenges", databaseError.toException());
            }
        });
    }

    private void loadCompletionStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference userChallengesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("challenges");

        userChallengesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < challengeIds.size(); i++) {
                    String challengeId = challengeIds.get(i);
                    if (dataSnapshot.hasChild(challengeId)) {
                        Boolean completed = dataSnapshot.child(challengeId).getValue(Boolean.class);
                        if (completed != null && completed) {
                            challengeCompleted.set(i, true);
                        }
                    }
                }

                // Refresh the list view
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChallengesFragment", "Error loading challenge status", databaseError.toException());
            }
        });
    }
}