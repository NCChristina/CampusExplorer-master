package com.example.campusexplorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvPointsTotal, tvChallengesCompleted, tvRanking;
    private TextView tvFeaturedChallenge, tvFeaturedChallengeDesc;
    private ProgressBar progressChallenges;
    private Button btnViewChallenges;

    private List<String> challengeTitles = new ArrayList<>();
    private List<String> challengeDescriptions = new ArrayList<>();
    private List<String> challengeIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvPointsTotal = view.findViewById(R.id.tvPointsTotal);
        tvChallengesCompleted = view.findViewById(R.id.tvChallengesCompleted);
        tvRanking = view.findViewById(R.id.tvRanking);
        tvFeaturedChallenge = view.findViewById(R.id.tvFeaturedChallenge);
        tvFeaturedChallengeDesc = view.findViewById(R.id.tvFeaturedChallengeDesc);
        progressChallenges = view.findViewById(R.id.progressChallenges);
        btnViewChallenges = view.findViewById(R.id.btnViewChallenges);

        // challenges data
        initializeChallengesData();

        // Set featured challenge block
        setFeaturedChallenge();

        // Load user data
        loadUserData();

        btnViewChallenges.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.navigation_challenges);
        });

        return view;
    }

    private void initializeChallengesData() {
        challengeTitles.add("Visit Global Lounge");
        challengeDescriptions.add("Make yourself at home and don't forget your coffee and sweets.");
        challengeIds.add("GLOBAL_LOUNGE");

        challengeTitles.add("Clifton Trail");
        challengeDescriptions.add("Take a long walk on the nature trail and re-energize.");
        challengeIds.add("CLIFTON_TRAIL");

        challengeTitles.add("Careers Fair");
        challengeDescriptions.add("Explore your future recruiters and ask the important questions.");
        challengeIds.add("CAREER_FAIR");
    }

    private void setFeaturedChallenge() {
        // display an exclusive or featured challenge
        int featuredIndex = new Random().nextInt(challengeTitles.size());
        tvFeaturedChallenge.setText(challengeTitles.get(featuredIndex));
        tvFeaturedChallengeDesc.setText(challengeDescriptions.get(featuredIndex));
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Geting the user name for welcome message
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                if (firstName != null) {
                    tvWelcome.setText("Hi, " + firstName + "!");
                }

                // Getting the  points
                Integer points = dataSnapshot.child("points").getValue(Integer.class);
                if (points != null) {
                    tvPointsTotal.setText(points + " points");
                    // user rank calculation
                    fetchUserRank(user.getUid(), points);
                } else {
                    tvPointsTotal.setText("0 points");
                    tvRanking.setText("Rank: N/A");
                }

                //  counting all challenges only once
                List<String> challengeIds = new ArrayList<>();
                challengeIds.add("GLOBAL_LOUNGE");
                challengeIds.add("CLIFTON_TRAIL");
                challengeIds.add("CAREER_FAIR");

                 int completedCount = 0;
                if (dataSnapshot.hasChild("challenges")) {
                    DataSnapshot challengesSnapshot = dataSnapshot.child("challenges");
                    for (String challengeId : challengeIds) {
                        if (challengesSnapshot.hasChild(challengeId)) {
                            Boolean completed = challengesSnapshot.child(challengeId).getValue(Boolean.class);
                            if (completed != null && completed) {
                                completedCount++;
                            }
                        }
                    }
                }

                tvChallengesCompleted.setText(completedCount + " of 10 completed");
                progressChallenges.setProgress(completedCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                tvRanking.setText("Cannot display rank");
            }
        });
    }

    private void fetchUserRank(String userId, int userPoints) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        Query rankQuery = usersRef.orderByChild("points");

        rankQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    List<Integer> allPoints = new ArrayList<>();


                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Integer points = userSnapshot.child("points").getValue(Integer.class);
                        if (points != null) {
                            allPoints.add(points);
                        }
                    }
                    allPoints.sort((a, b) -> b - a);

                    // Find user's rank
                    int rank = allPoints.indexOf(userPoints) + 1;

                    if (rank > 0) {
                        tvRanking.setText("Rank " + rank + " of " + allPoints.size());
                    } else {
                        tvRanking.setText("Rank: N/A");
                    }
                } catch (Exception e) {
                    tvRanking.setText("Cannot display rank");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                tvRanking.setText("Cannot display rank");
            }
        });
    }
}