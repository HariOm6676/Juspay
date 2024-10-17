package ConcurrentCollections;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {
    public static void main(String args[]) throws InterruptedException {
        int numberOfChefs=3;
        CountDownLatch latch = new CountDownLatch(numberOfChefs);
        new Thread(new Chef("Chef A", "Samosa", latch)).start();
        new Thread(new Chef("Chef B", "Chaat", latch)).start();
        new Thread(new Chef("Chef C", "Mirchi Vada", latch)).start();
        latch.await();
        System.out.println("All dishes are ready");
    }  
}
class Chef implements Runnable{
    private final String name;
    private final String dish;
    private final CountDownLatch latch;
    public Chef(String name, String dish, CountDownLatch latch) {
        this.name = name;
        this.dish = dish;
        this.latch = latch;
    }
    @Override
    public void run() {
        System.out.println(name +" is Preparing "+ dish);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(name +" has prepared "+ dish);
        latch.countDown();
    }
}
