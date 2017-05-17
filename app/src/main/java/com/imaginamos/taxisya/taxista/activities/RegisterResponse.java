package com.imaginamos.taxisya.taxista.activities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by leo on 11/15/15.
 */
public class RegisterResponse {
    @SerializedName("Response")
    private String response;
    @SerializedName("error")
    private int error;
    @SerializedName("msg")
    private String message;


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
