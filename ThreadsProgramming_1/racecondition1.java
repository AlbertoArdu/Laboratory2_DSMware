class MyThread1 implements Runnable {

    private static boolean flag;
	
	static int count = 0;

    public void shout() { System.out.println("in conditional"); }
  
    public void run() {
		flag = false;
		Thread.yield();
        flag = true;
		if(flag!=true) {
			shout();
		}
		flag = false;
    }
}

class RaceCondition1 {

    public static void main(String[] args) {
	int i;

	if (args.length < 1) {
	    System.out.println("Usage: java RaceCondition1 #nb_threads");
	    System.out.println("e.g., 100 / 1000 / 10000 threads");
	    return;
	}

	for (i=0;i<Integer.valueOf(args[0]);i++)
	    new Thread(new MyThread1()).start();

    }

}