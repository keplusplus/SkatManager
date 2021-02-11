package org.no_ip.dauerfeuer.skatmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class DataLoader {
    private final Context c;

    public DataLoader(Context c) {
        this.c = c;
    }

    public void refreshMainActivity() {
        MainActivityRefresher refresher = new MainActivityRefresher();
        refresher.execute();
    }

    private class MainActivityRefresher extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
            return null;
        }
    }
}
