package ru.yandex.app.service;

import ru.yandex.app.exceptions.ManagerSaveException;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.Task;
import ru.yandex.app.model.TaskType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;
    private static final String HEADER = "id,type,name,status,description,epic,startTime,duration,endTime";
    private static final CSVTaskFormatter csvTaskFormatter = new CSVTaskFormatter();

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    @Override
    public int addTask(Task task) {
        super.addTask(task);
        save();
        return task.getTaskId();
    }

    @Override
    public int updateTask(Task task) {
        super.updateTask(task);
        save();
        return task.getTaskId();
    }

    @Override
    public void removeTaskById(int taskId) {
        super.removeTaskById(taskId);
        save();
    }

    @Override
    public int addSubTask(Subtask subtask) {
        super.addSubTask(subtask);
        save();
        return subtask.getTaskId();
    }

    @Override
    public int updateSubTask(Subtask subtask) {
        super.updateSubTask(subtask);
        save();
        return subtask.getTaskId();
    }

    @Override
    public void removeSubTaskById(int taskId) {
        super.removeSubTaskById(taskId);
        save();
    }

    @Override
    public int addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic.getTaskId();
    }

    @Override
    public int updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic.getTaskId();
    }

    @Override
    public void removeEpicById(int taskId) {
        super.removeEpicById(taskId);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    @Override
    public Task getTaskByTaskId(int taskId) {
        super.getTaskByTaskId(taskId);
        save();
        return tasks.get(taskId);
    }

    @Override
    public Epic getEpicByTaskId(int taskId) {
        super.getEpicByTaskId(taskId);
        save();
        return epics.get(taskId);
    }

    @Override
    public Subtask getSubTaskByTaskId(int taskId) {
        super.getSubTaskByTaskId(taskId);
        save();
        return subtasks.get(taskId);
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
            writer.write(HEADER);
            writer.newLine();
            for (Task task : tasks.values()) {
                writer.write(csvTaskFormatter.toString(task));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(csvTaskFormatter.toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(csvTaskFormatter.toString(subtask));
                writer.newLine();
            }
            writer.newLine();
            writer.write(csvTaskFormatter.historyToString(historyManager));
        } catch (IOException exception) {
            throw new ManagerSaveException("Error writing to file.", exception);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file.toPath());
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return fileBackedTaskManager;
            }
            boolean hasHEADER = lines.get(0).trim().equals(HEADER);
            if (hasHEADER) {
                lines.remove(0);
            }

            int lastString = lines.size() - 1;
            String stringHistory = lines.get(lastString);
            List<Integer> history = csvTaskFormatter.historyFromString(stringHistory);
            if (!history.isEmpty()) {
                lines.remove(lastString);
            }

            for (String line : lines) {
                if (!line.isBlank() && !line.equals("\n")) {
                    Task task = csvTaskFormatter.fromString(line);
                    TaskType type = task.getType();
                    if (type == TaskType.EPIC) {
                        Epic epic = (Epic) task;
                        fileBackedTaskManager.epics.put(epic.getTaskId(), epic);
                        fileBackedTaskManager.prioritizedTasks.add(epic); // Добавление эпика в отсортированный список
                    } else if (type == TaskType.SUBTASK) {
                        Subtask subtask = (Subtask) task;
                        fileBackedTaskManager.subtasks.put(subtask.getTaskId(), subtask);
                        fileBackedTaskManager.prioritizedTasks.add(subtask); // Добавление подзадачи в отсортированный список
                        Epic parentEpic = fileBackedTaskManager.epics.get(((Subtask) task).getParentTaskId());
                        if (parentEpic != null) {
                            parentEpic.addSubtaskId(subtask.getTaskId()); // Добавление подзадачи в список внутри эпика
                        }
                    } else if (type == TaskType.TASK) {
                        fileBackedTaskManager.tasks.put(task.getTaskId(), task);
                        fileBackedTaskManager.prioritizedTasks.add(task); // Добавление задачи в отсортированный список
                    }
                }
            }
            for (Integer id : history) {
                if (fileBackedTaskManager.tasks.containsKey(id)) {
                    fileBackedTaskManager.historyManager.add(fileBackedTaskManager.tasks.get(id));
                } else if (fileBackedTaskManager.subtasks.containsKey(id)) {
                    fileBackedTaskManager.historyManager.add(fileBackedTaskManager.subtasks.get(id));
                } else if (fileBackedTaskManager.epics.containsKey(id)) {
                    fileBackedTaskManager.historyManager.add(fileBackedTaskManager.epics.get(id));
                }
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Error reading from file.", exception);
        }
        return fileBackedTaskManager;
    }
}