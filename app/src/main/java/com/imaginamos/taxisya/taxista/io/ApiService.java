package com.imaginamos.taxisya.taxista.io;

import com.google.gson.JsonElement;
import com.imaginamos.taxisya.taxista.activities.AppResponse;
import com.imaginamos.taxisya.taxista.activities.RegisterResponse;
import com.imaginamos.taxisya.taxista.activities.UploadResponse;
import com.squareup.okhttp.RequestBody;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * Created by leo on 11/15/15.
 */
public interface ApiService {
    // login
//    params.put("type", HomeActivity.TYPE_USER);
    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_LOGIN)
    void login(@Field("type") String type,
               @Field("login") String login,
               @Field("pwd") String pwd,
               @Field("uuid") String uuid,
               Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_REGISTER)
    void register(@Field("type") String type,
                  @Field("name") String name,
                  @Field("lastname") String lastname,
                  @Field("email") String email,
                  @Field("login") String login,
                  @Field("pwd") String pwd,
                  @Field("token") String token,
                  @Field("cellphone") String cellphone,
                  @Field("uuid") String uuid, Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_REGISTER)
    void registerDriver(@Field("name") String name,
                        @Field("lastname") String lastname,
                        @Field("login") String login,
                        @Field("pwd") String pwd,
                        @Field("telephone") String telephone,
                        @Field("cellphone") String cellphone,
                        @Field("cedula") String cedula,
                        @Field("license") String license,
                        @Field("dir") String dir,
                        @Field("movil") String movil,
                        @Field("city_id") int city,
                        @Field("car_tag") String carPlate,
                        @Field("car_brand") String carBrand,
                        @Field("car_line") String carLine,
                        @Field("car_movil") String carMovil,
                        @Field("car_year") String carYear,
                        @Field("car_company") String carCompany,
                        @Field("image") String image,
                        @Field("document") String document,
                        @Field("document2") String document2,
                        @Field("document3") String document3,
                        @Field("document4") String document4,
                        Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_UPLOAD)
    void update(@Field("type") String type,
                @Field("name") String name,
                @Field("lastname") String lastname,
                @Field("email") String email,
                @Field("login") String login,
                @Field("pwd") String pwd,
                @Field("token") String token,
                @Field("cellphone") String cellphone,
                @Field("uuid") String uuid, Callback<UploadResponse> callback);

    @Multipart
    @POST("/upload/login.php")
    void setDriverImage(
            @QueryMap Map<String, String> params,
            // the @Part has the parameter "pathImage".
            // You should pass this in your php code.
            @Part("pathImage") TypedFile file,
            Callback<JsonElement> response);

    @Multipart
    @POST("/uploads")
    void uploadImage(@Part("file") TypedFile file, Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.APP_VERSION)
    void appVersion(@Field("descript") String descript, Callback<AppResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_COUNTRY)
    void getCountries(@Field("empty") String empty, Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_DEPARTMENT)
    void getDepartments(@Field("empty") String empty, Callback<RegisterResponse> callback);

    @FormUrlEncoded
    @POST(ApiConstants.DRIVER_CITY)
    void getCities(@Field("empty") String empty, Callback<RegisterResponse> callback);

}
