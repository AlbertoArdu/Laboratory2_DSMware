import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ClientManager {

	private SocketChannel socketChannel;
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	private Log log;
	private String page_requested;
	private InputStream fileStream;
	private int id;
	private boolean header_sent = false;

	private StringBuilder req_headers = null;
	private ArrayList<String> resp_headers = null;

	public ClientManager(SocketChannel socketChannel, Log log, int id) {
		this.socketChannel = socketChannel;
		this.log = log;
		this.resp_headers = new ArrayList<String>();
		this.id = id;
	}

	public void readRequest() throws IOException {
		String str;
		req_headers = new StringBuilder();
		System.err.println("Headers:");

		while (true) {
			readBuffer.clear();
			int n = socketChannel.read(readBuffer);

			str = Charset.defaultCharset().decode(readBuffer).toString();
			req_headers.append(str);
			System.err.println(str);

			if (n <= 0) {
				break;
			}
		}

		if (req_headers.indexOf("GET ") != -1) {
			str = req_headers.substring(req_headers.indexOf("GET "));
			page_requested = str.split(" ")[1];
			if (page_requested.equals("/"))
				page_requested = "/index.html";
			System.err.println("Page requested:" + page_requested);
			page_requested = page_requested.replaceFirst("/", "");
		}
		
		header_sent = false;
	}

	public int writeNext() throws IOException {
		if (!header_sent){
			openFileStream();
			sendHeader();
			header_sent = true;
			return 1;
		}else{
			return displayPageNext();
		}
	}

	private void openFileStream() {
		resp_headers.add("HTTP/1.1 500 Internal Server Error");
		resp_headers.add("Content-Type: text/html");

		try {
			File f = new File(page_requested);
			if (f.isFile()) {
				resp_headers.set(0, "HTTP/1.1 200 OK");
			} else {
				resp_headers.set(0, "HTTP/1.1 404 Not Found");
				f = new File("notFound.html");
			}
			Long size = new Long(f.length());
			resp_headers.add("Content-Length: " + size);
			fileStream = new FileInputStream(f);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			resp_headers.set(0, "HTTP/1.1 500 Internal Server Error");
			resp_headers.add("Content-Length: 0");
		}

	}

	private void sendHeader() throws IOException {
		for (String h : resp_headers) {
			log.log("Client " + this.id + ": writing a header");
			socketChannel.write(Charset.defaultCharset().encode(h + "\r\n"));
		}
		socketChannel.write(Charset.defaultCharset().encode("\r\n"));
		this.resp_headers.clear();
	}

	private int displayPageNext() throws IOException {
		byte[] buf = new byte[200];
		int bRead = 0;
		// Return selected page
		
		try {
			bRead = fileStream.read(buf);
			if (bRead > 0) {
				socketChannel.write(ByteBuffer.wrap(buf, 0, bRead));
				log.log("Thread " + this.id + ": writing " + bRead + " bytes");
				try {
					Thread.sleep(200); // for debugging
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bRead;
	}
	
	public void close() throws IOException{
		fileStream.close();
		socketChannel.close();
	}
}
