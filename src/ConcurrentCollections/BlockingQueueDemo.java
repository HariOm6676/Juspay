package ConcurrentCollections;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo {
    static final int QUEUE_CAPACITY= 10;
    static BlockingQueue<Integer> taskQueue= new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    public static void main(String[] args) {
        // Producer
        Thread producer= new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    taskQueue.put(i);
                    System.out.println("Task Produced : " +i);
                    Thread.sleep(1000); 
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread consumer1= new Thread(()->{
            try{
                while (true) { 
                    int task= taskQueue.take();
                    processTask(task,"Consumer1");
                }
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        Thread consumer2= new Thread(()->{
            try{
                while (true) { 
                    int task= taskQueue.take();
                    processTask(task,"Consumer2");
                }
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        producer.start();
        consumer1.start();
        consumer2.start();
    }
    private static void processTask(int task, String consumerName) throws InterruptedException{
        System.out.println("Task being Processed by "+ consumerName + " : "+ task);
        Thread.sleep(1000);
        System.out.println("Task Processed by "+ consumerName + " : "+ task);
    }
}
