package com.example.campusexplorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.example.campusexplorer.LoginFragment;
import com.example.campusexplorer.SignupFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Authen
        mAuth = FirebaseAuth.getInstance();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        setupViewPager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in and verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // if yesm go to main activity ashboard
            startActivity(new Intent(this, MainDashboardActivity.class));
            finish();
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new LoginFragment());
        adapter.addFragment(new SignupFragment());
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Login");
                    } else {
                        tab.setText("Sign Up");
                    }
                }).attach();
    }

    // ViewPager adapter class
    private class ViewPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        private final Fragment[] fragments = new Fragment[2];

        public void addFragment(Fragment fragment) {
            if (fragments[0] == null) {
                fragments[0] = fragment;
            } else {
                fragments[1] = fragment;
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }
    }
}