package ru.yandex.app.service;

import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaskManager {
    // 2. Методы для каждого из типа задач(Задача/Эпик/Подзадача):
    // 2.a Получение списка всех задач.
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubTasks();

    // 2.b  Удаление всех задач.
    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubTasks();

    // 2.c  Получение по идентификатору.
    Task getTaskByTaskId(int taskId);

    Epic getEpicByTaskId(int taskId);

    Subtask getSubTaskByTaskId(int taskId);

    // 2.d  Создание. Сам объект должен передаваться в качестве параметра.
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubTask(Subtask subtask);

    // 2.e Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    int updateTask(Task task);

    int updateEpic(Epic epic);

    int updateSubTask(Subtask subtask);

    // 2.f Удаление по идентификатору.
    void removeTaskById(int taskId);

    void removeSubTaskById(int taskId);

    void removeEpicById(int taskId);

    // 3.a  Получение списка всех подзадач определённого эпика.
    List<Subtask> getEpicSubtasks(int taskId);

    List<Task> getHistory();

    Set<Task> getPrioritizedTasks();

}