package ru.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> childId;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.status = TaskStatus.NEW;
        this.childId = new ArrayList<>();
        this.endTime = null;
    }

    public Epic(int taskId, String name, String description) {
        super(taskId, name, description);
        this.childId = new ArrayList<>();
        this.endTime = null;
    }

    public Epic(int taskId, String name, String description, List<Integer> childId) {
        super(taskId, name, description);
        this.childId = childId;
        this.endTime = null;
    }

    public Epic(int taskId, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        super(taskId, name, description);
        this.childId = new ArrayList<>();
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = null;
    }

    public List<Integer> getSubTaskIds() {
        return new ArrayList<>(childId);
    }

    public void addSubtaskId(int id) {
        childId.add(id);
    }

    public void removeSubtaskId(int id) {
        childId.remove((Integer) id);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        String result = "Task{" +
                "ID=" + taskId +
                ", Name='" + name + '\'' +
                ", Description='" + description + '\'' +
                ", status='" + status + '\'' + '}';
        if (!childId.isEmpty()) {
            result = result + ", subtasks=" + childId;
        }
        return result;
    }
}
