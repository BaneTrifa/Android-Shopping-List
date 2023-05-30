package branko.trifkovic.shoppinglist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import branko.trifkovic.shoppinglist.other.DbHelper;
import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.itemListAdapter.CustomAdapterOneList;
import branko.trifkovic.shoppinglist.itemListAdapter.OneShoppingListElement;
import branko.trifkovic.shoppinglist.other.HttpHelper;
import branko.trifkovic.shoppinglist.other.MyService;

public class ShowListActivity extends AppCompatActivity implements View.OnClickListener {

    public TextView listTitle;
    public EditText taskToAddInListEditText;
    public Button addTaskInListButton;
    public ImageButton refreshButton;
    public ImageButton homeButtonShowListActivity;
    boolean sharedList;
    HttpHelper httpHelper = new HttpHelper();
    CustomAdapterOneList adapter;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        listTitle = findViewById(R.id.listTitle);
        taskToAddInListEditText = findViewById(R.id.taskToAddInListEditText);
        addTaskInListButton = findViewById(R.id.addTaskInListButton);
        refreshButton = findViewById(R.id.refreshButton);
        homeButtonShowListActivity = findViewById(R.id.homeButtonShowListActivity);

        Intent i = getIntent();
        Bundle b = i.getExtras();

        if(b != null) {
            listTitle.setText(b.getString("listTitle"));
            String shared = b.getString("shared");
            sharedList = shared.equals("true");
        }

        ListView list = findViewById(R.id.listOneShoppingList);
        dbHelper = new DbHelper(this, getResources().getString(R.string.DB_NAME), null, 1);
        adapter = new CustomAdapterOneList(this);
        list.setAdapter(adapter);

        if(!sharedList) {
            refreshButton.setVisibility(View.INVISIBLE);
            OneShoppingListElement[] allItems = dbHelper.readItems(listTitle.getText().toString());
            adapter.update(allItems);
        } else {
            readSharedTasks(listTitle.getText().toString());
        }

        refreshButton.setOnClickListener(this);
        addTaskInListButton.setOnClickListener(this);
        homeButtonShowListActivity.setOnClickListener(this);


        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                OneShoppingListElement el = (OneShoppingListElement) adapter.getItem(i);
                if(sharedList) {
                    removeSharedTask(el);
                    return false;
                } else {
                    adapter.removeElement(el);
                    dbHelper.deleteItem(el.getId());
                    return false;
                }

            }
        });
    }


    // GET request - getting tasks from list listName from server
    private void readSharedTasks(String listName) {
        String URL = getResources().getString(R.string.GET_ITEMS_URL) + "/" + listName;

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONArray array = httpHelper.getJSONArrayFromURL(URL);

                    for(int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        ShowListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    String taskName = obj.getString("name");
                                    boolean done = obj.getBoolean("done");
                                    String id = obj.getString("taskId");
                                    OneShoppingListElement elem = new OneShoppingListElement(taskName, done, id);
                                    adapter.addElement(elem);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                            }
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

    // POST request - adding tasks to list on server
    private void addSharedTask(OneShoppingListElement el) {
        String URL = getResources().getString(R.string.POST_CREATE_ITEM_URL);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject httpBody = new JSONObject();
                    httpBody.put("name", el.getTask());
                    httpBody.put("list", listTitle.getText().toString());
                    httpBody.put("done", false);
                    httpBody.put("taskId", el.getId());

                    if (httpHelper.postJSONObjectFromURL(URL, httpBody)) {

                        ShowListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.addElement(el);
                                taskToAddInListEditText.setText("");
                            }
                        });

                    } else {
                        ShowListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(ShowListActivity.this, "Connection problem", Toast.LENGTH_SHORT).show();
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

    // DELETE request - remove tasks from list on server
    private void removeSharedTask(OneShoppingListElement el) {
        String URL = getResources().getString(R.string.DELETE_ITEM_URL) + "/" + el.getId();

        new Thread(new Runnable() {
            public void run() {
                try {

                    if (httpHelper.httpDelete(URL)) {

                        ShowListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.removeElement(el);
                            }
                        });

                    } else {
                        ShowListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(ShowListActivity.this, "Connection problem", Toast.LENGTH_SHORT).show();
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



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.addTaskInListButton) {
            String taskName = taskToAddInListEditText.getText().toString();

            OneShoppingListElement el = new OneShoppingListElement(taskName, false, UUID.randomUUID().toString());
            if(!taskName.equals("")) {
                if(sharedList) {
                    addSharedTask(el);
                } else {
                    adapter.addElement(el);
                    taskToAddInListEditText.setText("");

                    dbHelper.addItem(el, listTitle.getText().toString());
                }
            }
        } else if(view.getId() == R.id.refreshButton) {
            if(adapter.getCount() != 0) adapter.clear();

            readSharedTasks(listTitle.getText().toString());
        } else if(view.getId() == R.id.homeButtonShowListActivity) {
            // Stop sync DB and server
            Intent intent = new Intent(this, MyService.class);
            stopService(intent);

            Intent home = new Intent(ShowListActivity.this, MainActivity.class);
            startActivity(home);
        }

    }
}