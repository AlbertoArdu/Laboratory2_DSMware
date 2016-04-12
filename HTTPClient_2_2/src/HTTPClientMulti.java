import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class HTTPClientMulti {
	private InetAddress serverAddr = null;
	private int port = 0;
	private ArrayList<Thread> threadRequests = null;

	private ArrayList<String> headers = null;

	public HTTPClientMulti(String addr, int port) {
		try {
			serverAddr = InetAddress.getByName(addr);
		} catch (IOException e) {
			System.err.println("Server not found");
			System.exit(-1);
		}
		this.port = port;

		headers = new ArrayList<>(10);
		headers.add("GET / HTTP/1.1");
		headers.add("Host: " + addr);
		headers.add("User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0");
		headers.add("Accept: text/html,application/xhtml+xml,application/xml;");
		headers.add("Accept-Language: en-US,en;q=0.5");
		headers.add("Connection: close");
		
		threadRequests = new ArrayList<>();
	}

	public void doGET(String remoteFilename, String locaFilename) {
		int respCode = -1;
		Socket sock = null;
		// DataInputStream in_stream = null;
		BufferedReader in_stream=null;
		PrintWriter out_stream = null;
		String headerGET = headers.get(0);

		if (remoteFilename != null) {
			if (remoteFilename.charAt(0) != '/') {
				remoteFilename = '/' + remoteFilename;
			}
			headerGET = new StringBuilder("GET ").append(remoteFilename).append(" HTTP/1.1")
					.toString();
		}

		try {
			sock = new Socket(this.serverAddr, this.port);
		} catch (IOException e) {
			System.err.println("Problem connecting the socket to server");
			System.exit(-1);
		}

		try {
			// in_stream = new DataInputStream(sock.getInputStream());
			in_stream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out_stream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					sock.getOutputStream())));
		} catch (IOException e) {
			System.err.println("Problem opening streams");
			System.exit(-1);
		}

		this.sendRequest(out_stream, headerGET);

		try {
			respCode = this.readResponseHeader(in_stream);
		} catch (IOException e) {
			System.err.println("Problem reading the response headers");
			System.exit(-1);
		}

		if (respCode == 200) {
			try {
				if(locaFilename == null){
				this.getResponse(in_stream, System.out);
				}else{
					PrintStream fileWriter = new PrintStream(new FileOutputStream(locaFilename, false));
					this.getResponse(in_stream, fileWriter);
					fileWriter.close();
				}
			} catch (IOException e) {
				System.err.println("Problem reading the response content");
				System.exit(-1);
			}
		}
	}

	private void sendRequest(PrintWriter out_stream, String headerGET) {
		if (out_stream != null) {
			for (int i = 0; i < headers.size(); i++) {
				if (i == 0 && headerGET != null) {
					out_stream.print(headerGET + "\r\n");
					System.out.println(headerGET); // Debugging
				} else {
					out_stream.print(headers.get(i) + "\r\n");
					System.out.println(headers.get(i)); // Debugging
				}
			}
			out_stream.print("\r\n");
			out_stream.flush();
		}
		System.out.println("----------------REQUEST-END-----------------------"); // Debugging
	}

	private int readResponseHeader(BufferedReader in_stream) throws IOException {
		int respCode = -1;
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(this.in_stream));
		BufferedReader br = in_stream;
		String currentH;
		currentH = br.readLine();
		if (currentH != null) {
			System.out.println(currentH);
			// Response code
			respCode = Integer.valueOf(currentH.split(" ")[1]);
			// Other headers
			do {
				currentH = br.readLine();
				System.out.println(currentH);
				// Analize header

			} while (currentH != null && currentH.length() > 1);
		}

		System.out.println("---------------RESPONSE-END-----------------------"); // Debugging
		return respCode;

	}

	private void getResponse(BufferedReader in_stream, PrintStream output) throws IOException {
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(this.in_stream));
		BufferedReader br = in_stream;
		String line;
		line = br.readLine();
		while (line != null) {
			output.print(line + System.lineSeparator());
			line = br.readLine();
		}
		System.out.println("---------------CONTENT-END------------------------"); // Debugging
	}

	public void doGETasync(String remoteFilename, String locaFilename){
		Thread t = new Thread(new TaskRequest(this, remoteFilename, locaFilename));
		threadRequests.add(t);
		t.start();
	}
	
	public void close() throws InterruptedException {
		for(Thread t : threadRequests){
			t.join();
		}
	}

	private class TaskRequest implements Runnable{
		String remoteFilename, locaFilename;
		HTTPClientMulti client = null;
		public TaskRequest(HTTPClientMulti client, String remoteFilename,String locaFilename){
			this.remoteFilename = remoteFilename;
			this.locaFilename = locaFilename;
			this.client = client;
		}
		
		@Override
		public void run() {
			client.doGET(remoteFilename, locaFilename);
		}
	}

}


