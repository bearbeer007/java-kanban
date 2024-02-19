package ru.yandex.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void emptyHistory() {
        List<Task> list = historyManager.getHistory();
        assertNotNull(list);
        assertEquals(0, list.size());
    }



    @Test
    public void testAddAndRemoveTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.DONE);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(3, historyManager.getHistory().size());

        // Удаляем задачу из начала истории
        historyManager.remove(task1.getId());
        assertAll(
                () -> assertEquals(2, historyManager.getHistory().size()),
                () -> assertEquals(task2, historyManager.getHistory().get(0)),
                () -> assertEquals(task3, historyManager.getHistory().get(1))
        );

        // Удаляем задачу из середины истории
        historyManager.remove(task2.getId());
        assertAll(
                () -> assertEquals(1, historyManager.getHistory().size()),
                () -> assertEquals(task3, historyManager.getHistory().get(0))
        );

        // Удаляем задачу из конца истории
        historyManager.remove(task3.getId());
        assertEquals(0, historyManager.getHistory().size());
    }

}
