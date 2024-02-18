package ru.yandex.app.service;

import ru.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private Node first = null;
    private Node last = null;
    private final HashMap<Integer, Node> indexTask = new HashMap<>();

    private static class Node {
        Task value;
        Node prevTask;
        Node nextTask;

        Node(Task value) {
            this.value = value;
        }

        Node getPrev() {
            return prevTask;
        }

        Node getNext() {
            return nextTask;
        }

        void setPrev(Node prevTask) {
            this.prevTask = prevTask;
        }

        void setNext(Node nextTask) {
            this.nextTask = nextTask;
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


    @Override
    public void removeAllTask() {
        indexTask.clear();
    }

    private Node linkLast(Task task) {
        Node node = new Node(task);
        if (first == null) {
            first = node;
            last = node;
        } else {
            node.setPrev(last);
            last.setNext(node);
            last = node;
        }
        return node;
    }

    private List<Task> getTasks() {
        ArrayList<Task> res = new ArrayList<>();
        Node node = first;
        while (node != null) {
            res.add(node.value);
            node = node.getNext();
        }
        return res;
    }

    private void removeNode(Node node) {
        if (node == null) return;
        if (node == first) {
            first = node.getNext();
        }
        if (node == last) {
            last = node.getPrev();
        }
        Node prev = node.getPrev();
        Node next = node.getNext();
        if (prev != null) {
            prev.setNext(next);
        }
        if (next != null) {
            next.setPrev(prev);
        }
        node.setPrev(null);
        node.setNext(null);
        node.value = null;
    }
}