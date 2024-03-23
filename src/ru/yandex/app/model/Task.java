package ru.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected int taskId;
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    // Конструктор с двумя параметрами
    public Task(String name, String description) {
        this(0, name, description, TaskStatus.NEW, null, null);
    }

    // Конструктор с тремя параметрами
    public Task(int taskId, String name, String description) {
        this(taskId, name, description, TaskStatus.NEW, null, null);
    }

    // Конструктор с пятью параметрами
    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this(0, name, description, TaskStatus.NEW, startTime, duration);
    }

    // Конструктор с шестью параметрами
    public Task(int taskId, String name, String description, LocalDateTime startTime, Duration duration) {
        this(taskId, name, description, TaskStatus.NEW, startTime, duration);
    }

    // Основной конструктор с полным набором параметров
    public Task(int taskId, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        } else {
            return startTime.plus(duration);
        }
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    // Переопределенный метод getType()
    public TaskType getType() {
        return TaskType.TASK;
    }

    // Переопределенные методы toString(), equals() и hashCode()
    @Override
    public String toString() {
        return "Task{" +
                "ID=" + taskId +
                ", Name='" + name + '\'' +
                ", Description='" + description + '\'' +
                ", Status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}