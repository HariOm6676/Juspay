package Threads;

public class DemonThreadExample {
    public static void main(String[] args) {
        Thread demonThread = new Thread(new DemonHelper());
        Thread userThread = new Thread(new UserThreadHelper());
        demonThread.setDaemon(true);
        demonThread.start();
        userThread.start();
    }
}

class DemonHelper implements Runnable {
    @Override
    public void run() {
        int count=0;
        while(count<100)
        {
            try {
                Thread.sleep((1000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            count++;
            System.out.println("Demon Thread is running");
        }
    }
}
class UserThreadHelper implements Runnable {
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("User Thread is done with the exection");
}
}