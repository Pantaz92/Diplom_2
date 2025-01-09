package ru.stellarburgers.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {

    private final static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static Gson getGson() {
        return gson;
    }
}
