package ru.yandex.app.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.app.http.adapters.DurationAdapter;
import ru.yandex.app.http.adapters.LocalDateAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

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

    public static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        return gsonBuilder.create();
    }
}