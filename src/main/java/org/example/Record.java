package org.example;

import java.io.Serializable;

public class Record implements Serializable {
    private final int key;
    private String data;

    public Record(int key, String data) {
        this.key = key;
        this.data = data;
    }

    public int getKey() {
        return key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}