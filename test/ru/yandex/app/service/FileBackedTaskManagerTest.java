package test;

import managers.FileBackedTaskManager;
import managers.TaskManager;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    TaskManager fileBackedTaskManager;
    File tmpFile;

    {
        try {
            tmpFile = File.createTempFile("data", ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @BeforeEach
    public void createFileBackTaskManager() {
        fileBackedTaskManager = new FileBackedTaskManager(tmpFile);
    }


    public Task createTaskForTests() {
        Task task = new Task("Task name", "Task description");
        fileBackedTaskManager.createTask(task);
        return task;
    }

    public Epic createEpicForTests() {
        Epic epic = new Epic("Epic name", "Epic description");
        fileBackedTaskManager.createEpic(epic);
        return epic;
    }

    public Subtask createSubtaskForTests(Integer epicId) {
        Subtask subtask = new Subtask("Subtask name", "Subtask description", epicId);
        fileBackedTaskManager.createSubtask(subtask);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }

        assertEquals(0, newFBTM.getAllTasks().size());
        assertEquals(0, newFBTM.getAllSubtasks().size());
        assertEquals(0, newFBTM.getAllEpics().size());
        assertEquals(0, newFBTM.getHistory().size());
    }


}


