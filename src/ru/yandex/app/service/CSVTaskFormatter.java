package ru.yandex.app.service;


import ru.yandex.app.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVTaskFormatter {
    private static final String DELIMITER = ",";

    public String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getTaskId()).append(DELIMITER);
        sb.append(task.getType()).append(DELIMITER);
        sb.append(task.getName()).append(DELIMITER);
        sb.append(task.getStatus()).append(DELIMITER);
        sb.append(task.getDescription()).append(DELIMITER);
        if (task.getType() == TaskType.SUBTASK) {
            sb.append(((Subtask) task).getParentTaskId());
        }
        sb.append(DELIMITER);
        sb.append(task.getStartTime()).append(DELIMITER);
        sb.append(task.getDuration()).append(DELIMITER);
        sb.append(task.getEndTime());
        return sb.toString();
    }

    public Task fromString(String value) {
        String[] values = value.split(DELIMITER);
        int epicId = 0;
        int taskId = Integer.parseInt(values[0]);
        TaskType type = TaskType.valueOf(values[1]);
        String name = values[2];
        TaskStatus status = TaskStatus.valueOf(values[3]);
        String description = values[4];
        if (type.equals(TaskType.SUBTASK)) {
            epicId = Integer.parseInt(values[5]);
        }
        LocalDateTime startTime = LocalDateTime.parse(values[6]);
        Duration duration = Duration.parse(values[7]);
        switch (type) {
            case EPIC:
                return new Epic(taskId, name, description, status, startTime, duration);
            case SUBTASK:
                return new Subtask(taskId, name, description, epicId, status, startTime, duration);
            default:
                return new Task(taskId, name, description, status, startTime, duration);
        }
    }

    public List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (value.isBlank() || value.isEmpty()) {
            return history;
        }
        String[] values = value.split(DELIMITER);
        for (String task : values) {
            history.add(Integer.parseInt(task));
        }
        return history;
    }

    public String historyToString(HistoryManager manager) {
        StringBuilder sb = new StringBuilder();
        for (Task task : manager.getHistory()) {
            sb.append(task.getTaskId()).append(DELIMITER);
        }
        if (!sb.toString().isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

}