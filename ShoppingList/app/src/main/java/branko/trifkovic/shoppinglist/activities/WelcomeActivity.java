package branko.trifkovic.shoppinglist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import branko.trifkovic.shoppinglist.other.DbHelper;
import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.allListAdapter.AllShoppingListsElement;
import branko.trifkovic.shoppinglist.allListAdapter.CustomAdapterAllLists;
import branko.trifkovic.shoppinglist.other.HttpHelper;
import branko.trifkovic.shoppinglist.other.MyService;
import branko.trifkovic.shoppinglist.other.SharedUsername;


public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    Button seeMyListsButton, newListButton;
    ImageButton homeButtonWelcomeActivity;
    TextView usernameTextViewWelcome;
    SharedPreferences sharedPref;
    DbHelper dbHelper;
    CustomAdapterAllLists adapter;
    HttpHelper httpHelper = new HttpHelper();
    String owner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Set all variables
        Intent i = getIntent();
        Bundle b = i.getExtras();
        sharedPref  = getSharedPreferences("branko.trifkovic.shoppinglist.prefs", MODE_PRIVATE);

        usernameTextViewWelcome = findViewById(R.id.usernameTextViewWelcome);
        newListButton = findViewById(R.id.newListButton);
        seeMyListsButton = findViewById(R.id.seeMyListsButton);
        homeButtonWelcomeActivity = findViewById(R.id.homeButtonWelcomeActivity);

        newListButton.setOnClickListener(this);
        seeMyListsButton.setOnClickListener(this);
        homeButtonWelcomeActivity.setOnClickListener(this);


        // get username
        if(b != null) { // read from bundle if data sent
            String username = b.getString("username");
            usernameTextViewWelcome.setText(username);
        } else { // read from SharedPreferences if we came back to welcome acitvity
            String savedText = sharedPref.getString("username", "");
            usernameTextViewWelcome.setText(savedText);
        }
        owner = usernameTextViewWelcome.getText().toString();

        // Start sync DB and server
        Intent serviceIntent = new Intent(this, MyService.class);
        SharedUsername su = SharedUsername.getInstance();
        su.setSharedVariable(owner);
        startService(serviceIntent);

        // List initialization, loading adapter and linking adapter and list
        ListView list = findViewById(R.id.listAllShoppingLists);
        adapter = new CustomAdapterAllLists(this);
        list.setAdapter(adapter);

        // DB initialization and loading lists
        dbHelper = new DbHelper(this, getResources().getString(R.string.DB_NAME), null, 1);
        AllShoppingListsElement[] allShoppingLists = dbHelper.readAllLists(owner);

        adapter.update(allShoppingLists);


        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AllShoppingListsElement el = (AllShoppingListsElement) adapter.getItem(i);

                if(!el.getmOwner().equals(owner)) {
                    Toast.makeText(WelcomeActivity.this, "You can only delete your list!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (el.getmShared() == "true") {
                    deleteSharedList(el);
                }

                adapter.removeElement(el);
                dbHelper.deleteList(el.getmTitle());

                return false;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showList = new Intent(WelcomeActivity.this, ShowListActivity.class);

                Bundle b = new Bundle();
                AllShoppingListsElement sl = (AllShoppingListsElement) adapter.getItem(i);
                b.putString("listTitle", sl.getmTitle());
                b.putString("shared", sl.getmShared());
                showList.putExtras(b);

                startActivity(showList);
            }
        });

    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.newListButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.newListDialogMessage)
                    .setTitle(getString(R.string.newListDialogTitle))
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent newList = new Intent(WelcomeActivity.this, NewListActivity.class);
                            Bundle b = new Bundle();
                            b.putString("username", owner);
                            newList.putExtras(b);

                            startActivity(newList);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else if( view.getId() == R.id.seeMyListsButton && seeMyListsButton.getText().equals("SEE SHARED LISTS") ) {

            if (adapter.getCount() != 0)
                adapter.clear();

            loadSharedLists();

            seeMyListsButton.setText("SEE MY LISTS");

        } else if( view.getId() == R.id.seeMyListsButton && seeMyListsButton.getText().equals("SEE MY LISTS") ) {
            AllShoppingListsElement[] allShoppingLists = dbHelper.readAllLists(owner);
            adapter.update(allShoppingLists);

            seeMyListsButton.setText("SEE SHARED LISTS");

        } else if(view.getId() == R.id.homeButtonWelcomeActivity) {
            // Stop sync DB and server
            Intent intent = new Intent(this, MyService.class);
            stopService(intent);

            Intent home = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(home);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String textToSave = usernameTextViewWelcome.getText().toString();
        SharedPreferences.Editor editor = sharedPref .edit();
        editor.putString("username", textToSave);
        editor.apply();
    }

    private void loadSharedLists() {
        String URL = getResources().getString(R.string.GET_LISTS_URL);
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONArray arrayLists = httpHelper.getJSONArrayFromURL(URL);
                    for (int i = 0; i < arrayLists.length(); ++i) {
                        JSONObject obj = arrayLists.getJSONObject(i);
                        String listTitle = obj.getString("name");
                        String listCreator = obj.getString("creator");

                        runOnUiThread(new Runnable() {
                            public void run() {
                                AllShoppingListsElement elem = new AllShoppingListsElement(listTitle, "true", listCreator);
                                adapter.addElement(elem);
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void deleteSharedList(AllShoppingListsElement el) {
        String URL = getResources().getString(R.string.GET_LISTS_URL) + "/" + owner + "/" + el.getmTitle();

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (httpHelper.httpDelete(URL)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.removeElement(el);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "You can only delete your list!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}