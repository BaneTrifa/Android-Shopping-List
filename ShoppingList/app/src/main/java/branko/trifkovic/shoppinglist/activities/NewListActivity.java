package branko.trifkovic.shoppinglist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import branko.trifkovic.shoppinglist.other.DbHelper;
import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.other.HttpHelper;
import branko.trifkovic.shoppinglist.other.MyService;


public class NewListActivity extends AppCompatActivity implements View.OnClickListener {

    Button saveButton, okButton;
    ImageButton homeButtonNewListActivity;
    EditText listTitleEditText;
    TextView listTitleTextView;
    RadioButton yesRadioButton, noRadioButton;
    String owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        owner = b.getString("username");

        saveButton = findViewById(R.id.saveButton);
        okButton = findViewById(R.id.okButton);
        homeButtonNewListActivity = findViewById(R.id.homeButtonNewListActivity);

        listTitleEditText = findViewById(R.id.listTitleEditText);
        listTitleTextView = findViewById(R.id.listTitleTextView);

        yesRadioButton = findViewById(R.id.yesRadioButton);
        noRadioButton = findViewById(R.id.noRadioButton);


        saveButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        homeButtonNewListActivity.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if( view.getId() == R.id.saveButton && checkTextView() && checkRadioButtonsAndTitle() && addList() ) {

            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);

        } else if(view.getId() == R.id.okButton) {
            String title = listTitleEditText.getText().toString();
            listTitleTextView.setText(title);

        } else if (view.getId() == R.id.homeButtonNewListActivity) {
            // Stop sync DB and server
            Intent intent = new Intent(this, MyService.class);
            stopService(intent);

            Intent home = new Intent(NewListActivity.this, MainActivity.class);
            startActivity(home);
        }
    }


    // Validation methods
    private boolean checkTextView() {
        if(listTitleTextView.getText().toString().equals("TITLE")) {
            Toast.makeText(this, "Please submit list title on OK button", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean checkRadioButtonsAndTitle() {
        if ( (yesRadioButton.isChecked() || noRadioButton.isChecked()) && !listTitleEditText.getText().toString().isEmpty() )
            return true;
        else {
            Toast.makeText(this, "Please fill all informations", Toast.LENGTH_SHORT).show();
            return false;
        }
    }



    // Add list on local database
    private boolean addList() {
        DbHelper db = new DbHelper(this, getResources().getString(R.string.DB_NAME), null, 1);

        String title = listTitleTextView.getText().toString().trim();
        int shared = yesRadioButton.isChecked() ? 1 : 0;

        if (db.addList(title, shared, owner) != -1) {
            if (shared == 1) addSharedListToServer(owner, title);
            return true;
        } else {
            Toast.makeText(this, "List with that title already exist", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Add shared list on server
    private void addSharedListToServer(String owner, String title) {
        HttpHelper httpHelper = new HttpHelper();
        String URL = getResources().getString(R.string.POST_NEW_LIST_URL);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject httpBody= new JSONObject();
                    httpBody.put("name", title);
                    httpBody.put("creator", owner);
                    httpBody.put("shared", true);

                    if(httpHelper.postJSONObjectFromURL(URL, httpBody)) {
                        Intent welcome = new Intent(NewListActivity.this, WelcomeActivity.class);
                        startActivity(welcome);
                    } else {
                        NewListActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(NewListActivity.this, "Connection problem", Toast.LENGTH_SHORT).show();
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