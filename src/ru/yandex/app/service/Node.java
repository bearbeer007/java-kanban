package ru.yandex.app.service;

class Node<T> {
    protected T data;
    protected Node<T> prev;
    protected Node<T> next;

    protected Node(Node<T> prev, T data, Node<T> next) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }
}