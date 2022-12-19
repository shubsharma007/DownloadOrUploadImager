package com.example.apicalling2;

import java.io.InputStream;

public class Dog {

    public String status,message;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String  message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}
