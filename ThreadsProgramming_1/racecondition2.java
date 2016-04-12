class MyThread2 extends Thread {
    static int count = 0;
    int sleeptime = 0;

    public MyThread2(int time) {
        if (time != 0) sleeptime=time;
    }

    public void run() {
        try { sleep(sleeptime); } catch (Exception e) {}
            updateCount();
    }

    public void updateCount() {
        count++;
    }

}

class RaceCondition2 {

public static void main(String[] args) {
    int i;

    if (args.length < 2) {
        System.out.println("Usage: java RaceCondition2 #nb_threads #sleep_time");
        System.out.println("e.g., 1000 threads & 10 ms");
        return;
    }
   
    for (i=0;i<Integer.valueOf(args[0]);i++)
        new MyThread2(Integer.valueOf(args[1])).start();

    System.out.println("Total count = " + MyThread2.count +
               " vs. total number of threads = " + i);
}

}
