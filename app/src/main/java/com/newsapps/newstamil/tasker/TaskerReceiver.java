package com.newsapps.newstamil.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.newsapps.newstamil.ApiRequest;
import com.newsapps.newstamil.offline.OfflineDownloadService;
import com.newsapps.newstamil.offline.OfflineUploadService;
import com.newsapps.newstamil.util.SimpleLoginManager;

public class TaskerReceiver extends BroadcastReceiver {
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "Got action: " + intent.getAction());
		
		final Context fContext = context;
		if (com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
			
			final Bundle settings = intent.getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
			final int actionId = settings != null ? settings.getInt("actionId", -1) : -1;
			
			//Log.d(TAG, "received action id=" + actionId);
			
			SimpleLoginManager loginMgr = new SimpleLoginManager() {
				
				@Override
				protected void onLoginSuccess(int requestId, String sessionId, int apiLevel) {

					switch (actionId) {
					case TaskerSettingsActivity.ACTION_DOWNLOAD:
						if (true) {
							Intent intent = new Intent(fContext,
									OfflineDownloadService.class);
							intent.putExtra("sessionId", sessionId);
							intent.putExtra("batchMode", true);
	
							fContext.startService(intent);
						}
						break;
					case TaskerSettingsActivity.ACTION_UPLOAD:
						if (true) {
							Intent intent = new Intent(fContext,
									OfflineUploadService.class);
							intent.putExtra("sessionId", sessionId);
							intent.putExtra("batchMode", true);
	
							fContext.startService(intent);
						}						
						break;
					default:
						//Log.d(TAG, "unknown action id=" + actionId);
					}					
				}
				
				@Override
				protected void onLoginFailed(int requestId, ApiRequest ar) {
					Toast toast = Toast.makeText(fContext, fContext.getString(ar.getErrorMessage()), Toast.LENGTH_SHORT);
					toast.show();
				}
				
				@Override
				protected void onLoggingIn(int requestId) {
					//
				}
			};
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			String login = prefs.getString("login", "").trim();
			String password = prefs.getString("password", "").trim();
			String serverUrl = prefs.getString("server_url", "").trim();
			ApiRequest.trustAllHosts(prefs.getBoolean("ssl_trust_any", false), prefs.getBoolean("ssl_trust_any_host", false));
			
			if (serverUrl.equals("")) {
				Toast toast = Toast.makeText(fContext, "Could not download articles: not configured?", Toast.LENGTH_SHORT);
				toast.show();
			} else {				
				loginMgr.logIn(context, 1, login, password);
			}			
		}
	}

}
