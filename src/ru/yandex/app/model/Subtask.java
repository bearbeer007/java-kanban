package ru.yandex.app.model;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;



    public Subtask(String name, String description) {
        super(name, description);
        status = TaskStatus.NEW;
    }

    public Subtask(String name, String description, Duration minutes, LocalDateTime startTime) {
        super(name, description, minutes, startTime);
        status = TaskStatus.NEW;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return getEpicId() == subtask.getEpicId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, epicId);
    }

    @Override
    public String toString() {
        return id + "," + "SUBTASK," + name + "," + status + "," + description + ","
                + ((duration != null) ? duration.toMinutes() : duration) + ","
                + ((startTime != null) ? startTime.format(Constants.FORMATTER) + ","
                + getEndTime().format(Constants.FORMATTER) :
                startTime + "," + getEndTime());
    }
}