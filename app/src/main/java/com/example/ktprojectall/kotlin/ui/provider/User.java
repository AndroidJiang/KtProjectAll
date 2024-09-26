package com.example.ktprojectall.kotlin.ui.provider;

public class User {
    String name;
    int value;

    User(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
