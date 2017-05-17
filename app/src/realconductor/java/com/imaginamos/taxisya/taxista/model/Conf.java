package com.imaginamos.taxisya.taxista.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Conf {

    public final String tag_shared = "taxista";
    private SharedPreferences prefs;
    private String ISLOGIN = "ISLOGIN";
    private String USER = "login";
    private String PASS = "password";
    private String UUID = "uuid";
    private String IDUSER = "driver_id";
    private String ISFIRST = "isFirst";
    private String IDSERVICE = "IDSERVICE";
    private String APPVERSION = "APPVERSION";


    private Context mContext;

    public Conf(Context context) {
        mContext = context;
        prefs = mContext.getSharedPreferences(tag_shared, Context.MODE_PRIVATE);
    }


    public void setLogin(boolean is) {
        Editor editor = prefs.edit();
        editor.putBoolean(ISLOGIN, is);
        editor.commit();
    }

    public boolean getLogin() {
        return prefs.getBoolean(ISLOGIN, false);
    }

    public void setUser(String user) {
        Editor editor = prefs.edit();
        editor.putString(USER, user);
        editor.commit();
    }

    public String getUser() {
        return prefs.getString(USER, null);
    }

    public void setPass(String pass) {
        Editor editor = prefs.edit();
        editor.putString(PASS, pass);
        editor.commit();
    }

    public String getPass() {
        return prefs.getString(PASS, null);
    }

    public void setUuid(String uuid) {
        Editor editor = prefs.edit();
        editor.putString(UUID, uuid);
        editor.commit();
    }

    public String getUuid() {
        return prefs.getString(UUID, null);
    }

    public void setIdUser(String iduser) {
        Editor editor = prefs.edit();
        editor.putString(IDUSER, iduser);
        editor.commit();
    }

    public String getIdUser() {
        return prefs.getString(IDUSER, null);
    }

    public void setIsFirst(boolean isFirst) {
        Editor editor = prefs.edit();
        editor.putBoolean(ISFIRST, isFirst);
        editor.commit();
    }

    public boolean getIsFirst() {
        return prefs.getBoolean(ISFIRST, false);
    }

    public String getServiceId() {
        return prefs.getString(IDSERVICE, null);
    }

    public void setServiceId(String idservice) {
        Editor editor = prefs.edit();
        editor.putString(IDSERVICE, idservice);
        editor.commit();
    }

    public int getAppVersion() {
        return prefs.getInt(APPVERSION, 0);
    }

    public void setAppVersion(int appversion) {
        Editor editor = prefs.edit();
        editor.putInt(APPVERSION, appversion);
        editor.commit();
    }


    public void oldLogin() {
        if (prefs.getString("pass", null) != null
                && prefs.getString("login", null) != null
                && prefs.getString("id_user", null) != null) {
            setPass(prefs.getString("pass", null));
            setUser(prefs.getString("login", null));
            setIdUser(prefs.getString("id_user", null));
            setIsFirst(false);
            setLogin(true);
        }
    }
}
