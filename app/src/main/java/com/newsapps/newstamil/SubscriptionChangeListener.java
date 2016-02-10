package com.newsapps.newstamil;

public interface SubscriptionChangeListener {
    public void onSubscriptionChanged(int sum);

    public void expandGroupEvent(int groupPosition, boolean isExpanded);
}
