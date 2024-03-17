package ru.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime epicStartTime;
    private Duration epicDuration;
    private LocalDateTime epicEndTime;

    private ArrayList<Subtask> subtasks;



    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }



    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }


    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void solveStartTimeAndDuration() {
        if (subtasks.isEmpty()) {
            epicDuration = null;
            epicStartTime = null;
            epicEndTime = null;
            return;
        }
        epicDuration = Duration.ofMinutes(0);
        subtasks.stream()
                .filter(subtask -> subtask.duration != null) //Если продолжительности нет, то задача не интересна
                .forEach(subtask -> epicDuration = epicDuration.plus(subtask.duration)); //Прибавить к epicDuration
        //durations subtasks
        epicStartTime = subtasks.stream().min(Comparator.comparing((Subtask sub) -> sub.startTime)).get().startTime;

        if (epicStartTime != null) {
            epicEndTime = epicStartTime.plus(epicDuration); //Время окончания = время начала первой задачи + общая продолжительность
        }

    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return id + "," + "EPIC," + name + "," + status + "," + description  + ","
                + ((epicDuration != null) ? epicDuration.toMinutes() : epicDuration)
                + "," + ((epicStartTime != null) ? epicStartTime.format(Constants.FORMATTER) +
                "," + epicEndTime.format(Constants.FORMATTER)
                : epicStartTime + "," + epicEndTime);
    }



}