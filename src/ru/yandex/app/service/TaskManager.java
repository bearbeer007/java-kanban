package ru.yandex.app.service;

import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaskManager {

    void createEpic(Epic epic);

    Optional<Epic> receiveOneEpic(int epicId);

    /**
     * История просмотров задач, вернем последние 10 просмотренных задач
     */
    List<Task> getHistory();

    /**
     * добавим задачу
     */
    int addTask(Task task);

    /**
     * добавим эпик
     */
    int addEpic(Epic epic);

    /**
     * добавим подзадачу
     */
    int addSubtask(Subtask subtask);

    void addSubTaskInEpic(int epicId, Subtask subtask);

    Optional<Subtask> receiveSubtasksUseID(int subtaskId);

    void deleteUseID(int id);
    Optional<Task> receiveOneTask(int id);

    /**
     * методы обновления
     * обновим задачу
     */
    int updateTask(Task task);

    /**
     * обновим эпик
     */
    int updateEpic(Epic epic);

    /**
     * получить задачу по id
     */
    Task getTask(Integer id);

    /**
     * список всех задачь
     */
    List<Task> getAllTasks();

    /**
     * список всех эпиков
     */
    List<Epic> getAllEpics();

    /**
     * список всех подзадачь
     */
    List<Subtask> getAllSubtasks();

    /**
     * получить подзадачу по id
     */
    Subtask getSubtask(Integer id);

    /**
     * получить эпик по id
     */
    Epic getEpic(Integer id);

    /**
     * метод получения всех подзадач эпика по его идентификатору
     */
    List<Subtask> getSubtasksByEpicId(int epicId);

    /**
     * удалим задачу
     */
    boolean removeTask(Integer id);

    /**
     * удалим подзадачу
     */
    boolean removeSubtask(Integer id);

    boolean removeEpic(Integer id);

    /**
     * Удалим все подзадачи
     * При удалении всех подзадач, удаление эпиков осуществлять - не требуется.
     * Следует лишь пройтись по ним и почистить в них списки идентификаторов подзадач и обновить их статусы.
     */
    void removeAllSubtask();

    void removeAllEpic();

    void removeAllTask();

    List<Task> getPrintTaskMap();

    List<Epic> getPrintEpicMap();

    List<Subtask> getPrintSubtaskMap();
}
