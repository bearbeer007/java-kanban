package ru.yandex.app.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.app.http.adapters.DurationAdapter;
import ru.yandex.app.http.adapters.LocalDateAdapter;
import ru.yandex.app.http.handler.*;
import ru.yandex.app.service.Managers;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;


public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private static Gson gson;


    public HttpTaskServer(TaskManager taskManager) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        gson = gsonBuilder.create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(taskManager, Managers.createGson()));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, Managers.createGson()));
        server.createContext("/epics", new EpicsHandler(taskManager, Managers.createGson()));
        server.createContext("/history", new HistoryHandler(taskManager, Managers.createGson()));
        server.createContext("/prioritized", new PrioritizedTasksHandler(taskManager, Managers.createGson()));
    }

    public void start() {
        System.out.println("Go to http://localhost:" + PORT + "/");
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    public static Gson getGson() {
        return gson;
    }


}