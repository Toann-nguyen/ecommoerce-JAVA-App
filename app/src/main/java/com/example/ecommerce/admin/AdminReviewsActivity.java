package com.example.ecommerce.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecommerce.R;
import com.example.ecommerce.fragments.AdminProductReviewsFragment;
import com.example.ecommerce.fragments.AdminRecentReviewsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminReviewsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        // Check if user is admin
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem trang này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // TODO: Implement proper admin check here

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
                    tab.setText("Mới nhất");
                    break;
                case 1:
                    tab.setText("Theo sản phẩm");
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
                    // Show all recent reviews with delete option
                    return new AdminRecentReviewsFragment();
                case 1:
                    // Show products that can be selected to see their reviews
                    return new AdminProductReviewsFragment();
                default:
                    return new AdminRecentReviewsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Two tabs
        }
    }
}