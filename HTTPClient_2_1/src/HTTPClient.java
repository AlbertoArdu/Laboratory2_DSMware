import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPClient {
	Socket sock;
	// DataInputStream in_stream = null;
	BufferedReader in_stream;
	PrintWriter out_stream = null;

	ArrayList<String> headers = null;
	ArrayList<String> responseHeaders = null;

	int respCode = 0;

	public HTTPClient(String addr, int port) {
		InetAddress serverAddr = null;
		try {
			serverAddr = InetAddress.getByName(addr);
		} catch (IOException e) {
			System.err.println("Server not found");
			System.exit(-1);
		}
		try {
			sock = new Socket(serverAddr, port);
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

		headers = new ArrayList<>(10);
		headers.add("GET / HTTP/1.1");
		headers.add("Host: " + addr);
		headers.add("User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0");
		headers.add("Accept: text/html,application/xhtml+xml,application/xml;");
		headers.add("Accept-Language: en-US,en;q=0.5");
		headers.add("Connection: close");
	}

	public void doGET(String filename, PrintStream output) {
		if (filename != null) {
			if (filename.charAt(0) != '/') {
				filename = '/' + filename;
			}
			String req = new StringBuilder("GET ").append(filename).append(" HTTP/1.1").toString();
			this.headers.set(0, req);
		}
		this.sendRequest();
		try {
			this.readResponseHeader();
		} catch (IOException e) {
			System.err.println("Problem reading the response headers");
			System.exit(-1);
		}

		if (respCode == 200) {
			try {
				getResponse(output);
			} catch (IOException e) {
				System.err.println("Problem reading the response content");
				System.exit(-1);
			}
		}
	}

	private void sendRequest() {
		for (String header : this.headers) {
			this.out_stream.print(header + "\r\n");
			System.out.println(header); // Debugging
		}
		this.out_stream.print("\r\n");
		this.out_stream.flush();

		System.out.println("----------------REQUEST-END-----------------------"); // Debugging
	}

	private void readResponseHeader() throws IOException {
		responseHeaders = new ArrayList<>();
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(this.in_stream));
		BufferedReader br = in_stream;
		String currentH;
		currentH = br.readLine();
		if (currentH != null) {
			System.out.println(currentH);
			this.responseHeaders.add(currentH);
			// Response code
			respCode = Integer.valueOf(currentH.split(" ")[1]);
			// Other headers
			do {
				currentH = br.readLine();
				System.out.println(currentH);
				this.responseHeaders.add(currentH);
				// Analize header

			} while (currentH != null && currentH.length() > 1);
		}

		System.out.println("---------------RESPONSE-END-----------------------"); // Debugging

	}

	private void getResponse(PrintStream output) throws IOException {
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

	public void close() {
		try {
			if (in_stream != null)
				in_stream.close();
			if (out_stream != null)
				out_stream.close();
			if (sock != null)
				sock.close();
		} catch (IOException e) {
			System.err.println("Connection close error");
			System.exit(-1);
		}
	}
}
