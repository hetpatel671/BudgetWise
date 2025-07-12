package com.budgetwise.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.R;

/**
 * Adapter for the onboarding ViewPager
 */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final Context context;
    private final int[] imageResources = {
            R.drawable.ic_dashboard_24,
            R.drawable.ic_transactions_24,
            R.drawable.ic_analytics_24,
            R.drawable.ic_notification,
            R.drawable.ic_add_24
    };

    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.imageView.setImageResource(imageResources[position]);
    }

    @Override
    public int getItemCount() {
        return imageResources.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}