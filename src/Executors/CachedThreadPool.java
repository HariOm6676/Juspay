package Executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPool {
    public static void main(String[] args) {
        try(ExecutorService service = Executors.newCachedThreadPool();)
        {
            for (int i = 0; i < 1000; i++) {
                service.execute(new Task(i));
            }
        }
    }
}
