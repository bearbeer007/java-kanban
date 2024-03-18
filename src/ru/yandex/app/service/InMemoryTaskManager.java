package ru.yandex.app.service;

import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

import static ru.yandex.app.model.TaskStatus.*;

// Менеджер задач для хранения задач в памяти
public class InMemoryTaskManager implements TaskManager {

    protected final HistoryManager historyManagers = Managers.getDefaultHistory();
    private static int id = 0;

    protected HashMap<Integer, Task> taskMap = new HashMap<>();
    protected HashMap<Integer, Epic> epicMap = new HashMap<>();
    protected HashMap<Integer, Subtask> subtaskMap = new HashMap<>();

    protected Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));

    private int getNextId() {
        return id++;
    }

    private void solveStartTimeAndDuration(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.getEndTime();
            return;
        }

        Duration epicDuration = Duration.ofMinutes(0);
        LocalDateTime epicStartTime = epic.getSubtasks().stream().min(Comparator.comparing(Subtask::getStartTime)).get().getStartTime();

        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getDuration() != null) {
                epicDuration = epicDuration.plus(subtask.getDuration());
            }
        }

        LocalDateTime epicEndTime = epicStartTime.plus(epicDuration);

        epic.setDuration(epicDuration);
        epic.setStartTime(epicStartTime);
        epic.setEndTime(epicEndTime);
    }


    @Override
    public List<Task> getHistory() {
        return historyManagers.getHistory();
    }

    @Override
    public Task getTask(Integer id) {
        Task task = taskMap.get(id);
        historyManagers.add(task);
        return task;
    }

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    public boolean isCrossingOther(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        for (Task task1 : getPrioritizedTasks()) {
            if (!(task.getStartTime().isAfter(task1.getEndTime()) || task.getEndTime().isBefore(task1.getStartTime()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createEpic(Epic epic) {

    }

    @Override
    public Optional<Epic> receiveOneEpic(int epicId) {
        Optional<Epic> particularEpic = epicMap.values().stream().filter(epic -> epic.getId() == epicId).findFirst();
        if (particularEpic.isPresent()) {
            historyManagers.add(epicMap.get(epicId));
            return particularEpic;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void addSubTaskInEpic(int epicId, Subtask subtask) {
        if (isCrossingOther(subtask)) {
            return;
        }

        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            subtask.setId(++id);
            epic.getSubtasks().add(subtask);
            subtaskMap.put(id, subtask);
            solveStartTimeAndDuration(epic);
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public Optional<Subtask> receiveSubtasksUseID(int subtaskId) {
        Optional<Subtask> particularSubtask = subtaskMap.values().stream().filter(subtask -> subtask.getId() == subtaskId).findFirst();
        if (particularSubtask.isPresent()) {
            historyManagers.add(subtaskMap.get(subtaskId));
            return particularSubtask;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteUseID(int id) {
        if (taskMap.containsKey(id)) {
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
            historyManagers.remove(id);
        }
    }

    @Override
    public Optional<Task> receiveOneTask(int id) {
        return Optional.ofNullable(taskMap.get(id));
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }
    @Override
    public Subtask getSubtask(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        historyManagers.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epicMap.get(id);
        historyManagers.add(epic);
        return epic;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            return -1;
        }
        int id = getNextId();
        task.setId(id);
        taskMap.put(id, task);
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        int id = getNextId();
        epic.setId(id);
        epicMap.put(id, epic);
        checkEpicState(id);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }
        int epicId = subtask.getEpicId();
        Epic epic = epicMap.get(epicId);
        if (epic == null) {
            return -1;
        }
        int id = getNextId();
        subtask.setId(id);
        subtaskMap.put(id, subtask);
        epic.getSubtaskIds().add(id);
        checkEpicState(id);
        return id;
    }

    @Override
    public int updateTask(Task task) {
        if (task == null) {
            return -1;
        }
        taskMap.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int updateEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        Epic oldEpic = epicMap.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        return epic.getId();
    }

    private void checkEpicState(int id) {
        boolean flagNew = false;
        boolean flagInProgress = false;
        boolean flagDone = false;
        Epic epic = epicMap.get(id);
        if (epic == null) {
            return;
        }
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtaskMap.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            switch (subtask.getTaskStatus()) {
                case NEW:
                    flagNew = true;
                    break;
                case IN_PROGRESS:
                    flagInProgress = true;
                    break;
                case DONE:
                    flagDone = true;
                    break;
            }
        }
        if (!flagInProgress && !flagDone) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else if (!flagNew && !flagInProgress) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = epicMap.get(epicId);
        if (epic == null) {
            return result;
        }
        for (int id : epic.getSubtaskIds()) {
            Subtask subtask = subtaskMap.get(id);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public boolean removeTask(Integer id) {
        Task task = taskMap.remove(id);
        historyManagers.remove(id);
        return (task != null);
    }

    @Override
    public boolean removeSubtask(Integer id) {
        Subtask subtask = subtaskMap.remove(id);
        if (subtask == null) {
            return false;
        }
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic == null) {
            return false;
        }
        epic.getSubtaskIds().remove(id);
        checkEpicState(epic.getId());
        historyManagers.remove(id);
        return true;
    }

    @Override
    public boolean removeEpic(Integer id) {
        Epic epic = epicMap.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtaskMap.remove(subtaskId);
                historyManagers.remove(subtaskId);
            }
            historyManagers.remove(id);
        }
        return (epic != null);
    }

    @Override
    public void removeAllSubtask() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtaskIds().clear();
            checkEpicState(epic.getId());
        }
    }

    @Override
    public void removeAllEpic() {
        epicMap.clear();
        subtaskMap.clear();

    }

    @Override
    public void removeAllTask() {
        prioritizedTasks = prioritizedTasks
                .stream()
                .filter(task -> task.getClass() != Task.class) //Если объект в коллекции Task, удаляется, тк удаляются все Task
                .collect(Collectors.toSet());
        taskMap.clear();

    }

    @Override
    public List<Task> getPrintTaskMap() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Epic> getPrintEpicMap() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getPrintSubtaskMap() {
        return new ArrayList<>(subtaskMap.values());
    }
}
