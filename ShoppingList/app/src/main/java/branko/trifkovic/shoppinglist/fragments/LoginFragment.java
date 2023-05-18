package branko.trifkovic.shoppinglist.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
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
import branko.trifkovic.shoppinglist.activities.WelcomeActivity;
import branko.trifkovic.shoppinglist.other.HttpHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginSubmitButton = view.findViewById(R.id.loginSubmitButton);
        EditText usernameLoginEditText = view.findViewById(R.id.usernameLoginEditText);
        EditText passwordLoginEditText = view.findViewById(R.id.passwordLoginEditText);

        loginSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HttpHelper httpHelper = new HttpHelper();

                String URL = getResources().getString(R.string.POST_LOGIN_URL);
                String username = usernameLoginEditText.getText().toString().trim();
                String password = passwordLoginEditText.getText().toString().trim();

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            JSONObject httpBody = new JSONObject();
                            httpBody.put("username", username);
                            httpBody.put("password", password);

                            if( httpHelper.postJSONObjectFromURL(URL, httpBody) ) {
                                Intent welcome = new Intent(getActivity(), WelcomeActivity.class);

                                Bundle b = new Bundle();
                                b.putString("username", username);
                                welcome.putExtras(b);

                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        clearEditTexts();
                                    }
                                });

                                startActivity(welcome);

                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        clearEditTexts();
                                        Toast.makeText(getActivity(), "Incorrect username or password!", Toast.LENGTH_SHORT).show();
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

            private void clearEditTexts() {
                usernameLoginEditText.setText("");
                passwordLoginEditText.setText("");
            }
        });

        return view;
    }
}