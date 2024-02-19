package ru.yandex.app.service;

import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.app.model.TaskStatus.*;

// Менеджер задач для хранения задач в памяти
public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManagers = Managers.getDefaultHistory();
    private static int id = 0;

    private Map<Integer, Task> taskMap = new HashMap<>();
    private Map<Integer, Epic> epicMap = new HashMap<>();
    private Map<Integer, Subtask> subtaskMap = new HashMap<>();

    private int getNextId() {
        return id++;
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
