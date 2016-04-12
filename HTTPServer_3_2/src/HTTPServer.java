//HttpServer.java
import java.net.*;
import java.util.*;

class HTTPServer {

	private ServerSocket serverSocket;
	private ArrayList<ServerThread> threadRequests = null;
	private Queue<Integer> freeThreads = null;
	
	private Log l;
	
	public HTTPServer(){
		this.threadRequests = new ArrayList<>();
		this.freeThreads = new LinkedList<Integer>();	
		this.l = new Log();
	}
	
	public void initWebServer(int port) {
		Socket s;
		ServerThread st;
		try {
			// Socket creation
			serverSocket = new ServerSocket(port);

			System.err.println("Web Server started");
			while (true) {
				// Start listening and then accept
				s = serverSocket.accept();
				
				if(freeThreads.size() == 0){
					st = new ServerThread(s,threadRequests.size(),this,l);
					//Launch a thread for the new connection
					this.threadRequests.add(st);
					st.start();
				}else{
					Integer i = freeThreads.poll();
					if(i == null){
						this.threadRequests.add(new ServerThread(s,threadRequests.size(),this,l));
					}else{
						this.threadRequests.get(i).reassign(s);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void addToFreeQueue(int i){
		if(!this.freeThreads.offer(new Integer(i))){
			//If there is no more space kill the thread
			threadRequests.get(i).terminate();
			threadRequests.remove(i);
		}
	}
}