package ThreadsSynchronization;

public class WaitAndNotify {
    private static final Object LOCK = new Object();
    public static void main(String[] args) {
         Thread one = new Thread(()->{
            try {
                one();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         });
         Thread two= new Thread(()-> {
            try {
                two();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         });
         one.start();
         two.start();
    }    
    private static void one() throws InterruptedException{
        synchronized (LOCK){
            System.out.println("Hello from thread one");
            LOCK.wait();
            System.err.println("Back again in thread one");
        }
    }
    private static void two() throws InterruptedException{
        synchronized (LOCK) {
            System.out.println("Hello from thread two");
            LOCK.notify();
            System.err.println("Hello from thread two even after notifying");
        }
    }
    }

