package com.newsapps.newstamil.tasker;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ProgressBar;

public class LoadingTask extends AsyncTask<Void, Integer, Boolean> {


    public interface LoadingTaskFinishedListener {
        void onTaskFinished(boolean success); // If you want to pass something back to the listener add a param to this method
    }

	private final Activity mActivity;
    // This is the progress bar you want to update while the task is in progress
    private final ProgressBar progressBar;
    // This is the listener that will be told when this task is finished
    private final LoadingTaskFinishedListener finishedListener;

    public LoadingTask(Activity mActivity, ProgressBar progressBar, LoadingTaskFinishedListener finishedListener) {
        this.progressBar = progressBar;
        this.finishedListener = finishedListener;
        this.mActivity = mActivity;
    }
 
    @Override
    protected Boolean doInBackground(Void... params) {
        if(checkRegistrationStatus()){
            initializeApplication();
        }
        // Perhaps you want to return something to your post execute
        return true;
    }
 
    private boolean checkRegistrationStatus() {
        // Here you would query your app's internal state to see if this download had been performed before
        // Perhaps once checked save this in a shared preference for speed of access next time
        return true; // returning true so we show the splash every time
    }
 
 
    private void initializeApplication() {
        // We are just imitating some process thats takes a bit of time (loading of resources / downloading)

        int count = 10;
        for (int i = 0; i < count; i++) {
        	
            // Update the progress bar after every step
            int progress = (int) ((i / (float) count) * 100);
            publishProgress(progress);
 
            // Do some long loading things
            try { Thread.sleep(50); } catch (InterruptedException ignore) {}
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressBar.setProgress(values[0]); // This is ran on the UI thread so it is ok to update our progress bar ( a UI view ) here
    }
 
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        finishedListener.onTaskFinished(result); // Tell whoever was listening we have finished
    }

}