package ru.yandex.app.http.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.app.http.HttpTaskServer;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;


public class SubtasksHandler extends AbstractHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                if (path.equals("/subtasks")) {
                    String response = gson.toJson(taskManager.getAllSubTasks());
                    sendText(httpExchange, response);
                    return;
                }

                if (Pattern.matches("^/subtasks/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        String response = gson.toJson(taskManager.getSubTaskByTaskId(taskId));
                        sendText(httpExchange, response);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                        return;
                    }
                }
            }

            if (requestMethod.equals("DELETE")) {
                if (path.equals("/subtasks")) {
                    taskManager.removeAllSubTasks();
                    httpExchange.sendResponseHeaders(200, 0);
                    return;
                }

                if (Pattern.matches("^/subtasks/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        taskManager.removeSubTaskById(taskId);
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                        return;
                    }
                }
            }

            if (requestMethod.equals("POST")) {
                if (Pattern.matches("^/subtasks/\\d$", path)) {
                    String body = readText(httpExchange);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    taskManager.updateSubTask(subtask);
                    httpExchange.sendResponseHeaders(201, 0);
                    return;
                }

                if (path.equals("/subtasks")) {
                    String body = readText(httpExchange);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    taskManager.addSubTask(subtask);
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