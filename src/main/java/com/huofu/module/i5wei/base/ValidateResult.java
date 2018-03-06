package com.huofu.module.i5wei.base;

/**
 * Created by akwei on 2/25/15.
 */
public class ValidateResult {

    private boolean success;


    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "success[" + success + "] message[" + this.message + "]";
    }
}
