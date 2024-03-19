package ru.yandex.app.model;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int parentTaskId;

    public Subtask(String name, String description, int parentTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.status = TaskStatus.NEW;
        this.type = TaskType.SUBTASK;
        this.parentTaskId = parentTaskId;
    }

    public Subtask(int taskId, String name, String description, int parentTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.taskId = taskId;
        this.status = TaskStatus.NEW;
        this.type = TaskType.SUBTASK;
        this.parentTaskId = parentTaskId;
    }

    public Subtask(int taskId, String name, String description, int parentTaskId, TaskStatus status, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.taskId = taskId;
        this.status = status;
        this.type = TaskType.SUBTASK;
        this.parentTaskId = parentTaskId;
    }

    public int getParentTaskId() {
        return parentTaskId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "ID=" + taskId +
                ", Type='" + type + '\'' +
                ", Name='" + name + '\'' +
                ", Description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", EpicId=" + parentTaskId + '}';
    }
}