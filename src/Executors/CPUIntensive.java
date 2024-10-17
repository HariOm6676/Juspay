package Executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CPUIntensive {
    public static void main(String[] args) {
        int cores= Runtime.getRuntime().availableProcessors();
        System.out.println(cores);
        ExecutorService service= Executors.newFixedThreadPool(cores);
        for(int i=0;i<20;i++)
        {
            service.execute(new CPUTask());
        }
    }
}
class CPUTask implements Runnable{
    @Override
    public void run() {
        System.out.println("Some CPU intensive task being done by: "+Thread.currentThread().getName());
    }
}