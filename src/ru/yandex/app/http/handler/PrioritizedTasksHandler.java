package ru.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;


public class PrioritizedTasksHandler extends AbstractHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PrioritizedTasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String path = httpExchange.getRequestURI().toString();
            String requestMethod = httpExchange.getRequestMethod();

            if (!requestMethod.equals("GET")) {
                httpExchange.sendResponseHeaders(405, 0);
                return;
            }

            if (!path.equals("/prioritized")) {
                httpExchange.sendResponseHeaders(404, 0);
                return;
            }

            String response = gson.toJson(taskManager.getPrioritizedTasks());
            sendText(httpExchange, response);
        } catch (Exception e) {
            httpExchange.sendResponseHeaders(404, 0);
        } finally {
            httpExchange.close();
        }
    }

}