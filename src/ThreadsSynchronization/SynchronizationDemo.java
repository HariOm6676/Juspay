package ThreadsSynchronization;

public class SynchronizationDemo {
    static int count=0;
    public static void main(String[] args) {
        Thread one= new Thread(()->{
            for(int i=0;i<20000;i++)
            increament();
        });
        Thread two= new Thread(()->{
            for(int i=0;i<20000;i++)
            increament();
        });
        one.start();
        two.start();
        try {
            one.join();
            two.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                }
                System.out.println(count);
                }
                private synchronized static void increament(){
                    count++;
                }
                }
            

