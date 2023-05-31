package branko.trifkovic.shoppinglist.other;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import branko.trifkovic.shoppinglist.R;

// Service for syncing database and server
public class DataBaseSyncing extends Service {


    private boolean mRun = true;
    private String mUsername;
    private HttpHelper httpHelper = new HttpHelper();
    private DbHelper db = new DbHelper(this, "shared_list_app.db", null, 1);
    private String CHANNEL_ID = "1";
    NotificationManager notificationManager;
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedUsername su = SharedUsername.getInstance();
        mUsername = su.getSharedVariable();
        String URL = "http://192.168.1.16:3000/users/" + mUsername + "/lists";

        createNotificationChannel();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    try {
                        db.deleteListByUsername(mUsername);
                        JSONArray array = httpHelper.getJSONArrayFromURL(URL);

                        for(int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String name = obj.getString("name");
                            boolean shared = obj.getBoolean("shared");
                            String creator = obj.getString("creator");

                            db.addList(name, shared ? 1 : 0, creator);
                        }

                        notificationManager.notify(0, notification);

                        Thread.sleep(3000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ServiceTAG", "Service is starting");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRun = false;
        Log.d("ServiceTAG", "Service is stopped");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CH1";
            String description = "sync channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(DataBaseSyncing.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_shopping_cart_24)
                .setContentTitle("Shopping List")
                .setContentText("Database synchronized")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Database synchronized"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notification = builder.build();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}