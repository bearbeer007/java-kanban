package ru.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int parentTaskId;

    public Subtask(String name, String description, int parentTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.status = TaskStatus.NEW;
        this.parentTaskId = parentTaskId;
    }

    public Subtask(int taskId, String name, String description, int parentTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.taskId = taskId;
        this.status = TaskStatus.NEW;
        this.parentTaskId = parentTaskId;
    }

    public Subtask(int taskId, String name, String description, int parentTaskId, TaskStatus status, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.taskId = taskId;
        this.status = status;
        this.parentTaskId = parentTaskId;
    }

    public int getParentTaskId() {
        return parentTaskId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Task{" +
                "ID=" + taskId +
                ", Name='" + name + '\'' +
                ", Description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", EpicId=" + parentTaskId + '}';
    }
}