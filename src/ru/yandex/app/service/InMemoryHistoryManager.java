package ru.yandex.app.service;

import ru.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private Node first;
    private Node last;
    private final HashMap<Integer, Node> indexTask = new HashMap<>();

    private static class Node {
        Task value;
        Node prevTask;
        Node nextTask;

        Node(Task value) {
            this.value = value;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        Node node = linkLast(task);
        indexTask.put(task.getId(), node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (indexTask.containsKey(id)) {
            Node node = indexTask.get(id);
            removeNode(node);
            indexTask.remove(id);
        }
    }

    private Node linkLast(Task task) {
        Node node = new Node(task);
        if (first == null) {
            first = node;
            last = node;
        } else {
            node.prevTask = last;
            last.nextTask = node;
            last = node;
        }
        return node;
    }

    private List<Task> getTasks() {
        ArrayList<Task> res = new ArrayList<>();
        Node node = first;
        while (node != null) {
            res.add(node.value);
            node = node.nextTask;
        }
        return res;
    }

    private void removeNode(Node node) {
        if (node == null) return;
        if (node == first) {
            first = node.nextTask;
        }
        if (node == last) {
            last = node.prevTask;
        }
        Node prev = node.prevTask;
        Node next = node.nextTask;
        if (prev != null) {
            prev.nextTask = next;
        }
        if (next != null) {
            next.prevTask = prev;
        }
    }
}