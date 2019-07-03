package com.teajey.searchreminder;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements
        SettingsFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        QueueFragment.OnFragmentInteractionListener {
    public static final String NOTIFICATION_CHANNEL_ID = "search_reminder_notif_channel";

    public static boolean active = false;

    ConnectionChecker mConnectionChecker;

    private final MainActivity context = this;

    private ViewPager viewPager;
    private BottomNavigationView navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewPager = findViewById(R.id.fragmentView);
        navigator = findViewById(R.id.navigation);

        navigator.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_settings:
                                viewPager.setCurrentItem(0);
                                return true;
                            case R.id.navigation_search:
                                viewPager.setCurrentItem(1);
                                return true;
                            case R.id.navigation_queue:
                                viewPager.setCurrentItem(2);
                                return true;
                        }
                        return false;
                    }
                }
        );
        navigator.getMenu().getItem(1).setChecked(true);

        viewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}

                    @Override
                    public void onPageSelected(int position) {
                        navigator.getMenu().getItem(position).setChecked(true);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state){}
                }
        );

        setupViewPager(viewPager);
        viewPager.setCurrentItem(1);

        createNotificationChannel();
        Intent connectionCheckerIntent = new Intent(this, ConnectionChecker.class);
        startService(connectionCheckerIntent);
        getApplicationContext().bindService(connectionCheckerIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public ArrayList<SearchQuery> getSearchQueryQueue() {
        return readQueryQueueFile();
    }

    public ArrayList<SearchQuery> readQueryQueueFile() {
        ArrayList<SearchQuery> sqq;
        try {
            FileInputStream inputStream = openFileInput("query_queue");
            try {
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                //noinspection unchecked
                sqq = (ArrayList<SearchQuery>) ois.readObject();
                inputStream.close();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                sqq = null;
            }
        } catch (FileNotFoundException fnfe) {
            sqq = writeToQueryQueueFile(new ArrayList<SearchQuery>());
        }

        return sqq;
    }

    public ArrayList<SearchQuery> writeToQueryQueueFile(ArrayList<SearchQuery> sqq) {
        try {
            FileOutputStream outputStream = openFileOutput("query_queue", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(sqq);
            outputStream.close();
            oos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return sqq;
    }

    private void setupViewPager(ViewPager vp) {
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(SettingsFragment.newInstance(), "Settings");
        adapter.addFragment(SearchFragment.newInstance(), "Search");
        adapter.addFragment(QueueFragment.newInstance(), "Queue");
        vp.setAdapter(adapter);
    }

    /*@Override
    public void onFragmentInteraction(Uri uri) {}*/

    public void addSearchQueryToList(SearchQuery sq) {
        ArrayList<SearchQuery> newQueryQueue = readQueryQueueFile();
        newQueryQueue.add(sq);
        writeToQueryQueueFile(newQueryQueue);
    }

    public void searchButtonPressed(View view) {
        addSearchQuery();
    }

    public void addSearchQuery() {
        EditText queryTextBox = findViewById(R.id.queryBox);
        Spinner searchEngineSpinner = findViewById(R.id.searchEngineSpinner);

        boolean emptyQuery = Pattern.matches("^\\s*$", queryTextBox.getText().toString());
        if (!emptyQuery) {
            this.addSearchQueryToList(new SearchQuery(queryTextBox.getText().toString(), searchEngineSpinner.getSelectedItem().toString()));

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            queryTextBox.setText("");
        }

        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                SearchQuery editedSq = (SearchQuery) data.getSerializableExtra(QueueRecyclerViewAdapter.SEARCH_QUERY);
                int index = data.getIntExtra(QueueRecyclerViewAdapter.QUERY_INDEX, 0);

                ArrayList<SearchQuery> newQueryQueue = readQueryQueueFile();
                newQueryQueue.set(index, editedSq);
                writeToQueryQueueFile(newQueryQueue);
            }
            else if (resultCode == QueryConfigActivity.RESULT_DELETE) {
                int index = data.getIntExtra(QueueRecyclerViewAdapter.QUERY_INDEX, 0);

                ArrayList<SearchQuery> newQueryQueue = readQueryQueueFile();
                newQueryQueue.remove(index);
                writeToQueryQueueFile(newQueryQueue);
            }
        }
        viewPager.getAdapter().notifyDataSetChanged();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ConnectionChecker.LocalBinder binder = (ConnectionChecker.LocalBinder) service;
            mConnectionChecker = binder.getService();
            mConnectionChecker.setMainActivity(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mConnectionChecker.setMainActivity(null);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }
}
