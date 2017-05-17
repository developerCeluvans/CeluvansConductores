package com.imaginamos.taxisya.taxista.io;

import com.imaginamos.taxisya.taxista.BuildConfig;

/**
 * Created by leo on 11/15/15.
 */
public class ApiConstants {

    public static String app_code = "TAXISYA";
    //public static String app_secret_key = "oDX4b0Yk4usp0ptEf1XFK00KQSxAgV";
    public static String app_secret_key = "jN2Dk59eDX1h7W7NtiV3YYbzCFSHRY";
    public static boolean api_env = false;

    public static final String BASE_URL = "http://" + BuildConfig.HOST + "/public/";

    // USER
    public static final String DRIVER_LOGIN = "/user/login";
    public static final String DRIVER_REGISTER = "/driver/register_driver";
    public static final String DRIVER_UPLOAD = "/uploads";
    public static final String DRIVER_COUNTRY = "/country";
    public static final String DRIVER_DEPARTMENT = "/department";
    public static final String DRIVER_CITY = "/city";

    public static final String APP_VERSION = "/app/versions";

}

