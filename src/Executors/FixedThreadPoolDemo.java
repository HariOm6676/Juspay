package Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class FixedThreadPoolDemo {
    public static void main(String[] args) {
        try(ExecutorService service = Executors.newFixedThreadPool(2))
        {
            for(int i=0;i<10;i++)
            service.execute(new Work(i));
        }
    }
}
class Work implements Runnable {
    private final int taskId;
    public Work(int taskId) {
        this.taskId=taskId;
    }
    @Override
    public void run() {
        System.out.println("Task "+taskId+" is executed by "+ Thread.currentThread().getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
