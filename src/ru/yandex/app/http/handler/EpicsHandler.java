package ru.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import ru.yandex.app.model.Epic;
import ru.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class EpicsHandler extends AbstractHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().toString();
        String requestMethod = httpExchange.getRequestMethod();
        try {
            // Проверка на неподдерживаемый метод запроса
            if (!"GET".equals(requestMethod) && !"POST".equals(requestMethod) && !"DELETE".equals(requestMethod)) {
                httpExchange.sendResponseHeaders(405, 0);
                return;
            }

            // Обработка GET-запросов
            if ("GET".equals(requestMethod)) {
                if (Pattern.matches("^/epics$", path)) {
                    String response = gson.toJson(taskManager.getAllEpics());
                    sendText(httpExchange, response);
                } else if (Pattern.matches("^/epics/\\d/subtasks$", path)) {
                    String[] pathId = path.split("/");
                    int epicId = parsePathId(pathId[2]);
                    if (epicId != -1) {
                        String response = gson.toJson(taskManager.getEpicByTaskId(epicId).getSubTaskIds());
                        sendText(httpExchange, response);
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                } else if (Pattern.matches("^/epics/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        String response = gson.toJson(taskManager.getEpicByTaskId(taskId));
                        sendText(httpExchange, response);
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                } else {
                    httpExchange.sendResponseHeaders(500, 0);
                }
            }

            // Обработка DELETE-запросов
            else if ("DELETE".equals(requestMethod)) {
                if (Pattern.matches("^/epics$", path)) {
                    taskManager.removeAllEpics();
                    httpExchange.sendResponseHeaders(200, 0);
                } else if (Pattern.matches("^/epics/\\d$", path)) {
                    String[] pathId = path.split("/");
                    int taskId = parsePathId(pathId[2]);
                    if (taskId != -1) {
                        taskManager.removeEpicById(taskId);
                        httpExchange.sendResponseHeaders(200, 0);
                    } else {
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                } else {
                    httpExchange.sendResponseHeaders(405, 0);
                }
            }

            // Обработка POST-запросов
            else if ("POST".equals(requestMethod)) {
                if (Pattern.matches("^/epics$", path)) {
                    String body = readText(httpExchange);
                    Epic epic = gson.fromJson(body, Epic.class);
                    taskManager.addEpic(epic);
                    httpExchange.sendResponseHeaders(201, 0);
                } else {
                    httpExchange.sendResponseHeaders(404, 0);
                }
            }
        } catch (Exception e) {
            httpExchange.sendResponseHeaders(500, 0);
        } finally {
            httpExchange.close();
        }
    }
}