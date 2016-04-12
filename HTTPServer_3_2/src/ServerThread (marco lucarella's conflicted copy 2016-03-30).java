import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;


public class ServerThread extends Thread {
	private Socket socket;
	private String user_address;
	private PrintStream ps;
	private BufferedReader bis;
	
	private String page_requested = "/";
	
	private int id;
	private HTTPServer manager;
	private Boolean newClientReady;
	private Boolean shouldDie;
	
	private ArrayList<String> req_headers = null;
	
	
	public ServerThread(Socket sock, int id, HTTPServer manager){
		this.socket = sock;
		this.id = id;
		this.manager = manager;
		this.newClientReady = new Boolean(true);
		this.shouldDie = new Boolean(false);
	}	
	
	@Override
	public void run(){
		
		while(true){
			if(this.socket == null){
				try {
					synchronized (shouldDie) {
						if(shouldDie){
							return;
						}
					}
					synchronized(this){
						while(!newClientReady){
							this.wait();
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			synchronized (this) {
				try{
					// Buffer creation
					retrieveSocketData(this.socket);
					// Read client request
					this.readRequest();
					
					// Display request contents
					System.err
					.println("-----------------------------------------------");
		
					System.err.println("Name and address of client:");
					System.err.println("---------------------------");
					System.err.println(user_address + "\n");
					System.err.println("writing back page:" + page_requested);
		
					displayPage(page_requested);
		
					// Socket close
					ps.close();
					bis.close();
					socket.close();
					socket = null;
					this.manager.addToFreeQueue(id);
					newClientReady = false;
				}catch(IOException e){
					System.err.println(e);
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}
	
	private void retrieveSocketData(Socket s){
		// Get client state
		user_address = socket.getInetAddress().toString();
		try{
			// Buffer creation
			bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ps = new PrintStream(socket.getOutputStream());
		}catch(IOException e ){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void sendHeader() {
		ps.println("HTTP/1.1 200 OK");
		ps.println("Connection:\t close");
		ps.println("Content-Type: text/html");
		ps.println("\r\n\r\n");
	}

	private void displayPage(String name) {
		// Return selected page
		sendHeader();
		displayPage1();
	}

	private void displayPage1() {
		ps.println("<HTML>\n<title>Java Socket Web Server Page 1</title>");
		ps.println("<H1>Java Socket Web Server - Welcome to page 1</H1>");
		ps.println("<P>This server is powered by Java Sockets.");
		ps.println("This is not so neat, but not so big either</P>");
		ps.println("Want another cup of java? Click <A HREF=\"page2\">here</A>");
		ps.println("<HR>\n</HTML>");
	}
	
	private void readRequest() throws IOException{
		String str;
		req_headers = new ArrayList<>();
		System.err.println("Headers:");
		
		while (true) {
			str = bis.readLine();
			req_headers.add(str);
			
			if (str == null || str.length() < 2)
				break;
			System.err.println(str);
		}
		if (req_headers.get(0).indexOf("GET ") == 0) {
			System.out.println(req_headers.get(0));
			page_requested = req_headers.get(0).split(" ")[1];
			System.err.println("Page requested:" + page_requested);
		}
	}
	
	public void reassign(Socket s){
		synchronized(this){
			this.socket = s;
			this.newClientReady = true;
			this.notify();
		}
	}
	
	public void terminate(){
		synchronized (shouldDie) {
			shouldDie = true;
		}
	}
}
