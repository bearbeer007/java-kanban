package ru.yandex.app.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.service.FileBackedTaskManager;
import ru.yandex.app.service.TaskManager;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest {

    private TaskManager fileBackedTaskManager;
    private File tmpFile;

    @BeforeEach
    public void setUp() {
        try {
            tmpFile = File.createTempFile("data", ".csv");
            fileBackedTaskManager = new FileBackedTaskManager(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file", e);
        }
    }

    @AfterEach
    public void tearDown() {
        if (tmpFile != null && tmpFile.exists()) {
            tmpFile.delete();
        }
    }

    private Task createTaskForTests() {
        Task task = new Task("Task name", "Task description");
        fileBackedTaskManager.addTask(task);
        return task;
    }

    private Epic createEpicForTests() {
        Epic epic = new Epic("Epic name", "Epic description");
        fileBackedTaskManager.addEpic(epic);
        return epic;
    }

    private Subtask createSubtaskForTests(Integer epicId) {
        Subtask subtask = new Subtask("Subtask name", "Subtask description", epicId);
        fileBackedTaskManager.addSubtask(subtask);
        return subtask;
    }

    @Test
    public void shouldReturnHistoryAndDataAfterCreating() {
        Task task = createTaskForTests();
        Epic epic = createEpicForTests();
        Subtask subtask1 = createSubtaskForTests(epic.getId());
        Subtask subtask2 = createSubtaskForTests(epic.getId());

        fileBackedTaskManager.getTaskById(task.getId());
        fileBackedTaskManager.getEpicById(epic.getId());
        fileBackedTaskManager.getSubtaskById(subtask1.getId());
        fileBackedTaskManager.getSubtaskById(subtask2.getId());

        TaskManager newFBTM;
        try {
            newFBTM = FileBackedTaskManager.loadFromFile(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FileBackedTaskManager from file", e);
        }

        assertEquals(1, newFBTM.getAllTasks().size());
        assertEquals(2, newFBTM.getAllSubtasks().size());
        assertEquals(1, newFBTM.getAllEpics().size());
        assertEquals(4, newFBTM.getHistory().size());
    }

    @Test
    public void shouldReturnEmptyHistoryAndDataAfterDeleting() {
        fileBackedTaskManager.deleteAllTasks();
        fileBackedTaskManager.deleteAllEpics();

        TaskManager newFBTM;
        try {
            newFBTM = FileBackedTaskManager.loadFromFile(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FileBackedTaskManager from file", e);
        }

        assertEquals(0, newFBTM.getAllTasks().size());
        assertEquals(0, newFBTM.getAllSubtasks().size());
        assertEquals(0, newFBTM.getAllEpics().size());
        assertEquals(0, newFBTM.getHistory().size());
    }
}