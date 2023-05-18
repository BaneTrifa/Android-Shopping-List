package branko.trifkovic.shoppinglist.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import branko.trifkovic.shoppinglist.other.DbHelper;
import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.other.HttpHelper;
import branko.trifkovic.shoppinglist.other.User;
import branko.trifkovic.shoppinglist.activities.WelcomeActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button registerSubmitButton;
    EditText usernameRegisterEditText;
    EditText passwordRegisterEditText;
    EditText emailRegisterEditText;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterForm.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        registerSubmitButton = view.findViewById(R.id.registerSubmitButton);
        usernameRegisterEditText = view.findViewById(R.id.usernameRegisterEditText);
        passwordRegisterEditText = view.findViewById(R.id.passwordRegisterEditText);
        emailRegisterEditText = view.findViewById(R.id.emailRegisterEditText);

        registerSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DbHelper db = new DbHelper(getActivity(), getResources().getString(R.string.DB_NAME), null, 1);
                String username = usernameRegisterEditText.getText().toString().trim();
                String password = passwordRegisterEditText.getText().toString().trim();
                String email = emailRegisterEditText.getText().toString().trim();


                if(checkIfUsernameIsEmpty(username) &&
                        checkIfPasswordIsEmpty(password) &&
                        validateEmail(email)) {

                    registerUser(username, password, email, db);

                }
            }
        });

        // Inflate the layout for this fragment
        return view;

    }

    private void clearEditTexts() {
        usernameRegisterEditText.setText("");
        passwordRegisterEditText.setText("");
        emailRegisterEditText.setText("");
    }

    //192.168.1.16
    // Validation methods
    private boolean checkIfUsernameIsEmpty(String username) {
        if(username.equals("")) {
            Toast.makeText(getActivity(), "Username box must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIfPasswordIsEmpty(String password) {
        if(password.equals("")) {
            Toast.makeText(getActivity(), "Password box must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validateEmail(String email) {

        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        } else {
            Toast.makeText(getActivity(), "Enter valid Email address !", Toast.LENGTH_SHORT).show();
            return false;
        }
    }




    // Register user on server
    private void registerUser(String username, String password, String email, DbHelper db) {
        HttpHelper httpHelper = new HttpHelper();

        String URL = getResources().getString(R.string.POST_REGISTER_URL);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject httpBody = new JSONObject();
                    httpBody.put("username", username);
                    httpBody.put("password", password);
                    httpBody.put("email", email);

                    if (httpHelper.postJSONObjectFromURL(URL, httpBody)) {
                        dbRegisterUser(username, password, email, db);
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), "User with that username already exist!", Toast.LENGTH_SHORT).show();
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

    // Register user on local database
    private void dbRegisterUser(String username, String password, String email, DbHelper db) {
        if (db.registerUser(new User(username, email, password)) != -1) {
            Intent welcome = new Intent(getActivity(), WelcomeActivity.class);
            Bundle b = new Bundle();
            b.putString("username", username);
            welcome.putExtras(b);

            clearEditTexts();

            startActivity(welcome);
        } else {
            Toast.makeText(getActivity(), "User with that username already exist!", Toast.LENGTH_SHORT).show();
        }
    }

}