import org.junit.jupiter.api.BeforeEach;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void setUp() {
        taskManager = new InMemoryTaskManager();
    }


    @Test
    void getEpicSubtasks_ShouldReturnListOfSubtasks() {

        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId,
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId,
                LocalDateTime.of(2024, 3, 6, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addSubTask(subtask1);
        taskManager.addSubTask(subtask2);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        assertNotNull(subtasks, "Список подзадач не должен быть null.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач.");
        assertTrue(subtasks.contains(subtask1), "Список подзадач не содержит subtask1.");
        assertTrue(subtasks.contains(subtask2), "Список подзадач не содержит subtask2.");
    }

    @Test
    void setStatusEpic_ShouldSetCorrectStatus() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId,
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.addSubTask(subtask1);
        taskManager.setStatusEpic(epicId);
        assertEquals(TaskStatus.DONE, taskManager.getEpicByTaskId(epicId).getStatus(),
                "Статус эпика должен быть DONE.");
    }

    @Test
    void setEndTimeEpic_ShouldSetCorrectTimeAndDuration() {

        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId,
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId,
                LocalDateTime.of(2024, 3, 6, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addSubTask(subtask1);
        taskManager.addSubTask(subtask2);
        taskManager.setEndTimeEpic(epicId);
        Epic updatedEpic = taskManager.getEpicByTaskId(epicId);
        assertNotNull(updatedEpic.getStartTime(), "Время начала эпика не должно быть null.");
        assertNotNull(updatedEpic.getEndTime(), "Время окончания эпика не должно быть null.");
        assertNotNull(updatedEpic.getDuration(), "Продолжительность эпика не должна быть null.");
    }

    @Test
    void checkIntersectionTasks_ShouldThrowExceptionIfTasksIntersect() {
        Task task1 = new Task("Task 1", "Description",
                LocalDateTime.of(2024, 3, 5, 15, 0, 0), Duration.ofMinutes(30));
        taskManager.addTask(task1);
        Task task2 = new Task("Task 2", "Description",
                LocalDateTime.of(2024, 3, 5, 15, 15, 0), Duration.ofMinutes(30));
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            taskManager.checkIntersectionTasks(task2);
        });
        String expectedMessage = "Задачи не должны пересекаться по времени выполнения";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Должно быть выброшено исключение о пересечении задач.");
    }
}