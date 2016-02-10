package com.newsapps.newstamil.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.newsapps.newstamil.OnlineActivity;
import com.newsapps.newstamil.R;

public class SmallWidgetProvider extends AppWidgetProvider {
	private final String TAG = this.getClass().getSimpleName();

	public static final String ACTION_REQUEST_UPDATE = "com.newsapps.newstamil.WIDGET_FORCE_UPDATE";
    public static final String ACTION_UPDATE_RESULT = "com.newsapps.newstamil.WIDGET_UPDATE_RESULT";

    @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.d(TAG, "onUpdate");

        Intent intent = new Intent(context, OnlineActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        views.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);

        Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        context.startService(serviceIntent);
	}


	@Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "onReceive");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SmallWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		if (ACTION_REQUEST_UPDATE.equals(intent.getAction())) {
            //Log.d(TAG, "onReceive: got update request");

		    onUpdate(context, appWidgetManager, appWidgetIds);

		} else if (ACTION_UPDATE_RESULT.equals(intent.getAction())) {
            int unread = intent.getIntExtra("unread", -1);
            int resultCode = intent.getIntExtra("resultCode", WidgetUpdateService.UPDATE_RESULT_ERROR_OTHER);

            //Log.d(TAG, "onReceive: got update result from service: " + unread + " " + resultCode);

            updateWidgetsText(context, appWidgetManager, appWidgetIds, unread, resultCode);
        } else {
            super.onReceive(context, intent);
        }
	}

    private void updateWidgetsText(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int unread, int resultCode) {

        Intent intent = new Intent(context, OnlineActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        views.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

        String viewText;

        switch (resultCode) {
            case WidgetUpdateService.UPDATE_RESULT_OK:
                viewText = String.valueOf(unread);
                break;
            case WidgetUpdateService.UPDATE_IN_PROGRESS:
                viewText = "...";
                break;
            default:
                viewText = "?";
        }

        views.setTextViewText(R.id.widget_unread_counter, viewText);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

}
