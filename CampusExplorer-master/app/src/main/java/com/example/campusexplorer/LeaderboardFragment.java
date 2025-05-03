package com.example.campusexplorer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private ListView leaderboardListView;
    private List<UserScore> userScores = new ArrayList<>();
    private LeaderboardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboardListView = view.findViewById(R.id.leaderboardListView);

        adapter = new LeaderboardAdapter();
        leaderboardListView.setAdapter(adapter);

        loadLeaderboardData();

        return view;
    }

    private class LeaderboardAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return userScores.size();
        }

        @Override
        public Object getItem(int position) {
            return userScores.get(position);
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
                view = getLayoutInflater().inflate(R.layout.item_leaderboard_card, parent, false);
                holder = new ViewHolder();
                holder.rank = view.findViewById(R.id.tvRank);
                holder.userName = view.findViewById(R.id.tvUserName);
                holder.points = view.findViewById(R.id.tvPoints);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            UserScore user = userScores.get(position);
            int rank = position + 1;
            if (position > 0 && user.points.equals(userScores.get(position - 1).points)) {
                // adjust user rank
                rank = Integer.parseInt(((ViewHolder)leaderboardListView.getChildAt(position - 1).getTag()).rank.getText().toString());
            }

            holder.rank.setText(String.valueOf(rank));
            holder.userName.setText(user.name);
            holder.points.setText(user.points + " pts");

            return view;
        }

        class ViewHolder {
            TextView rank;
            TextView userName;
            TextView points;
        }
    }

    private void loadLeaderboardData() {

        userScores.clear();

        FirebaseDatabase.getInstance().getReference().child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userScores.clear();

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            String firstName = userSnapshot.child("firstName").getValue(String.class);
                            String lastName = userSnapshot.child("lastName").getValue(String.class);
                            Integer points = userSnapshot.child("points").getValue(Integer.class);

                            if (firstName != null && lastName != null && points != null) {
                                userScores.add(new UserScore(userId, firstName + " " + lastName, points));
                            }
                        }

                        // Sorting players by tier reank
                        Collections.sort(userScores, new Comparator<UserScore>() {
                            @Override
                            public int compare(UserScore o1, UserScore o2) {
                                return o2.points.compareTo(o1.points);
                            }
                        });
                        //adapt to changing UI needs
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("LeaderboardFragment", "Error loading leaderboard data", databaseError.toException());
                    }
                });
    }

    // class to store user data
    private static class UserScore {
        String userId;
        String name;
        Integer points;

        UserScore(String userId, String name, Integer points) {
            this.userId = userId;
            this.name = name;
            this.points = points;
        }
    }
}