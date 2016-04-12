import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class ServerThread extends Thread {
	private Socket socket;
	private String user_address;
	private PrintStream ps;
	private BufferedReader bis;
	private InputStream fileStream;
	
	private String page_requested = "/";
	
	private int id;
	private HTTPServer manager;
	private Boolean newClientReady;
	private Boolean shouldDie;
	private Log l;
	
	
	private ArrayList<String> req_headers = null;
	private ArrayList<String> resp_headers = null;
	
	
	public ServerThread(Socket sock, int id, HTTPServer manager, Log l){
		this.socket = sock;
		this.id = id;
		this.manager = manager;
		this.newClientReady = new Boolean(true);
		this.shouldDie = new Boolean(false);
		this.resp_headers = new ArrayList<String>();
		this.l = l;
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
					this.processRequest();
					this.displayPage();
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
			if (page_requested.equals("/"))
				page_requested = "/index.html";
			System.err.println("Page requested:" + page_requested);
			page_requested = page_requested.replaceFirst("/", "");
		}
	}
	
	public void processRequest(){
		resp_headers.add("HTTP/1.1 500 Internal Server Error");
		//resp_headers.add("Connection: close");
		//resp_headers.add("Content-Location: /"+page_requested);
		resp_headers.add("Content-Type: text/html");
		//resp_headers.add("Server: ArdussoLucarella");
		
		try {
			File f = new File(page_requested);
			if (f.isFile()) {
				resp_headers.set(0, "HTTP/1.1 200 OK");
			} else {
				resp_headers.set(0, "HTTP/1.1 404 Not Found");
				f = new File("notFound.html");
			}
			Long size = new Long(f.length());
			resp_headers.add("Content-Length: "+size);
			fileStream = new FileInputStream(f);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			resp_headers.set(0, "HTTP/1.1 500 Internal Server Error");
			resp_headers.add("Content-Length: 0");
		}
	}
	
	private void sendHeader() {
		for(String h : resp_headers){
			l.log("Thread "+this.id+": writing a header");
			ps.print(h);
			ps.print("\r\n");
		}
		ps.print("\r\n");
		this.resp_headers.clear();
	}

	private void displayPage() {
		byte[] buf = new byte[200];
		int bRead = 0;
		// Return selected page
		sendHeader();
		try{
			while ((bRead = fileStream.read(buf)) != -1) {
				ps.write(buf, 0, bRead);
				l.log("Thread "+this.id+": writing "+bRead+" bytes");
				try {
					sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			fileStream.close();
		}catch(IOException e){
			e.printStackTrace();
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
