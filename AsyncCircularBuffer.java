package EPAM2015_lab12;

import java.util.LinkedList;
import java.util.Random;

public class AsyncCircularBuffer<T> {

    private T[] container;
    boolean flag;
    int front;
    int rare;
    boolean annotNeeded;

    public boolean isAnnotNeeded() {
        return annotNeeded;
    }

    public void setAnnotNeeded(boolean annotNeeded) {
        this.annotNeeded = annotNeeded;
    }

    @SuppressWarnings("unchecked")
    public AsyncCircularBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity value: " + capacity);
        }
        this.container = (T[]) new Object[capacity];
        front = 0;
        rare = 0;
    }

    public synchronized void put(T el) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        container[rare++] = el;
        if (rare == container.length) {
            rare = 0;
            flag = !flag;
        }
        if (annotNeeded) {
            System.out.printf("Thread #%s puts: %s.%n", Thread.currentThread().getName(), el.toString());
            System.out.println(this);
        }
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        T toReturn = container[front];
        container[front++] = null;
        if (front == container.length) {
            front = 0;
            flag = !flag;
        }
        if (annotNeeded) {
            System.out.printf("Thread #%s takes: %s.%n", Thread.currentThread().getName(), toReturn.toString());
            System.out.println(this);
        }
        notifyAll();
        return toReturn;
    }

    public boolean isEmpty() {
        return front == rare && !flag;
    }

    public boolean isFull() {
        return front == rare && flag;
    }

    public int size() {
        return flag ? container.length - front + rare : front - rare;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < container.length; i++) {
            if (i == front) {
                builder.append("F=");
            }
            if (i == rare) {
                builder.append("R=");
            }
            builder.append(container[i] == null ? "null" : container[i].toString());
            builder.append(i != container.length - 1 ? ", " : "");
        }
        builder.insert(0, "[");
        builder.append("]");
        return builder.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        AsyncCircularBuffer<Integer> buf = new AsyncCircularBuffer<>(10);
        buf.setAnnotNeeded(true);
        LinkedList<Thread> consumers = new LinkedList<>();
        LinkedList<Thread> producers = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            consumers.add(new Thread() {
                Random rand = new Random();

                public void run() {
                    for (int j = 0; j < 5; j++) {
                        try {
                            sleep(rand.nextInt(500));
                            buf.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            producers.add(new Thread() {
                Random rand = new Random();

                public void run() {
                    for (int j = 0; j < 5; j++) {
                        try {
                            sleep(rand.nextInt(500));
                            buf.put(rand.nextInt(100));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            consumers.getLast().start();
            producers.getLast().start();
        }
    }
}