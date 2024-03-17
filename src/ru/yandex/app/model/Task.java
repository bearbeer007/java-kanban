package ru.yandex.app.model;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import ru.yandex.app.model.Constants;

import static ru.yandex.app.model.TaskStatus.NEW;

public class Task {

    protected int id;//идентификатор задачи
    protected String name;//название задачи
    protected String description;//описание задачи
    protected TaskStatus taskStatus = NEW;
    protected TaskStatus status;
    protected Duration duration; //Продолжительность в минутах
    protected LocalDateTime startTime; //Время начала задачи
    protected LocalDateTime endTime;

    //конструкторы


    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = taskStatus; //Как только задача создана, она новая.
    }

    public Task(String name, String description, long minutes, String startTime) {
        this.name = name;
        this.description = description;
        status = taskStatus;
        duration = Duration.ofMinutes(minutes);
        this.startTime = LocalDateTime.parse(startTime, Constants.FORMATTER);
    }

    //методы get и set


    public LocalDateTime getStartTime() {
        return startTime;
    }
    public LocalDateTime getEndTime() {
        if (startTime == null | duration == null) return null;
        endTime = startTime.plus(duration);
        return endTime;
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
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                Objects.equals(id, task.id) &&
                Objects.equals(taskStatus, task.taskStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, taskStatus);
    }

    @Override
    public String toString() {
        return id + "," + "TASK," + name + "," + status + "," + description + ","
                + ((duration != null) ? duration.toMinutes() : duration) + ","
                + ((startTime != null) ? startTime.format(Constants.FORMATTER) + "," + getEndTime().format(Constants.FORMATTER)
                : startTime + "," + getEndTime());
    }



}
