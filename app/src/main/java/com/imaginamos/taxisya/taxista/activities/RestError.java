package com.imaginamos.taxisya.taxista.activities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by leo on 5/11/16.
 */
public class RestError {
    @SerializedName("error")
    public int error;
    @SerializedName("msg")
    public String errorMessage;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
