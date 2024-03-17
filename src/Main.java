
import ru.yandex.app.service.HistoryManager;
import ru.yandex.app.service.InMemoryTaskManager;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.service.Managers;
import ru.yandex.app.service.TaskManager;

import java.util.List;


public class Main {
    public static void main(String[] args) {


        List<Task> history;
        Task task1 = new Task("task 1", "пример простой задачи 1");
        Task task2 = new Task("task 2", "пример простой задачи 2");
        //TaskManager manager = new InMemoryTaskManager();
        TaskManager manager = Managers.getDefault();
        //HistoryManager history = Managers.getDefaultHistory();
        int taskId1 = manager.addTask(task1);
        int taskId2 = manager.addTask(task2);


        manager.createEpic(new Epic("Устроить праздник", "Устраиваем званный ужин")); //1
        manager.addSubTaskInEpic(1, new Subtask("Сходить в магазин", "Купить макароны", 100L, "01.03.2024 12:30"));//2
        manager.addSubTaskInEpic(1, new Subtask("Вернутся домой", "Там будем готовить", 320L, "01.03.2024 12:30")); //3
        manager.addSubTaskInEpic(1, new Subtask("Готовим ужин", "Желательно вкусный", 470L, "14.05.2024 12:30")); //4
        manager.addSubTaskInEpic(1, new Subtask("Зовем гостей", "Ждем гостей", 91L, "12.02.2024 12:30")); //5

        manager.createEpic(new Epic("Доехать до дома", "Едем домой")); //6


        manager.getTask(taskId1);
        manager.getTask(taskId2);
        history = manager.getHistory();
        System.out.println(history);

        //--

        Epic epic1 = new Epic("epic 1", "пример эпика 1");
        int epicId1 = manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask 1", "пример подзадачи 1", epicId1);
        Subtask subtask2 = new Subtask("subtask 2", "пример подзадачи 2", epicId1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);


        manager.getSubtask(subtask1.getId());
        history = manager.getHistory();
        System.out.println(history);
        //--

        Epic epic2 = new Epic("epic 2", "пример эпика 2");
        int epicId2 = manager.addEpic(epic2);
        Subtask subtask3 = new Subtask("subtask 3", "пример подзадачи 3", epicId2);
        manager.addSubtask(subtask3);

        //--

        /*
        System.out.println(manager.getPrintTaskMap());
        System.out.println(manager.getPrintEpicMap());
        System.out.println(manager.getPrintSubtaskMap());
*/



    }
}
