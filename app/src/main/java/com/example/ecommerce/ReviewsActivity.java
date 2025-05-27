package com.example.ecommerce;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecommerce.fragments.ReviewsListFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ReviewsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        initViews();
        setupToolbar();
        setupViewPager();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        ReviewPagerAdapter pagerAdapter = new ReviewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect the TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Gần đây");
                    break;
                case 1:
                    tab.setText("Của tôi");
                    break;
            }
        }).attach();
    }

    // Adapter for the ViewPager
    private static class ReviewPagerAdapter extends FragmentStateAdapter {

        public ReviewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Create fragment for each tab
            switch (position) {
                case 0:
                    // Show all recent reviews
                    return ReviewsListFragment.newInstance(ReviewsListFragment.TYPE_RECENT);
                case 1:
                    // Show only user's reviews
                    return ReviewsListFragment.newInstance(ReviewsListFragment.TYPE_MY_REVIEWS);
                default:
                    return ReviewsListFragment.newInstance(ReviewsListFragment.TYPE_RECENT);
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Two tabs
        }
    }
}