package ru.yandex.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import managers.TaskManager;
import models.Epic;
import models.Subtask;
import models.Task;
import models.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    public void resetAllIdTo0() {
        Task.setCount(0);
    }

    @Test
    public void tasksShouldCreatedAndAddToList() {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);
        final int taskId = task.getId();
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Task not found after create");
        assertEquals(task, savedTask, "Tasks with same id not are equals");
        assertEquals(1, task.getId(), "task has an invalid id");
        assertEquals(1, taskManager.getAllTasks().size(), "task is not added to TasksList");

    }

    @Test
    void subtasksShouldCreatedAndAddToList() {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);

        final int subtaskId = subtask.getId();
        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Subtask not found after create");
        assertEquals(subtask, savedSubtask, "Subtasks with same id not are equals");
        assertEquals(2, subtask.getId(), "Subtask has an invalid id");
        assertEquals(1, taskManager.getAllSubtasks().size(), "Subtask is not added to SubtaskList");
        assertEquals(1, taskManager.getEpicTasks(epic.getId()).size(), "Subtask not found in EpicSubtaskList");

    }

    @Test
    void epicsShouldCreatedAndAddToList() {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);

        final int epicId = epic.getId();
        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Epic not found after create");
        assertEquals(epic, savedEpic, "Epics with same id not are equals");
        assertEquals(1, epic.getId(), "Epic has an invalid id");
        assertEquals(1, taskManager.getAllEpics().size(), "Epic is not added to EpicList");
    }


    @Test
    void shouldDeleteTaskById() {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);

        taskManager.deleteTaskById(task.getId());

        assertEquals(0, taskManager.getAllTasks().size(), "Task has not been removed from the TaskMap");

    }

    @Test
    void shouldGetAllTasks() {
        assertEquals(0, taskManager.getAllTasks().size(), "TaskList not empty");
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);

        assertEquals(1, taskManager.getAllTasks().size(), "task is not added to TasksList");
    }


    @Test
    void shouldGetAllSubtasks() {
        assertEquals(0, taskManager.getAllSubtasks().size(), "SubtaskList not empty");
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);

        assertEquals(1, taskManager.getAllSubtasks().size(), "Subtask is not added to SubtaskList");
    }


    @Test
    void shouldGetAllEpics() {
        assertEquals(0, taskManager.getAllEpics().size(), "EpicList not empty");

        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);

        assertEquals(1, taskManager.getAllEpics().size(), "Epic is not added to EpicList");
    }


    @Test
    void EpicShouldChangeStatus() {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Epic status after init is invalid");

        epic.setTaskStatus(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getTaskStatus(), "Epic status after change is invalid");
    }


    @Test
    void EpicShouldChangeStatusAfterChangeStatusSubtasks() {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);

        subtask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateEpics(epic);

        assertEquals(TaskStatus.DONE, epic.getTaskStatus(), "Epic status after change subtask status is invalid");
    }


    @Test
    void shouldUpdateTask() {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);

        task.setName("newName");
        task.setDuration(Duration.ofMinutes(50));
        taskManager.updateTask(task);

        assertEquals("newName", taskManager.getTaskById(task.getId()).getName(), "Name task is not updated");
        assertEquals("PT50M", taskManager.getTaskById(task.getId()).getDuration().toString(), "Duration task is not updated");

    }


    @Test
    void shouldUpdateSubtask() {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);


        subtask.setId(10);
        subtask.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtasks(subtask);


        assertEquals(10, subtask.getId(),
                "Subtask id is not updated");
        assertEquals(TaskStatus.IN_PROGRESS, subtask.getTaskStatus(), "Subtask Status is not updated");

    }


    @Test
    void shouldUpdateEpic() {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);

        epic.setDescription("newDescription");
        taskManager.updateEpics(epic);

        assertEquals("newDescription", epic.getDescription(), "Epic description is not updated");


    }

    @Test
    void shouldGetHistory() {
        assertEquals(taskManager.getHistory().size(), 0, "History is fill");

        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createTask(task);
        taskManager.createEpic(epic);

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        assertEquals(taskManager.getHistory().size(), 2, "History has incorrect fill");

    }

    @Test
    public void shouldGetEndTime() {
        Task task = new Task("Title", "Description", Duration.ofDays(10), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0), task.getEndTime());
    }

    @Test
    public void shouldReturnPrioritizedTasks() {
        Task task1 = new Task("task1", "taskDescription1", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        Task task2 = new Task("task1", "taskDescription1", Duration.ofMinutes(10), LocalDateTime.of(2024, 1, 5, 0, 0));
        Task task3 = new Task("task1", "taskDescription1", Duration.ofMinutes(15), LocalDateTime.of(2024, 2, 5, 0, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        assertEquals(3, taskManager.getPrioritizedTasks().size(), "PrioritizedTasks has incorrect size");
    }

    @Test
    public void shouldDeleteEpicById() {
        Epic epic1 = new Epic("epic1", "epicDescription");
        Epic epic2 = new Epic("epic2", "epicDescription");
        Epic epic3 = new Epic("epic3", "epicDescription");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createEpic(epic3);

        taskManager.deleteEpicById(2);

        assertEquals(2, taskManager.getAllEpics().size(), "Incorrect delete Epic by id");

    }

    @Test
    public void shouldDeleteSubtaskById() {
        Epic epic = new Epic("epic1", "epicDescription");
        Subtask subtask1 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 6, 20, 0), Duration.ofMinutes(10));
        Subtask subtask3 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 7, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        assertEquals(3, subtask2.getId(), "Subtask id is incorrect");
        assertEquals(3, taskManager.getEpicTasks(epic.getId()).size(), "Epic subtasks size is incorrect");

        taskManager.deleteSubtaskById(3);

        assertEquals(2, taskManager.getEpicTasks(epic.getId()).size(), "Epic subtasks size after deleting is incorrect");
    }

    @Test
    public void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("epic1", "epicDescription");
        Subtask subtask1 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 6, 20, 0), Duration.ofMinutes(10));
        Subtask subtask3 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 7, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        assertEquals(3, taskManager.getEpicTasks(epic.getId()).size(), "Epic subtasks size is incorrect");

        taskManager.deleteAllSubtasks();

        assertEquals(0, taskManager.getAllSubtasks().size(), "Epic subtasks size after deleting is incorrect");

    }

    @Test
    public void shouldReturnNewStatusEpicWithoutSubtask() {
        Epic epic = new Epic("epic1", "epicDescription");
        taskManager.createEpic(epic);
        assertEquals("NEW", epic.getTaskStatus().toString(), "incorrect Epic status without creating subtasks");
    }

    @Test
    public void shouldReturnNewStatusEpicWithStatusSubtaskNew() {
        Epic epic = new Epic("epic1", "epicDescription");
        Subtask subtask1 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 6, 20, 0), Duration.ofMinutes(10));
        Subtask subtask3 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 7, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        assertEquals("NEW", epic.getTaskStatus().toString(), "incorrect Epic status");
        assertEquals(epic.getId(), subtask1.getEpicId());
        assertEquals(epic.getId(), subtask2.getEpicId());
    }

    @Test
    public void TasksWithIntersectionShouldNotBeCreated() {
        Task task1 = new Task("task1", "taskDescription1", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        Task task2 = new Task("task1", "taskDescription1", Duration.ofMinutes(5), LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertEquals(1, taskManager.getAllTasks().size(), "tasks with intersection have be created");

    }

    @Test
    public void SubtasksWithIntersectionShouldNotBeCreated() {
        Epic epic = new Epic("epic1", "epicDescription");
        Subtask subtask1 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(1, taskManager.getAllSubtasks().size(), "subtasks with intersection have be created");
    }


}
}