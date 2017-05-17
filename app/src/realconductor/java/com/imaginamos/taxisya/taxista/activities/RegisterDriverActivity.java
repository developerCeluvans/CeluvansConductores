package com.imaginamos.taxisya.taxista.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.carouseldemo.controls.CarouselAdapter;
import com.google.gson.JsonElement;
import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.io.ApiAdapter;
import com.imaginamos.taxisya.taxista.model.City;
import com.imaginamos.taxisya.taxista.model.Country;
import com.imaginamos.taxisya.taxista.model.Department;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

public class RegisterDriverActivity extends Activity implements View.OnClickListener {

    private String TAG = "RegisterDriverActivity";

    private EditText name;
    private EditText email;
    private EditText identity;
    private EditText license;
    private EditText address;
    private EditText pass;

    private EditText phone;
    private EditText cellphone;

    private EditText carPlate;
    private EditText carBrand;
    private EditText carLine;
  //  private EditText carMobileId;
    private EditText carYear;
    private EditText carCompany;

    private ImageView photoImageView;
    private ImageView documentImageView;
    private ImageView document2ImageView;
    private ImageView document3ImageView;
    private ImageView document4ImageView;

    private ImageView volver;

    private Spinner mCountrySpinner;
    private Spinner mDepartmentSpinner;
    private Spinner mCitiesSpinner;

    private ProgressDialog registerProgressDialog;

    private static final int CAMERA_PHOTO_REQUEST = 1888;
    private static final int CAMERA_DOCUMENT_REQUEST = 1889;
    private static final int CAMERA_DOCUMENT2_REQUEST = 1890;
    private static final int CAMERA_DOCUMENT3_REQUEST = 1891;
    private static final int CAMERA_DOCUMENT4_REQUEST = 1892;

    private static final int PIC_CROP = 1890;
    public static final MediaType MEDIA_TYPE_MARKDOWN = null;

    private Uri picUri;
    private OkHttpClient mClient;
    private String mPhotoString;
    private String mDocumentString;
    private String mDocument2String;
    private String mDocument3String;
    private String mDocument4String;
    private File photoFilesDirectory = null;
    private File mPhotoFilePath = null;
    private File mDocumentFilePath = null;
    private File mDocument2FilePath = null;
    private File mDocument3FilePath = null;
    private File mDocument4FilePath = null;
    private File mTempFilePath;
    private Button btnRegister;
    private ArrayList<Country> countriesArray;
    private ArrayList<Department> departmentsArray;
    private ArrayList<City> citiesArray;
    private int mCityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_driver);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        photoFilesDirectory = new File(Environment
                .getExternalStorageDirectory()+ "/TaxisYa");
        if (!photoFilesDirectory.exists()) {
            photoFilesDirectory.mkdir();
        }

        registerProgressDialog = new ProgressDialog(this);
        registerProgressDialog.setIndeterminate(true);
        registerProgressDialog.setMessage(getString(R.string.register_driver_in_progress));

        volver = (ImageView) findViewById(R.id.btn_volver);
        volver.setOnClickListener(this);

        name = (EditText) findViewById(R.id.txtName);

        identity = (EditText) findViewById(R.id.txtIdentify);

        license = (EditText) findViewById(R.id.txtLicense);

        email = (EditText) findViewById(R.id.txtUser);

        phone = (EditText) findViewById(R.id.txtPhone);

        cellphone = (EditText) findViewById(R.id.txtCellphone);

        address = (EditText) findViewById(R.id.txtAddress);

        pass = (EditText) findViewById(R.id.txtPass);

        carPlate = (EditText) findViewById(R.id.txtCarPlate);

        carBrand = (EditText) findViewById(R.id.txtCarBrand);

        carLine = (EditText) findViewById(R.id.txtCarLine);

      //  carMobileId = (EditText) findViewById(R.id.txtCarMobileId);

        carYear = (EditText) findViewById(R.id.txtCarYear);

        carCompany = (EditText) findViewById(R.id.txtCarCompany);

        btnRegister = (Button) findViewById(R.id.btnLogin);

        photoImageView = (ImageView) findViewById(R.id.driver_photo);
        photoImageView.setOnClickListener(this);

        documentImageView = (ImageView) findViewById(R.id.driver_document);
        documentImageView.setOnClickListener(this);

        document2ImageView = (ImageView) findViewById(R.id.driver_document2);
        document2ImageView.setOnClickListener(this);

        document3ImageView = (ImageView) findViewById(R.id.driver_document3);
        document3ImageView.setOnClickListener(this);

        document4ImageView = (ImageView) findViewById(R.id.driver_document4);
        document4ImageView.setOnClickListener(this);


        getSpinnerData();


        storageRegister();


    }


    public void getSpinnerData(){

        ApiAdapter.getApiService().getCountries("", new retrofit.Callback<RegisterResponse>() {

            @Override
            public void success(RegisterResponse registerResponse, Response response) {
                Log.i("COUNTRIES SUCCESS ", "SUCCESS RETURN " + response);
                Log.v("MAKE_DATA", "create countries");
                countriesArray = processCountriesResponse(new String(((TypedByteArray) response.getBody()).getBytes()));



                ApiAdapter.getApiService().getDepartments("", new retrofit.Callback<RegisterResponse>() {

                    @Override
                    public void success(RegisterResponse registerResponse, Response response) {
                        Log.i("DEPARTMENTS SUCCESS ", "SUCCESS RETURN " + response);
                        Log.v("MAKE_DATA", "create departmens");
                        departmentsArray = processDepartmentsResponse(new String(((TypedByteArray) response.getBody()).getBytes()));


                        ApiAdapter.getApiService().getCities("", new retrofit.Callback<RegisterResponse>() {

                            @Override
                            public void success(RegisterResponse registerResponse, Response response) {
                                Log.i("CITIES SUCCESS ", "SUCCESS RETURN " + response);
                                Log.v("MAKE_DATA", "create cities");
                                citiesArray = processCitiesResponse(new String(((TypedByteArray) response.getBody()).getBytes()));
                                prepareSpinners();
                            }

                            @Override
                            public void failure(RetrofitError error) {

                                Log.d("FAILURE CITIES ", "FAILURE RETURN " + error);

                            }
                        });

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        Log.d("FAILURE DEPARTMENTS", "FAILURE RETURN " + error);

                    }
                });

            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("FAILURE COUNTRIES", "FAILURE RETURN " + error);

            }
        });

    }


    public void prepareSpinners() {

        // set spinner
        mCountrySpinner = (Spinner) findViewById(R.id.spinner_country);

        mDepartmentSpinner = (Spinner) findViewById(R.id.spinner_department);

        mCitiesSpinner = (Spinner) findViewById(R.id.spinner_city);

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, countriesArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        mCountrySpinner.setAdapter(spinnerArrayAdapter);


        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int pos, long log) {

                Country item = (Country) parent.getItemAtPosition(pos);

                Log.v("UPDATE_DATA", "    spinner country " + String.valueOf(item.getId() + " " + item.getName()));

                int departmentId = firstDepartment(item.getId());
                updateDepartment(item.getId());
                updateCities(departmentId);

            }

            public void onNothingSelected(AdapterView arg0) {

            }
        });


        mDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int pos, long log) {

                Department item = (Department) parent.getItemAtPosition(pos);

                int departmentId = item.getId();

                Log.v("UPDATE_DATA", "    spinner department " + String.valueOf(item.getId() + " " + item.getName()));


                updateCities( departmentId );

            }

            public void onNothingSelected(AdapterView arg0) {

            }
        });

        mCitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int pos, long log) {

                City item = (City) parent.getItemAtPosition(pos);

                Log.v("UPDATE_DATA", "    spinner city " + String.valueOf(item.getId() + " " + item.getName()));

                mCityId = item.getId();

            }

            public void onNothingSelected(AdapterView arg0) {

            }
        });


        int countryId = countriesArray.get(0).getId();
        int departmentId = firstDepartment(countryId);

        Log.v("UPDATE_DATA"," ++++++++++++++++++++++++++++++ ");
        Log.v("UPDATE_DATA"," countryId: " + String.valueOf(countryId));
        Log.v("UPDATE_DATA"," departmentId: " + String.valueOf(departmentId));

        updateDepartment(countryId);

        updateCities( departmentId );

    }

    public void updateDepartment(int id) {
        Log.v("UPDATE_DATA","updateDepartment +");

        ArrayList<Department> da = new ArrayList<Department>();
        for (int i=0;i < departmentsArray.size(); i++ ) {
            Log.v("UPDATE_DATA","    i=" + String.valueOf(i));
            if (departmentsArray.get(i).getCountry_id() == id) {
                da.add(departmentsArray.get(i));
            }
        }
        ArrayAdapter saa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, da);
        saa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        mDepartmentSpinner.setAdapter(saa);
        Log.v("UPDATE_DATE", "updateDepartment -");

    }

    public void updateCities(int id) {

        ArrayList<City> ca = new ArrayList<City>();

        for (int i =0; i < citiesArray.size(); i++) {
            if (citiesArray.get(i).getDepartment_id() == id) {
                ca.add(citiesArray.get(i));
            }
        }

        ArrayAdapter saa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ca);
        saa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        mCitiesSpinner.setAdapter(saa);

    }

    public int firstDepartment(int id) {
        for (int i=0;i < departmentsArray.size(); i++ ) {
            if (departmentsArray.get(i).getCountry_id() == id) {
                return departmentsArray.get(i).getId();
            }
        }
        return -1;
    }

    public int firstCity(int id) {
        for (int i =0; i < citiesArray.size(); i++) {
            if (citiesArray.get(i).getDepartment_id() == id) {
                return citiesArray.get(i).getId();
            }
        }
        return -1;
    }

    public boolean storageRegister() {
        Log.v(TAG,"storageRegister 1");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String registerName = sharedPref.getString("register_name", "");
        Log.v(TAG, "onActivityResult = status " + registerName);
        if (registerName != null && !TextUtils.isEmpty(registerName)) { name.setText(registerName); }

        String registerIdentity    = sharedPref.getString("register_identity", "");
        String registerLicense     = sharedPref.getString("register_license", "");
        String registerEmail       = sharedPref.getString("register_email", "");
        String registerPhone       = sharedPref.getString("register_phone", "");
        String registerCellphone   = sharedPref.getString("register_cellphone", "");
        String registerAddress     = sharedPref.getString("register_address", "");
        String registerPassword    = sharedPref.getString("register_password", "");
        String registerCarPlate    = sharedPref.getString("register_car_plate", "");
        String registerCarBrand    = sharedPref.getString("register_car_brand", "");
        String registerCarLine     = sharedPref.getString("register_car_line", "");
        String registerCarMobileId = sharedPref.getString("register_car_mobile_id", "");
        String registerCarYear     = sharedPref.getString("register_car_year", "");
        String registerCarCompany  = sharedPref.getString("register_car_company", "");

        // photo
        String registerImage  = sharedPref.getString("register_photo", "");
        if (registerImage != null && !TextUtils.isEmpty(registerImage)) {
            Bitmap bitmapImage = decodeBase64(registerImage);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            photoImageView.setImageBitmap(scaled);
            mPhotoString = registerImage;
        }


        // doc1
        String registerDoc1  = sharedPref.getString("register_doc1", "");
        if (registerDoc1 != null && !TextUtils.isEmpty(registerDoc1)) {
            Bitmap bitmapImage = decodeBase64(registerDoc1);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            documentImageView.setImageBitmap(scaled);
            mDocumentString = registerDoc1;
        }


        // doc2
        String registerDoc2  = sharedPref.getString("register_doc2", "");
        if (registerDoc2 != null && !TextUtils.isEmpty(registerDoc2)) {
            Bitmap bitmapImage = decodeBase64(registerDoc2);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            document2ImageView.setImageBitmap(scaled);
            mDocument2String = registerDoc2;
        }


        // doc3
        String registerDoc3  = sharedPref.getString("register_doc3", "");
        if (registerDoc3 != null && !TextUtils.isEmpty(registerDoc3)) {
            Bitmap bitmapImage = decodeBase64(registerDoc3);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            document3ImageView.setImageBitmap(scaled);
            mDocument3String = registerDoc3;
        }


        // doc4
        String registerDoc4  = sharedPref.getString("register_doc4", "");
        if (registerDoc4 != null && !TextUtils.isEmpty(registerDoc4)) {
            Bitmap bitmapImage = decodeBase64(registerDoc4);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            document4ImageView.setImageBitmap(scaled);
            mDocument4String = registerDoc4;
        }



        if (registerIdentity != null && !TextUtils.isEmpty(registerIdentity)) { identity.setText(registerIdentity); }
        if (registerLicense != null && !TextUtils.isEmpty(registerLicense)) { license.setText(registerLicense); }
        if (registerEmail != null && !TextUtils.isEmpty(registerEmail)) { email.setText(registerEmail); }
        if (registerPhone != null && !TextUtils.isEmpty(registerPhone)) { phone.setText(registerPhone); }
        if (registerCellphone != null && !TextUtils.isEmpty(registerCellphone)) { cellphone.setText(registerCellphone); }
        if (registerAddress != null && !TextUtils.isEmpty(registerAddress)) { address.setText(registerAddress); }
        if (registerPassword != null && !TextUtils.isEmpty(registerPassword)) { pass.setText(registerPassword); }
        if (registerCarPlate != null && !TextUtils.isEmpty(registerCarPlate)) { carPlate.setText(registerCarPlate); }
        if (registerCarBrand != null && !TextUtils.isEmpty(registerCarBrand)) { carBrand.setText(registerCarBrand); }
        if (registerCarLine != null && !TextUtils.isEmpty(registerCarLine)) { carLine.setText(registerCarLine); }
        //if (registerCarMobileId != null && !TextUtils.isEmpty(registerCarMobileId)) { carMobileId.setText(registerCarMobileId); }
        if (registerCarYear != null && !TextUtils.isEmpty(registerCarYear)) { carYear.setText(registerCarYear); }
        if (registerCarCompany != null && !TextUtils.isEmpty(registerCarCompany)) { carCompany.setText(registerCarCompany); }

        Log.v(TAG,"storageRegister 2");

        return true;
    }

    public void registerService(View view) {

        if (validateFields()) {

       // if (mPhotoString != null) {
        Log.v("RegisterDriver", "registerService");
        Log.i(">> RegisterDriver", "mCityId = " + mCityId);

        //String imageString = convertImageToStringForServer( ((BitmapDrawable) photoImageView.getDrawable()).getBitmap() );
        //String documentString = convertImageToStringForServer( ((BitmapDrawable) documentImageView.getDrawable()).getBitmap() );






            String imageString = mPhotoString;
            String documentString = mDocumentString;
            String document2String = mDocument2String;
            String document3String = mDocument3String;
            String document4String = mDocument4String;



        Log.v("documentString","+++++++++++++++");
        Log.v("documentString","size image ");
        Log.v("documentString",imageString);
        Log.v("documentString","---------------");
        Log.v("documentString",documentString);
        Log.v("documentString","---------------");

        String md5Pass = md5(pass.getText().toString());





            registerProgressDialog.show();

            // save fields
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString("register_name", name.getText().toString());
            editor.putString("register_identity", identity.getText().toString());
            editor.putString("register_license", license.getText().toString());
            editor.putString("register_email", email.getText().toString());
            editor.putString("register_phone", phone.getText().toString());
            editor.putString("register_cellphone", cellphone.getText().toString());
            editor.putString("register_address", address.getText().toString());
            editor.putString("register_password", pass.getText().toString());
            editor.putString("register_car_plate", carPlate.getText().toString());
            editor.putString("register_car_brand", carBrand.getText().toString());
            editor.putString("register_car_line", carLine.getText().toString());
            editor.putString("register_car_mobile_id", "1");
            editor.putString("register_car_year", carYear.getText().toString());
            editor.putString("register_car_company", carCompany.getText().toString());

            Log.v("PHOTO_STRING","save = " + imageString);

            editor.putString("register_photo",imageString);
            editor.putString("register_doc1",documentString);
            editor.putString("register_doc2",document2String);
            editor.putString("register_doc3",document3String);
            editor.putString("register_doc4",document4String);

            editor.commit();

            // call register driver
            ApiAdapter.getApiService().registerDriver(name.getText().toString(),
                    "", email.getText().toString(),
                    pass.getText().toString(), phone.getText().toString(),
                    cellphone.getText().toString(), identity.getText().toString(), license.getText().toString(), address.getText().toString(), "0",
                    mCityId, carPlate.getText().toString(), carBrand.getText().toString(), carLine.getText().toString(),
                    "1", carYear.getText().toString(), carCompany.getText().toString(),
                    imageString, documentString,
                    document2String, document3String, document4String,
                    new retrofit.Callback<RegisterResponse>() {

                        @Override
                        public void success(RegisterResponse rep, Response response) {
                            registerProgressDialog.dismiss();
                            int error = 0;

                            Log.v("REGISTER_DRIVER","success - " + response.toString());
                            //IntputStream in  = response.getBody().in()
                            try {
                                error = rep.getError();
                            }
                            catch (Exception e) {
                            }

                            Log.v("REGISTER_DRIVER","success2 - " + String.valueOf(rep.getError()));
                            Log.v("REGISTER_DRIVER","success3 - " + String.valueOf(rep.getMessage()));


                            //Log.i("SUCCESS ", "SUCCESS RETURN " + response);
                            Log.i(">> RegisterDriverActivity >> registerService >> registerDriver()", "SUCCESS RETURN " + response);

                            if (error == 5) {
                                Toast.makeText(getApplicationContext(), rep.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), R.string.register_ok_message, Toast.LENGTH_LONG).show();
                            }
                            Intent i = new Intent(RegisterDriverActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            registerProgressDialog.dismiss();
                            Log.v("REGISTER_DRIVER","error - " + error.toString());
                            Log.d("FAILURE ", "FAILURE RETURN " + error);

//                            RestError body = (RestError) error.getBodyAs(RestError.class);
//                            if (body.getError() == 5) {
//                                Log.v("REGISTER_DRIVER", "FAILURE RETURN error 5");
//                                Toast.makeText(getApplicationContext(), R.string.register_bad_message, Toast.LENGTH_LONG).show();
//                                Intent i = new Intent(RegisterDriverActivity.this, LoginActivity.class);
//                                startActivity(i);
//                                finish();
//                            }
//                            else {
                                Toast.makeText(getApplicationContext(), R.string.register_bad_message, Toast.LENGTH_LONG).show();
//                            }

                        }
                    });

        }
            else {
            Toast.makeText(getApplicationContext(), R.string.register_all_fields, Toast.LENGTH_LONG).show();

        }
       // finish();

    }

    public boolean validateFields() {


        Log.v("CADENA1","str 0 " + mPhotoString);
        Log.v("CADENA1","str 2 " + mDocument2String);
        Log.v("CADENA1","str 3 " + mDocument3String);
        Log.v("CADENA1","str 4 " + mDocument4String);
        Log.v("CADENA1","str  " + mDocumentString);




        if (

                                (name.length() > 1) &&
                                (email.length() > 1) &&
                                (phone.length() > 1) &&
                                (cellphone.length() > 1) &&
                                (address.length()> 1) &&
                                (pass.length() > 1) &&
                                (identity.length() > 1) &&
                                        (license.length() > 1) &&
                                (mCityId > 0) &&
                                        (carPlate.length() > 0) &&
                                        (carBrand.length() > 0) &&
                                        (carLine.length() > 0) &&
                                      //  (carMobileId.length() > 0) &&
                                        (carYear.length() > 0) &&
                                        (carCompany.length() > 0)&&
                                        (mPhotoString!= null)&&
                                        (mDocument2String != null)&&
                                        (mDocument3String!= null)&&
                                        (mDocument4String!= null)&&
                                        (mDocumentString!= null)
                ) return true;

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.driver_photo:
                Log.v("onClick", "driver_photo");

                String photoName = getString(R.string.register_driver_photo).replace(" ", "_") + "_" + getCurrentDate();
                mPhotoFilePath = new File(photoFilesDirectory.toString(), photoName + ".jpg");
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mPhotoFilePath));
                startActivityForResult(photoIntent, CAMERA_PHOTO_REQUEST);
                break;

            case R.id.driver_document:
                Log.v("onClick", "driver_document" );

                String documentName = getString(R.string.register_driver_document_photo).replace(" ", "_") + "_" + getCurrentDate();
                mDocumentFilePath = new File(photoFilesDirectory.toString(), documentName + ".jpg");
                Intent documentIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                documentIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mDocumentFilePath));
                startActivityForResult(documentIntent, CAMERA_DOCUMENT_REQUEST);
                break;

            case R.id.driver_document2:
                Log.v("onClick", "driver_document2" );

                String document2Name = getString(R.string.register_driver_license_photo).replace(" ", "_") + "_" + getCurrentDate();
                mDocument2FilePath = new File(photoFilesDirectory.toString(), document2Name + ".jpg");
                Intent document2Intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                document2Intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mDocument2FilePath));
                startActivityForResult(document2Intent, CAMERA_DOCUMENT2_REQUEST);
                break;

            case R.id.driver_document3:
                Log.v("onClick", "driver_document3" );

                String document3Name = getString(R.string.register_driver_card_property).replace(" ", "_") + "_" + getCurrentDate();
                mDocument3FilePath = new File(photoFilesDirectory.toString(), document3Name + ".jpg");
                Intent document3Intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                document3Intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mDocument3FilePath));
                startActivityForResult(document3Intent, CAMERA_DOCUMENT3_REQUEST);
                break;

            case R.id.driver_document4:
                Log.v("onClick", "driver_document4" );

                String document4Name = getString(R.string.register_driver_card_operation).replace(" ", "_") + "_" + getCurrentDate();
                mDocument4FilePath = new File(photoFilesDirectory.toString(), document4Name + ".jpg");
                Intent document4Intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                document4Intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mDocument4FilePath));
                startActivityForResult(document4Intent, CAMERA_DOCUMENT4_REQUEST);
                break;

            case R.id.btn_volver:
                finish();
                break;


            default:
                break;

        }
    }

    /*
    private String saveToInternalSorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
        return directory.getAbsolutePath();
    }
    */



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_PHOTO_REQUEST && resultCode == RESULT_OK) {
            //picUri = data.getData();
            //performCrop()

            try {
                if (mPhotoFilePath != null) {

                    Bitmap photo = BitmapFactory.decodeStream(new FileInputStream(mPhotoFilePath));
                    setThumbnailImage(photoImageView, mPhotoFilePath.toString());
                    mPhotoString = convertImageToStringForServer(photo);

                }


            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }

        }

        if (requestCode == CAMERA_DOCUMENT_REQUEST && resultCode == RESULT_OK) {

            try {

                if (mDocumentFilePath != null) {

                    Bitmap photoDocument = BitmapFactory.decodeStream(new FileInputStream(mDocumentFilePath));
                    setThumbnailImage(documentImageView, mDocumentFilePath.toString());
                    mDocumentString = convertImageToStringForServer(photoDocument);

                }

            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_DOCUMENT2_REQUEST && resultCode == RESULT_OK) {

            try {
                if (mDocument2FilePath != null) {
                    Bitmap photoDocument2 = BitmapFactory.decodeStream(new FileInputStream(mDocument2FilePath));
                    setThumbnailImage(document2ImageView, mDocument2FilePath.toString());
                    mDocument2String = convertImageToStringForServer(photoDocument2);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }

        }

        if (requestCode == CAMERA_DOCUMENT3_REQUEST && resultCode == RESULT_OK) {

            try {
                if (mDocument3FilePath != null) {

                    Bitmap photoDocument3 = BitmapFactory.decodeStream(new FileInputStream(mDocument3FilePath));
                    setThumbnailImage(document3ImageView, mDocument3FilePath.toString());
                    mDocument3String = convertImageToStringForServer(photoDocument3);

                }

            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_DOCUMENT4_REQUEST && resultCode == RESULT_OK) {

            try {
                if (mDocument4FilePath != null) {

                    Bitmap photoDocument4 = BitmapFactory.decodeStream(new FileInputStream(mDocument4FilePath));
                    setThumbnailImage(document4ImageView, mDocument4FilePath.toString());
                    mDocument4String = convertImageToStringForServer(photoDocument4);
                }

            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        if (resultCode == PIC_CROP && requestCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photoImageView.setImageBitmap(photo);
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public void setDriverImage(String photoName) {

        //String photoName = "20150219_222813.jpg";
        File photo = new File(photoName );
        TypedFile typedImage = new TypedFile("application/octet-stream", photo);

        ApiAdapter.getApiService().uploadImage(typedImage, new retrofit.Callback<RegisterResponse>() {

            @Override
            public void success(RegisterResponse photo, Response response) {
                Log.d("SUCCESS ", "SUCCESS RETURN " + response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("FAILURE ", "FAILURE RETURN " + error);
            }
        });
    }

    private void performCrop() {
        //call the standard crop action intent (the user device may not support it)
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        cropIntent.setDataAndType(picUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, PIC_CROP);
    }


    public static String convertImageToStringForServer(Bitmap imageBitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(imageBitmap != null) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 15, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }else{
            return null;
        }
    }

    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;

    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }



    private File getTempFile()
    {
        //it will return /sdcard/image.tmp
//        return new File(Environment.getExternalStorageDirectory(),  "image.tmp");
        return new File(getFilesDir(),  "image.tmp");

    }

    public void storeImage(ImageView imageView) {
        Bitmap bm;

        View v=imageView;
        v.setDrawingCacheEnabled(true);
        bm=Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        String fileName="image.png";
        File file=new File(fileName);

        try
        {

            FileOutputStream fOut=openFileOutput(fileName, MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public ArrayList<Country> processCountriesResponse(String countriesResponse){

        ArrayList<Country> arrayListCountries = new ArrayList<Country>();

        try {

            JSONObject jsonResponse = new JSONObject(countriesResponse);
            JSONArray arrayCountries = jsonResponse.getJSONArray("countries");
            JSONObject country;

            for (int i = 0; i < arrayCountries.length(); i++) {

                country = arrayCountries.getJSONObject(i);
                //Log.i(">>processCountriesResponse", "id: "+ country.getInt("id"));
                //Log.i(">>processCountriesResponse", "name: " + country.getString("name"));
                arrayListCountries.add(new Country(country.getInt("id"), country.getString("name")));
            }

        } catch (Exception e) {
            //exception
        }

        return arrayListCountries;
    }

    public ArrayList<Department> processDepartmentsResponse(String departmentsResponse){

        ArrayList<Department> arrayListDepartments = new ArrayList<Department>();

        try {

            JSONObject jsonResponse = new JSONObject(departmentsResponse);
            JSONArray arrayDepartments = jsonResponse.getJSONArray("departments");
            JSONObject department;

            for (int i = 0; i < arrayDepartments.length(); i++) {

                department = arrayDepartments.getJSONObject(i);
                //Log.i(">>processDepartmentsResponse", "id: "+ country.getInt("id"));
                //Log.i(">>processDepartmentsResponse", "name: " + country.getString("name"));
                arrayListDepartments.add(new Department(department.getInt("id") ,department.getString("name"), department.getInt("country_id")));
            }

        } catch (Exception e) {
            //exception
        }

        return arrayListDepartments;
    }

    public ArrayList<City> processCitiesResponse(String citiesResponse){

        ArrayList<City> arrayListCities = new ArrayList<City>();

        try {

            JSONObject jsonResponse = new JSONObject(citiesResponse);
            JSONArray arrayCities = jsonResponse.getJSONArray("cities");
            JSONObject city;

            for (int i = 0; i < arrayCities.length(); i++) {

                city = arrayCities.getJSONObject(i);
                //Log.i(">>processDepartmentsResponse", "id: "+ country.getInt("id"));
                //Log.i(">>processDepartmentsResponse", "name: " + country.getString("name"));
                arrayListCities.add(new City(city.getInt("id") ,city.getString("name"), city.getInt("department_id")));
            }

        } catch (Exception e) {
            //exception
        }

        return arrayListCities;
    }


    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        return df.format(new Date());
    }


    private void setThumbnailImage(ImageView mImageView, String imagePath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private void setThumbnailImageStorage(ImageView mImageView, String imageString) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(imagePat, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

       // Bitmap bitmap = decodeBase64(imageString));



       byte[] b = Base64.decode(imageString, Base64.DEFAULT);
       InputStream is = new ByteArrayInputStream(b);
       //Bitmap bitmap = BitmapFactory.decodeStream(is, bmOptions);
        Bitmap bitmap = BitmapFactory.decodeFile(imageString, bmOptions);
        mImageView.setImageBitmap(bitmap);

    }



}
