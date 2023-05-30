package branko.trifkovic.shoppinglist.other;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationBuilderWithBuilderAccessor;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.activities.MainActivity;
import branko.trifkovic.shoppinglist.activities.ShowListActivity;
import branko.trifkovic.shoppinglist.activities.WelcomeActivity;
import branko.trifkovic.shoppinglist.allListAdapter.AllShoppingListsElement;
import branko.trifkovic.shoppinglist.itemListAdapter.OneShoppingListElement;

public class MyService extends Service {


    private boolean mRun = true;
    private String mUsername;
    private HttpHelper httpHelper = new HttpHelper();
    private DbHelper db = new DbHelper(this, "shared_list_app.db", null, 1);
    private String CHANNEL_ID = "1";
    NotificationManagerCompat notificationManager;
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedUsername su = SharedUsername.getInstance();
        mUsername = su.getSharedVariable();
        String URL = "http://192.168.1.16:3000/users/" + mUsername + "/lists";

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    Log.d("ServiceTAG", "Hello " + mUsername + " from service thread");
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

                        createNotificationChannel();
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.baseline_home_24)
                                .setContentTitle("My notification")
                                .setContentText("Much longer text that cannot fit one line...")
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("Much longer text that cannot fit one line..."))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        notification = builder.build();
                        notificationManager = NotificationManagerCompat.from(MyService.this);

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


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
    }
}