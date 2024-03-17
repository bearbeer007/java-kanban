package ru.yandex.app.service;

import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskStatus;

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

    public HashMap<Integer, Epic> getEpicMap() {
        return epicMap;
    }

    public HashMap<Integer, Subtask> getSubtaskMap() {
        return subtaskMap;
    }

    public HashMap<Integer, Task> getTaskMap() {
        return taskMap;
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

    //Метод поиска пересечений задач
    public boolean isCrossingOther(Task task) {
        //Если старт не задан, то пересечений не будет
        if (task.getStartTime() == null) {
            return false;
        }

        for (Task task1 : getPrioritizedTasks()) {
            if (task.getEndTime().isBefore(task1.getEndTime())
                    && task.getStartTime().isAfter(task1.getStartTime())) {
                return true;
            }
            if (task.getEndTime().isAfter(task1.getEndTime())
                    && task.getStartTime().isBefore(task1.getStartTime())) {
                return true;
            }
            if (task.getStartTime().equals(task1.getStartTime())
                    || task.getEndTime().equals(task1.getEndTime())) {
                return true;
            }
        }
        return false; //Пересечений нет
    }
    public int getId() {
        return id;
    }
    public void createEpic(Epic epic) {
        id += 1;
        epic.setId(id);
        epicMap.put(id, epic); //Эпик в мапу эпиков
        epic.solveStartTimeAndDuration(); //Расчет времени при создании Эпика
    }

    public Optional<Epic> receiveOneEpic(int epicId) {
        Optional<Epic> particularEpic = epicMap.values().stream().filter(epic -> epic.getId() == epicId).findFirst();
        if (particularEpic.isPresent()) {
            historyManagers.add(epicMap.get(epicId)); //Добавление вызванной задачи в историю
            return particularEpic;
        } else {
            return Optional.empty();
        }
    }

    public void addSubTaskInEpic(int epicId, Subtask subtask) {
        if (isCrossingOther(subtask)) { //Если есть пересечение, задача не добавляется
            return;
        }

        Epic epic = epicMap.get(epicId); // Получение объекта эпика по ID
        if (epic != null) {
            id += 1;
            subtask.setId(id);
            epic.getSubtasks().add(subtask); // Подзадач в список подзадач эпика
            subtaskMap.put(id, subtask); // Подазадча в мапу подзадач
            epic.solveStartTimeAndDuration(); //Пересчет времени при добавлении подзадачи
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);}}


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
            prioritizedTasks.remove(taskMap.get(id));prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id, taskMap.get(id));
            historyManagers.remove(id); //Удаление элемента из истории
        }
    }

    @Override
    public Optional<Task> receiveOneTask(int id) {
        return Optional.empty();
    }

    //Добавление вызванной задачи в историю
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
