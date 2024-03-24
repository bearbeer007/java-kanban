package ru.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.app.http.HttpTaskServer;
import ru.yandex.app.model.Epic;

import ru.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.regex.Pattern;


public class EpicsHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String path = httpExchange.getRequestURI().toString();
            String requestMethod = httpExchange.getRequestMethod();
            switch (requestMethod) {
                case "GET": {
                    if (Pattern.matches("^/epics$", path)) {
                        String response = gson.toJson(taskManager.getAllEpics());
                        sendText(httpExchange, response);
                        break;
                    }
                    if (Pattern.matches("^/epics/\\d/subtasks$", path)) {
                        String[] pathId = path.split("/");
                        int epicId = parsePathId(pathId[2]);
                        if (epicId != -1) {
                            String response = gson.toJson(taskManager.getEpicById(epicId).getSubTaskIds());
                            sendText(httpExchange, response);
                        } else {
                            httpExchange.sendResponseHeaders(404, 0);
                        }
                        break;
                    }
                    if (Pattern.matches("^/epics/\\d$", path)) {
                        String[] pathId = path.split("/");
                        int taskId = parsePathId(pathId[2]);
                        if (taskId != -1) {
                            String response = gson.toJson(taskManager.getEpicById(taskId));
                            sendText(httpExchange, response);
                        } else {
                            httpExchange.sendResponseHeaders(404, 0);
                        }
                    } else {
                        httpExchange.sendResponseHeaders(500, 0);
                    }
                    break;
                }
                case "DELETE": {
                    if (Pattern.matches("^/epics$", path)) {
                        taskManager.removeAllEpics();
                        httpExchange.sendResponseHeaders(200, 0);
                        break;
                    }

                    if (Pattern.matches("^/epics/\\d$", path)) {
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
                    break;
                }
                case "POST": {
                    if (Pattern.matches("^/epics$", path)) {
                        String body = readText(httpExchange);
                        Epic epic = gson.fromJson(body, Epic.class);
                        taskManager.addEpic(epic);
                        httpExchange.sendResponseHeaders(201, 0);
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                    break;
                }
                default: {
                    System.out.println("invalid request method " + requestMethod);
                }
            }

        } catch (Exception e) {
            httpExchange.sendResponseHeaders(404, 0);
        } finally {
            httpExchange.close();
        }
    }

    private int parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }
}