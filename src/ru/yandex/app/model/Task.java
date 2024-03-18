package ru.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    protected int id; // идентификатор задачи
    protected String name; // название задачи
    protected String description; // описание задачи
    protected TaskStatus status = TaskStatus.NEW; // Статус задачи
    protected Duration duration; // Продолжительность в минутах
    protected LocalDateTime startTime; // Время начала задачи
    private LocalDateTime endTime;

    // конструкторы

    public Task(String name, String description) {
        this(name, description, null, null);
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
    }

    // методы get и set

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (startTime != null && duration != null) {
            this.endTime = startTime.plus(duration);
        } else {
            this.endTime = endTime;
        }
    }


    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return (startTime != null && duration != null) ? startTime.plus(duration) : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return status;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.status = taskStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                Objects.equals(id, task.id) &&
                Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    @Override
    public String toString() {
        return id + "," + "TASK," + name + "," + status + "," + description + ","
                + ((duration != null) ? duration.toMinutes() : duration) + ","
                + ((startTime != null) ? startTime.format(Constants.FORMATTER) + "," + getEndTime().format(Constants.FORMATTER)
                : startTime + "," + getEndTime());
    }


}
