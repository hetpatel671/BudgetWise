package com.budgetwise.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.budgetwise.MainActivity;
import com.budgetwise.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity for the onboarding process when a user first installs the app
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnSkip;
    private TextView tvTitle;
    private TextView tvDescription;
    
    private final String[] titles = {
        "Welcome to BudgetWise",
        "Track Your Expenses",
        "Set Budgets",
        "AI-Powered Insights",
        "Get Started"
    };
    
    private final String[] descriptions = {
        "Your personal finance assistant to help you manage your money wisely.",
        "Easily record and categorize your expenses to see where your money goes.",
        "Create budgets for different categories and track your spending against them.",
        "Get personalized insights and recommendations to improve your financial health.",
        "You're all set! Start your journey to financial freedom today."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        
        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        
        // Set up the ViewPager
        viewPager.setAdapter(new OnboardingAdapter(this));
        
        // Set up the TabLayout with ViewPager
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // No text for tabs
        }).attach();
        
        // Update the title and description when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvTitle.setText(titles[position]);
                tvDescription.setText(descriptions[position]);
                
                // Show "Get Started" button on the last page
                if (position == titles.length - 1) {
                    btnNext.setText(R.string.get_started);
                } else {
                    btnNext.setText(R.string.next);
                }
            }
        });
        
        // Set up button click listeners
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == titles.length - 1) {
                // Last page, start the main activity
                finishOnboarding();
            } else {
                // Go to next page
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
        
        btnSkip.setOnClickListener(v -> finishOnboarding());
    }
    
    private void finishOnboarding() {
        // Save that onboarding is completed
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply();
        
        // Start the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}