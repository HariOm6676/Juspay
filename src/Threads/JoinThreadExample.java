package Threads;

public class JoinThreadExample {
    public static void main(String[] args) throws InterruptedException {
        Thread one = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Thread1 : "+i);
            }
        });
        Thread two = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Thread2 : "+i);
            }
        });
        System.err.println("Before Executing the threads");
        one.start();
        two.start();
        one.join();
        two.join();
        System.out.println("After Executing the threads");
    }
}
