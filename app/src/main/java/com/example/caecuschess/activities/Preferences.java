package com.example.caecuschess.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.example.caecuschess.CaecusChessApp;
import com.example.caecuschess.R;
import com.example.caecuschess.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Preferences extends PreferenceActivity {
    private static int currentItem = -1;
    private static int initialItem = -1;

    public static class Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            if (v == null)
                return null;

            final ListView lv = v.findViewById(android.R.id.list);
            if (lv != null) {
                lv.setOnScrollListener(new OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {
                        currentItem = firstVisibleItem;
                    }
                });
                lv.post(() -> {
                    if (initialItem >= 0)
                        lv.setSelection(initialItem);
                });
            }

            return v;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        initialItem = settings.getInt("prefsViewInitialItem", -1);
        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment())
                            .commit();
        Util.setFullScreenMode(this, settings);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CaecusChessApp.setLanguage(newBase, false));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = settings.edit();
        editor.putInt("prefsViewInitialItem", currentItem);
        editor.apply();
    }


    public interface ConfigChangedListener {
        void onConfigurationChanged(Configuration newConfig);
    }

    private Set<ConfigChangedListener> configChangedListeners = new HashSet<>();

    public void addRemoveConfigChangedListener(ConfigChangedListener listener, boolean add) {
        if (add) {
            configChangedListeners.add(listener);
        } else {
            configChangedListeners.remove(listener);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (ConfigChangedListener cl : configChangedListeners)
            cl.onConfigurationChanged(newConfig);
    }

    public interface ActivityHandler {
        void handleResult(int resultCode, Intent data);
    }

    private int nextRequestCode = 129866295;
    private Map<Integer, ActivityHandler> handlers = new HashMap<>();

    /** Start an activity and invoke handler when the activity finishes. */
    public void runActivity(Intent data, ActivityHandler handler) {
        int requestCode = nextRequestCode++;
        startActivityForResult(data, requestCode);
        handlers.put(requestCode, handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityHandler handler = handlers.get(requestCode);
        if (handler != null) {
            handlers.remove(requestCode);
            handler.handleResult(resultCode, data);
        }
    }
}
