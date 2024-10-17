package Executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPool {
    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new ProbeTask(), 1000, 2000, TimeUnit.MILLISECONDS);
        try {
            if(service.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                service.shutdown();
            }
        } catch (InterruptedException ex) {
            service.shutdown();
        }
    }
}
class ProbeTask implements Runnable{
    @Override
    public void run() {
        System.out.println("Probe task executed");
    }
}
