package branko.trifkovic.shoppinglist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.fragments.LoginFragment;
import branko.trifkovic.shoppinglist.fragments.RegisterFragment;
import branko.trifkovic.shoppinglist.other.MyService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginFormButton, registerFormButton;
    LinearLayout startLayout;
    FrameLayout fragmentLayout;

    LoginFragment loginForm;
    RegisterFragment registerForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginFormButton = findViewById(R.id.loginFormButton);
        registerFormButton = findViewById(R.id.registerFormButton);

        startLayout = findViewById(R.id.startLayout);
        fragmentLayout = findViewById(R.id.frameLayout1);

        loginForm = LoginFragment.newInstance("param1", "param2");
        registerForm = RegisterFragment.newInstance("param1", "param2");


        loginFormButton.setOnClickListener(this);
        registerFormButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        startLayout.setVisibility(View.INVISIBLE);

        if(view.getId() == R.id.loginFormButton) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frameLayout1, loginForm)
                    .addToBackStack(null)
                    .commit();


        } else if(view.getId() == R.id.registerFormButton) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frameLayout1, registerForm)
                    .addToBackStack(null)
                    .commit();

        }
    }

    @Override
    public void onBackPressed() {
        if(startLayout.getVisibility() == View.VISIBLE)
            super.onBackPressed();
        else {
            startLayout.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        }
    }

}