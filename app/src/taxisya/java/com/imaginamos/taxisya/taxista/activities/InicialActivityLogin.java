package com.imaginamos.taxisya.taxista.activities;


import android.app.Activity;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import com.imaginamos.taxisya.taxista.R;
import com.crashlytics.android.Crashlytics;
import com.imaginamos.taxisya.taxista.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by TaxisYa on 14/10/16.
 */

public class InicialActivityLogin extends Activity implements OnClickListener  {


    private Button btnLogin;
    private Button btnRegister;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.USE_CRASHLYTICS)
            Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_inicial);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnRegister:
                Intent btnRegister = new Intent(this, RegisterDriverActivity.class);
                startActivity(btnRegister);
                break;

            case R.id.btnLogin:
                Intent btnLogin = new Intent(this, LoginActivity.class);
                startActivity(btnLogin);
                break;

        }
    }
}