package com.teajey.searchreminder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionChecker extends Service {
    private static final String NOTIFICATION_GROUP = "com.teajey.searchreminder.NOTIFICATION_GROUP";

    private final IBinder mBinder = new LocalBinder();

    private ConnectivityManager connectivityManager;
    private boolean wasConnected = true;
    private MainActivity mainActivity = null;

    public ConnectionChecker() {
    }

    public class LocalBinder extends Binder {
        ConnectionChecker getService() {
            // Return this instance of ConnectionChecker so clients can call public methods
            return ConnectionChecker.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Context context = this;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean wifiConnected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
                boolean mobileConnected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

                boolean waitForWifi = preferences.getBoolean("wait_for_wifi", false);

                boolean connected = (mobileConnected && !waitForWifi) || wifiConnected;

                if (connected && !wasConnected) {

                    ArrayList<SearchQuery> queryQueue = new ArrayList<>();
                    try {
                        FileInputStream inputStream = openFileInput("query_queue");
                        ObjectInputStream ois = new ObjectInputStream(inputStream);
                        //noinspection unchecked
                        queryQueue = (ArrayList<SearchQuery>) ois.readObject();
                        inputStream.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (queryQueue.size() == 0) return;

                    ArrayList<Notification> notifications = new ArrayList<>();
                    if (queryQueue.size() > 1) {
                        notifications.add(
                                new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_search_black_24dp)
                                        .setContentTitle("Search Reminder")
                                        .setContentText("Error: No search reminders displayed.")
                                        .setGroup(NOTIFICATION_GROUP)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setGroupSummary(true)
                                        .setAutoCancel(true)
                                        .build()
                        );
                    }

                    for (int i = 0; i < queryQueue.size(); i++) {
                        notifications.add(
                                createSearchReminderNotification(queryQueue, i)
                        );
                    }

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    for (int i = 0; i < notifications.size(); i++) {
                        notificationManager.notify(i, notifications.get(i));
                    }

                    queryQueue.clear();

                    if (mainActivity != null) {
                        mainActivity.writeToQueryQueueFile(queryQueue);
                    } else {
                        try {
                            FileOutputStream outputStream = openFileOutput("query_queue", Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                            oos.writeObject(queryQueue);
                            outputStream.close();
                            oos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }

                wasConnected = connected;
            }
        }, 0, 5000);
        return super.onStartCommand(intent, flags, startId);
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private Notification createSearchReminderNotification(ArrayList<SearchQuery> queryQueue, int index) {
        String searchEngine = queryQueue.get(index).getSearchEngine();
        String searchEngineNames[] = getResources().getStringArray(R.array.search_engines);
        int searchEngineCode = 0;
        for (int i = 0; i < searchEngineNames.length; i++) {
            if (searchEngineNames[i].equals(searchEngine)) {
                searchEngineCode = i;
                break;
            }
        }

        String url = getResources().getStringArray(R.array.search_engine_urls)[searchEngineCode];
        url = url.replace(' ', '_');
        url = "https://" + url + queryQueue.get(index).getQuery();

        Intent intent = new Intent(this, RedirectToWeb.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("url", url);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, index, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        return new NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .setContentTitle(queryQueue.get(index).getSearchEngine())
                .setContentText(queryQueue.get(index).getQuery())
                .setGroup(NOTIFICATION_GROUP)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }
}
