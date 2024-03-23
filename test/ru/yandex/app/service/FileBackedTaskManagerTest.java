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

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tmpFile;

    @BeforeEach
    public void createFileBackTaskManager() throws IOException {
        tmpFile = File.createTempFile("test", ".csv");
        taskManager = new FileBackedTaskManager(tmpFile.toPath());
    }

    @Test
    public void shouldReturnTasksAndHistoryAfterCreating() {
        Task task = new Task("Task 1", "Test task 1",
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addTask(task);
        Epic epic = new Epic("Epic 1", "Test epic 1");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Test subtask 1", epic.getTaskId(),
                LocalDateTime.of(2024, 3, 5, 16, 0, 0), Duration.ofMinutes(30));
        taskManager.addSubTask(subtask);
        taskManager.getTaskByTaskId(1);
        TaskManager testFileBackedTaskManager;
        try {
            testFileBackedTaskManager = FileBackedTaskManager.loadFromFile(tmpFile);
        } catch (IOException exception) {
            throw new ManagerSaveException("Error reading from file.", exception);
        }
        assertEquals(1, testFileBackedTaskManager.getAllTasks().size());
        assertEquals(1, testFileBackedTaskManager.getAllSubTasks().size());
        assertEquals(1, testFileBackedTaskManager.getAllEpics().size());
        assertEquals(1, testFileBackedTaskManager.getHistory().size());
    }

    @Test
    public void shouldReturnTasksAndHistoryAfterLoading() {
        File tasksForTest = new File(String.valueOf(Path.of("src/main/resources/tasksForTest.csv")));
        TaskManager testLoadFileBackedTaskManager;
        try {
            testLoadFileBackedTaskManager = FileBackedTaskManager.loadFromFile(tasksForTest);
        } catch (IOException exception) {
            throw new ManagerSaveException("Error reading from file.", exception);
        }
        assertEquals(1, testLoadFileBackedTaskManager.getAllTasks().size());
        assertEquals(1, testLoadFileBackedTaskManager.getAllEpics().size());
        assertEquals(1, testLoadFileBackedTaskManager.getAllSubTasks().size());
        assertEquals(2, testLoadFileBackedTaskManager.getHistory().size());
    }

    @Test
    public void shouldReturnDoesNotThrowAfterCreateEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".csv");
        FileBackedTaskManager fileManager = new FileBackedTaskManager(emptyFile.toPath());
        assertDoesNotThrow(fileManager::save);
    }

    @Test
    public void shouldReturnEmptyAfterLoadEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".csv");
        TaskManager testLoadFileBackedTaskManager;
        testLoadFileBackedTaskManager = FileBackedTaskManager.loadFromFile(emptyFile);
        assertEquals(0, testLoadFileBackedTaskManager.getAllTasks().size());
        assertEquals(0, testLoadFileBackedTaskManager.getAllEpics().size());
        assertEquals(0, testLoadFileBackedTaskManager.getAllSubTasks().size());
        assertEquals(0, testLoadFileBackedTaskManager.getHistory().size());
    }

    @Test
    public void shouldPreserveOrderAfterReload() throws IOException {
        // Добавление задач в taskManager
        Task task1 = new Task("Task 1", "Test task 1",
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addTask(task1);
        Task task2 = new Task("Task 2", "Test task 2",
                LocalDateTime.of(2024, 3, 6, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addTask(task2);
        List<Task> originalTasks = new ArrayList<>(taskManager.getAllTasks());
        List<Subtask> originalSubtasks = new ArrayList<>(taskManager.getAllSubtasks());
        List<Project> originalProjects = new ArrayList<>(taskManager.getAllProjects());
        // Загрузка taskManager из файла
        TaskManager loadedTaskManager = FileBackedTaskManager.loadFromFile(tmpFile);
        // Получение порядка задач из загруженного taskManager
        List<Task> loadedTasks = new ArrayList<>(loadedTaskManager.getAllTasks());
        List<Subtask> loadedSubtasks = new ArrayList<>(loadedTaskManager.getAllSubtasks());
        List<Project> loadedProjects = new ArrayList<>(loadedTaskManager.getAllProjects());
        // Проверка сохранения порядка для каждого списка
        assertEquals(originalTasks, loadedTasks, "The order of tasks should be preserved after reload.");
        assertEquals(originalSubtasks, loadedSubtasks, "The order of subtasks should be preserved after reload.");
        assertEquals(originalProjects, loadedProjects, "The order of projects should be preserved after reload.");
    }

}