class my_class {
	static class MyThread extends Thread {
		String r1;
		String r2;
		int i;

		public MyThread(int tid, String res1, String res2) {
			i=tid;
			r1=res1;
			r2=res2;
		}
		
		public void run() {
			synchronized (r1) {
				System.out.println("Thread " + i + ": locked " + r1);
				yield();
				synchronized (r2) {
					System.out.println("Thread " + i + ": locked " + r2);
				}
			}
			System.out.println("Thread " + i + ": released " + r1);
			System.out.println("Thread " + i + ": released " + r2);
		}
	}

	public static void main(String[] args) {
		final String resource1 = "string 1";
		final String resource2 = "string 2";
		final String resource3 = "string 3";
		final String resource4 = "string 4";

		new MyThread(1,resource1,resource2).start();
		//I inverted the two resources for the next thread
		new MyThread(2,resource1,resource4).start();
		new MyThread(3,resource2,resource3).start();
		new MyThread(4,resource3,resource4).start();
	}
}