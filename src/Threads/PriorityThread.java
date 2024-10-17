package Threads;

public class PriorityThread {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " says hi ");
        Thread thread1 = new Thread(() -> {
            System.out.println("Thread 1 says hi ");
    }    );
    thread1.setPriority(Thread.MAX_PRIORITY);
    thread1.start();
}
}
