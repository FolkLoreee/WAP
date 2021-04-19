package com.example.wap.models;


public enum Authorization {
    USER("User"),
    ADMIN("Admin");
    private String alias = "";

    Authorization(String alias){
        this.alias = alias;
    }

    @Override
    public String toString(){
        return alias;
    }
}
