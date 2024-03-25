package ru.yandex.app.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.app.http.adapters.DurationAdapter;
import ru.yandex.app.http.adapters.LocalDateAdapter;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class Managers {
    private Managers() {
        // Пустой конструктор
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    private static Gson gson;

    static {
        createGson();
    }

    private static void createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        gson = gsonBuilder.create();
    }

    public static Gson getGson() {
        return gson;
    }
}