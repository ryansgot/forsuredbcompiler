package com.fsryan.forsuredb.jdbcexample;

import com.google.gson.annotations.SerializedName;

public class MyPojo {

    @SerializedName("my_int") private int awesomeInt;
    @SerializedName("my_string") private String awesomeString;
    @SerializedName("my_composed_object") private ComposedPojo composedPojo;

    public int getAwesomeInt() {
        return awesomeInt;
    }

    public void setAwesomeInt(int awesomeInt) {
        this.awesomeInt = awesomeInt;
    }

    public String getAwesomeString() {
        return awesomeString;
    }

    public void setAwesomeString(String awesomeString) {
        this.awesomeString = awesomeString;
    }

    public ComposedPojo getComposedPojo() {
        return composedPojo;
    }

    public void setComposedPojo(ComposedPojo composedPojo) {
        this.composedPojo = composedPojo;
    }

    public static class ComposedPojo {
        @SerializedName("composed_int") private int composedInt;
        @SerializedName("composed_string") private String composedString;

        public int getComposedInt() {
            return composedInt;
        }

        public void setComposedInt(int composedInt) {
            this.composedInt = composedInt;
        }

        public String getComposedString() {
            return composedString;
        }

        public void setComposedString(String composedString) {
            this.composedString = composedString;
        }
    }
}
