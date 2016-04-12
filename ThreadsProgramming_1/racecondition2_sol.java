class MyThread2 extends Thread {
	static Integer count = new Integer(0);
	int sleeptime = 0;

	public MyThread2(int time) {
		if (time != 0) sleeptime=time;
	}

	public void run() {
		try { sleep(sleeptime); } catch (Exception e) {}
			updateCount();
	}

	public synchronized void updateCount() {
		count++;
	}

}

class RaceCondition2 {

	public static void main(String[] args) throws InterruptedException{
		int i, tot;
		
		if (args.length < 2) {
			System.out.println("Usage: java RaceCondition2 #nb_threads #sleep_time");
			System.out.println("e.g., 1000 threads & 10 ms");
			return;
		}

		tot = Integer.valueOf(args[0]);
		MyThread2[] list = new MyThread2[tot];

		for (i=0;i<tot;i++){
			list[i] = new MyThread2(Integer.valueOf(args[1]));
			list[i].start();
		}
		tot = i;
	
		for(i = 0; i < tot; i++)
			list[i].join();
		//After every thread has finished...
		System.out.println("Total count = " + MyThread2.count +
				   " vs. total number of threads = " + i);
	}

}
