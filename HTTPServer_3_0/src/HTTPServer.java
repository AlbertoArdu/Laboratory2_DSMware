//HttpServer.java
import java.net.*;
import java.util.HashMap;
import java.io.*;

class HTTPServer {
	private PrintStream ps;

	private HashMap<String, Integer> state = new HashMap<>();
	private String user_address;
	private int user_state = 0;

	// private int i, j;

	public void initWebServer(int port) {
		try {
			String str;
			int valid = 0;
			InputStream is;
			BufferedReader bis;
			String page_requested = "/"; // page requested ("/" by default)
			// Socket creation
			ServerSocket serverSocket = new ServerSocket(port);

			System.err.println("Web Server started");
			while (true) {
				// Start listening and then accept
				Socket socket = serverSocket.accept();

				// Get client state
				user_address = socket.getInetAddress().toString();
				if (state.containsKey(user_address)) {
					user_state = state.get(user_address);
				} else {
					user_state = 1;
					state.put(user_address, user_state);
				}

				// Buffer creation
				is = socket.getInputStream();
				bis = new BufferedReader(new InputStreamReader(is));

				ps = new PrintStream(socket.getOutputStream());

				// Read client request
				while (true) {
					str = bis.readLine();
					if (str == null || str.equals(""))
						break;
					System.out.println(str);
					if (str.indexOf("GET ") == 0) {
						page_requested = str.split(" ")[1];
						System.out.println(page_requested);
						valid = 1;
					}
				}

				// Display request contents
				System.err.println();
				System.err.println("REQUEST:");
				System.err.println("--------");

				System.err.println();
				System.err.println("writing back page:" + page_requested);

				displayPage(page_requested);

				// Socket close
				ps.close();
				bis.close();
				socket.close();
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public void sendHeader() {
		ps.println("HTTP/1.1 200 OK");
		ps.println("Connection:\t close");
		ps.println("Content-Type: text/html");
		ps.println("\r\n\r\n");
	}

	public void displayPage(String name) {
		// Return selected page
		sendHeader();
		displayPage1();
	}

	public void displayPage1() {
		ps.println("<HTML>\n<title>Java Socket Web Server Page 1</title>");
		ps.println("<H1>Java Socket Web Server - Welcome to page 1</H1>");
		ps.println("<P>This server is powered by Java Sockets.");
		ps.println("This is not so neat, but not so big either</P>");
		ps.println("Want another cup of java? Click <A HREF=\"page2\">here</A>");
		ps.println("<HR>\n</HTML>");
	}
}