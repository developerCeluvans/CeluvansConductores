package com.imaginamos.taxisya.taxista.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.model.Conf;
import com.imaginamos.taxisya.taxista.model.Servicio;
import com.imaginamos.taxisya.taxista.io.MiddleConnect;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class HistoricActivity extends Activity implements OnClickListener {

    private ViewGroup mContainerView;
    private ImageView volver;
    private ProgressDialog pDialog;
    private String driver_id;
    private String respuesta;
    private ArrayList<Servicio> servicios;
    private String uuid;
    private Conf conf;

    private Handler hand = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String full_dir = "";

            Servicio s = (Servicio) msg.obj;

            if (s.getDireccion() != null) {
                full_dir = s.getDireccion();
            } else {
                full_dir = full_dir.concat(s.getIndice() + " " + s.getComp1() + "- " + s.getComp2() + " # " + s.getNumero());
            }
            addItem(s.getIdServicio(), full_dir, s.getBarrio(), s.getRat(), s.getFecha());

        }
    };

    @Override
    public void onRestart() {

        super.onRestart();
        overridePendingTransition(R.anim.hold, R.anim.pull_out_to_right);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("onCreate", "HistoricActivity");

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

        setContentView(R.layout.activity_historic);

        volver = (ImageView) findViewById(R.id.btn_volver);

        volver.setOnClickListener(this);

        conf = new Conf(this);

        driver_id = conf.getIdUser();

        uuid = conf.getUuid();

        servicios = new ArrayList<Servicio>();

        mContainerView = (ViewGroup) findViewById(R.id.mis_servicios);

        loadServices();

    }

    @Override
    public void onDestroy() {
        Log.v("onDestroy", "HistoricActivity");
        super.onDestroy();
    }

    private void addItem(String num, String dir, String block, String rat, String fecha) {

        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_historic, mContainerView, false);

        ((TextView) newView.findViewById(R.id.num_service)).setText(getString(R.string.historico_numero) + num);
        ((TextView) newView.findViewById(R.id.dir_service)).setText(dir);
        ((TextView) newView.findViewById(R.id.block_service)).setText(getString(R.string.historico_barrio) + block);
        ((TextView) newView.findViewById(R.id.dates)).setText(getString(R.string.historico_fecha) + fecha);

        if (rat.equals("null") || rat == null) {

            ((TextView) newView.findViewById(R.id.rat)).setText(R.string.historico_calificacion_ninguna);

        } else {

            switch (Integer.parseInt(rat)) {
                case 1:
                    rat = getString(R.string.historico_calificacion_muy_buena);
                    break;

                case 2:
                    rat = getString(R.string.historico_calificacion_buena);
                    break;
                case 3:
                    rat = getString(R.string.historico_calificacion_mala);
                    break;
                default:
                    rat = getString(R.string.historico_calificacion_nada);
                    break;
            }

            ((TextView) newView.findViewById(R.id.rat)).setText(getString(R.string.historico_calificacion_prefijo) + rat);
        }

        mContainerView.addView(newView, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View arg0) {

        switch (arg0.getId()) {

            case R.id.btn_volver:
                finish();
                break;

        }

    }

    private void loadServices() {

        MiddleConnect.loadHistory(this, driver_id, uuid, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                pDialog = new ProgressDialog(HistoricActivity.this);
                pDialog.setMessage(getString(R.string.historico_titulo_dialogo));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);

                Log.e("HISTORIAL", response + "");
                try {

                    respuesta = response;

                    convertirDatos();

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            int i = 0;

                            while (i < servicios.size()
                                    && !isFinishing()) {
                                try {

                                    Message msg = new Message();
                                    msg.obj = servicios.get(i);
                                    hand.sendMessage(msg);
                                    Thread.sleep(800);
                                } catch (InterruptedException e) {
                                }
                                i++;
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    err_services();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                err_services();
            }

            @Override
            public void onFinish() {
                try {
                    pDialog.dismiss();
                } catch (Exception e) {

                }
            }
        });
    }

    private void err_services() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(), getString(R.string.error_net), Toast.LENGTH_SHORT).show();
    }

    private void convertirDatos() {

        try {

            JSONObject obj = new JSONObject(respuesta);

            JSONArray lista = obj.getJSONArray("services");
            Log.v("HISTORIAL", "services " + lista.toString());

            int count = lista.length();

            //    if (count > 15)
            //    {
            // count = 15;
            //    }

            for (int i = 0; i < count; i++) {

                JSONObject jsObject = lista.getJSONObject(i);

                JSONObject cliente = jsObject.getJSONObject("user");

                String nombre = cliente.getString("name");

                String apellido = cliente.getString("lastname");

                String id_service = jsObject.getString("id");

                String indice = jsObject.getString("index_id");
                String c1 = jsObject.getString("comp1");
                String c2 = jsObject.getString("comp2");
                String numero = jsObject.getString("no");
                String barrio = jsObject.getString("barrio");
                String obs = jsObject.getString("obs");
                String fecha = jsObject.getString("created_at");
                String direccion = jsObject.getString("address");

                String payType = jsObject.getString("pay_type");
                String payReference = jsObject.getString("pay_reference");
                String userId = jsObject.getString("user_id");
                String userEmail = jsObject.getString("user_email");

                String userCardReference = jsObject.getString("user_card_reference");

                String units   = jsObject.getString("units");
                String charge1 = jsObject.getString("charge1");
                String charge2 = jsObject.getString("charge2");
                String charge3 = jsObject.getString("charge3");
                String charge4 = jsObject.getString("charge4");
                String value   = jsObject.getString("value");


                Servicio s = new Servicio(id_service, indice, c1, c2, numero, barrio, obs, "", "", nombre, apellido, 1, "", "", direccion, Integer.valueOf(payType), payReference, userId, userEmail, userCardReference, units, charge1, charge2, charge3,charge4, value);

                s.setFecha(fecha);

                s.setRat(jsObject.getString("qualification"));

                servicios.add(s);

            }

        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            Toast.makeText(this, R.string.historico_aviso_sin_historial,
                    Toast.LENGTH_LONG).show();
        }
    }

}
