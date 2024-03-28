package ru.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;


public class HistoryHandler extends AbstractHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestMethod = httpExchange.getRequestMethod();
        if (!requestMethod.equals("GET")) {
            httpExchange.sendResponseHeaders(405, -1);
            httpExchange.close();
            return;
        }

        try {
            String response = gson.toJson(taskManager.getHistory());
            sendText(httpExchange, response);
        } catch (Exception e) {
            httpExchange.sendResponseHeaders(404, 0);
        } finally {
            httpExchange.close();
        }
    }
}