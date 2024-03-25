package ru.yandex.app.http.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.app.http.HttpTaskServer;
import ru.yandex.app.model.Task;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;


public class TasksHandler extends AbstractHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager,Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String path = httpExchange.getRequestURI().toString();
            String requestMethod = httpExchange.getRequestMethod();

            if (!requestMethod.equals("GET") && !requestMethod.equals("DELETE") && !requestMethod.equals("POST")) {
                httpExchange.sendResponseHeaders(405, 0);
                return;
            }

            if (requestMethod.equals("GET")) {
                if (path.equals("/tasks")) {
                    String response = gson.toJson(taskManager.getAllTasks());
                    sendText(httpExchange, response);
                    return;
                }

                if (Pattern.matches("^/tasks/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        String response = gson.toJson(taskManager.getTaskByTaskId(taskId));
                        sendText(httpExchange, response);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                        return;
                    }
                }
            }

            if (requestMethod.equals("DELETE")) {
                if (path.equals("/tasks")) {
                    taskManager.removeAllTasks();
                    httpExchange.sendResponseHeaders(200, 0);
                    return;
                }

                if (Pattern.matches("^/tasks/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        taskManager.removeTaskById(taskId);
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                        return;
                    }
                }
            }

            if (requestMethod.equals("POST")) {
                if (Pattern.matches("^/tasks/\\d$", path)) {
                    String body = readText(httpExchange);
                    Task task = gson.fromJson(body, Task.class);
                    taskManager.updateTask(task);
                    httpExchange.sendResponseHeaders(201, 0);
                    return;
                }

                if (path.equals("/tasks")) {
                    String body = readText(httpExchange);
                    Task task = gson.fromJson(body, Task.class);
                    taskManager.addTask(task);
                    httpExchange.sendResponseHeaders(201, 0);
                    return;
                }
            }

            httpExchange.sendResponseHeaders(404, 0);
        } catch (Exception e) {
            httpExchange.sendResponseHeaders(404, 0);
        } finally {
            httpExchange.close();
        }
    }


}