package com.example.ktprojectall.kotlin.ui.provider;

public class ServerUser {
    String name;
    double value;

    ServerUser(String name, double value) {
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
