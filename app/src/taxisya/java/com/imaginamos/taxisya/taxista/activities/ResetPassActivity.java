package com.imaginamos.taxisya.taxista.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.io.MiddleConnect;
import com.imaginamos.taxisya.taxista.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

//import cz.msebera.android.httpclient.Header;

public class ResetPassActivity extends Activity implements View.OnClickListener {

    private EditText email;
    private EditText code;
    private EditText pass;
    private ImageButton send;
    private Button btnSend;
    private TextView msg;
    private ImageView bt_back;
    private ProgressDialog dialog;


    @SuppressLint("HandlerLeak")
    private Handler hand = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            // TODO Auto-generated method stub
            super.dispatchMessage(msg);

            switch (msg.what) {
                case 1:
                    dialog.show();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
                case 5:
                    setDialogSend();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

        setContentView(R.layout.activity_reset_pass);

        code = (EditText) findViewById(R.id.token);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.pass);
        bt_back = (ImageView) findViewById(R.id.btn_volver);
        bt_back.setOnClickListener(this);
        msg = (TextView) findViewById(R.id.msg_show);

        dialog = new ProgressDialog(ResetPassActivity.this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getString(R.string.enviando));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reset_pass, menu);
        return true;
    }

    @SuppressWarnings("unused")
    @Override
    public void onClick(final View v) {

//        if (v.getTag().toString().equals("1"))
        if (v.getTag().toString().equals("11"))

        {
            if (email.getText().toString().equals("")) {
                Toast.makeText(this, R.string.text_escribe_email,
                        Toast.LENGTH_LONG).show();
            } else {
                MiddleConnect.sendMailReset(getApplicationContext(), email.getText().toString(),
                        new AsyncHttpResponseHandler() {

                            @Override
                            public void onStart() {
                                super.onStart();

                                try {

                                    hand.sendEmptyMessage(1);

                                } catch (Exception e) {
                                }
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                //super.onSuccess(statusCode,headers,responseBody);
                                String arg1 = new String(responseBody);
                                //super.onSuccess(arg0, arg1);
                                Log.e("response", arg1);
                                try {

                                    JSONObject responsejson = new JSONObject(arg1);

                                    if (responsejson.getInt("error") == 0) {
                                        code.setVisibility(View.VISIBLE);
                                        email.setVisibility(View.GONE);
                                        msg.setVisibility(View.VISIBLE);
                                        pass.setVisibility(View.VISIBLE);
                                        v.setTag("2");
                                        hand.sendEmptyMessage(5);
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.texto_usuario_no_existe, Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.texto_usuario_no_existe,
                                            Toast.LENGTH_LONG).show();

                                }

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                // TODO Auto-generated method stub
                                //super.onFailure(arg0, arg1);
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_net, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFinish() {
                                // TODO Auto-generated method stub
                                super.onFinish();

                                hand.sendEmptyMessage(2);

                            }
                        });
            }

        } else if (v.getTag().toString().equals("2")) {
            if (code.getText().toString().equals("")
                    || pass.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), R.string.text_campos_imcompletos,
                        Toast.LENGTH_LONG).show();
            }

            String passmd5 = Utils.md5(pass
                    .getText().toString());

            Log.e("hola", passmd5);

            MiddleConnect.sendMailResetConfirm(getApplicationContext(), email
                    .getText().toString(), code.getText().toString(), passmd5, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    // TODO Auto-generated method stub
                    super.onStart();
                    try {
                        hand.sendEmptyMessage(1);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

                @Override
                public void onFinish() {
                    // TODO Auto-generated method stub
                    super.onFinish();
                    hand.sendEmptyMessage(2);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //super.onFailure(arg0, arg1);
                    Toast.makeText(getApplicationContext(),
                            R.string.error_net, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String arg1 = new String(responseBody);
                    // TODO Auto-generated method stub
                    //super.onSuccess(arg0, arg1);
                    Log.e("response", arg1);
                    try {

                        JSONObject responsejson = new JSONObject(arg1);

                        if (responsejson.getInt("error") == 0) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.text_contrasena_restablecida,
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    R.string.text_usuario_no_existe_codigo_incorrecto,
                                    Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                R.string.texto_usuario_no_existe,
                                Toast.LENGTH_LONG).show();

                    }

                }
            });
        } else {
            finish();
        }

    }

    private void setDialogSend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassActivity.this);
        builder.setMessage(R.string.type_code).setTitle(R.string.app_name);
        builder.setPositiveButton(getString(R.string.aceptar), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

