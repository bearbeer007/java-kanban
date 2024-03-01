package ru.yandex.app.service;

import ru.yandex.app.exceptions.ManagerSaveException;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File fileForSaveData;

    public FileBackedTaskManager(File file) {
        this.fileForSaveData = file;
    }


    private void save() {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileForSaveData))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                bufferedWriter.write(CSVTaskFormatter.toString(task) + "\n");
            }
            for (Task task : getAllSubtasks()) {
                bufferedWriter.write(CSVTaskFormatter.toString(task) + "\n");
            }
            for (Task task : getAllEpics()) {
                bufferedWriter.write(CSVTaskFormatter.toString(task) + "\n");
            }
            bufferedWriter.write("\n");
            bufferedWriter.write(CSVTaskFormatter.historyToString(historyManagers));


        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }


    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        List<String> lines = Files.readAllLines(Path.of(file.toURI()), StandardCharsets.UTF_8);
        lines.remove(lines.get(0));

        if (lines.isEmpty()) {
            return manager;
        }

        int lastElemNum = lines.size() - 1;
        String history = lines.get(lastElemNum);
        lines.remove(lastElemNum);

        for (String line : lines) {
            if (!line.isBlank() && !line.equals("\n")) {
                Task task = CSVTaskFormatter.fromString(line);
                if (task.getTaskType() == TaskType.SUBTASK) {
                    manager.subtaskMap.put(task.getId(), (Subtask) task);
                } else if (task.getTaskType() == TaskType.EPIC) {
                    manager.epicMap.put(task.getId(), (Epic) task);
                } else if (task != null) {
                    manager.taskMap.put(task.getId(), task);
                }
            }
        }
        if (CSVTaskFormatter.historyFromString(history) != null) {
            for (Integer id : CSVTaskFormatter.historyFromString(history)) {
                if (manager.taskMap.containsKey(id)) {
                    manager.historyManagers.add(manager.taskMap.get(id));
                } else if (manager.epicMap.containsKey(id)) {
                    manager.historyManagers.add(manager.epicMap.get(id));
                } else {
                    manager.historyManagers.add(manager.subtaskMap.get(id));
                }
            }
        }
        return manager;
    }


    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int updateTask(Task task) {
        int id = super.updateTask(task);
        save();
        return id;
    }

    @Override
    public int updateEpic(Epic epic) {
        int id = super.updateEpic(epic);
        save();
        return id;
    }

    @Override
    public boolean removeTask(Integer id) {
        boolean success = super.removeTask(id);
        if (success) {
            save();
        }
        return success;
    }

    @Override
    public boolean removeSubtask(Integer id) {
        boolean success = super.removeSubtask(id);
        if (success) {
            save();
        }
        return success;
    }

    @Override
    public boolean removeEpic(Integer id) {
        boolean success = super.removeEpic(id);
        if (success) {
            save();
        }
        return success;
    }

    @Override
    public void removeAllSubtask() {
        super.removeAllSubtask();
        save();
    }

    @Override
    public void removeAllEpic() {
        super.removeAllEpic();
        save();
    }

    @Override
    public void removeAllTask() {
        super.removeAllTask();
        save();
    }


    public static void main(String[] args) throws IOException {
        FileBackedTaskManager fileManager = new FileBackedTaskManager(new File("saveTasks2.csv"));
        fileManager.addTask(new Task("task1", "Купить автомобиль"));
        fileManager.addEpic(new Epic("new Epic1", "Новый Эпик"));
        fileManager.addSubtask(new Subtask("New Subtask", "Подзадача", 2));
        fileManager.addSubtask(new Subtask("New Subtask2", "Подзадача2", 2));
        fileManager.getTask(1);
        fileManager.getEpic(2);
        fileManager.getSubtask(3);
        System.out.println(fileManager.getAllTasks());
        System.out.println(fileManager.getAllSubtasks());
        System.out.println(fileManager.getHistory());
        System.out.println("\n\n" + "new" + "\n\n");
        FileBackedTaskManager fileBackedTasksManager = loadFromFile(new File("saveTasks2.csv"));
        System.out.println(fileBackedTasksManager.getAllTasks());
        System.out.println(fileBackedTasksManager.getAllSubtasks());
        System.out.println(fileBackedTasksManager.getHistory());
    }

}