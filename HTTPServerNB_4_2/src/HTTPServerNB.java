//HttpServer.java
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

class HTTPServerNB {

	ServerSocketChannel serverSocketChannel;
	private ServerSocket serverSocket;
	private Selector selector;
	private HashMap<String, ClientSelectThread> clietThreads = null;

	private Log log;
	private int clientId = 0;

	public HTTPServerNB() {
		this.log = new Log();
		clietThreads = new HashMap<String, ClientSelectThread>();
	}

	public void startWebServer(int port) {
		try {
			// Socket creation
			serverSocketChannel = ServerSocketChannel.open();
			serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);

			selector = Selector.open();
			SelectionKey serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			serverKey.attach(null);

			System.err.println("Web Server started on port " + port);
			while (true) {
				selector.select();

				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = (SelectionKey) keyIterator.next();
					keyIterator.remove();

					if (key.isValid()) {
						if (key.isAcceptable()) {
							// accept the client connect and set to
							// non-blocking:
							SocketChannel clientChannel = ((ServerSocketChannel) key.channel())
									.accept();
							clientChannel.configureBlocking(false);

							String mapKey = clientChannel.socket().getInetAddress().toString();
							if (!clietThreads.containsKey(mapKey) || !clietThreads.get(mapKey).isAlive()){
								System.err.println("New thread created with key: "+mapKey);
								clietThreads.put(mapKey, new ClientSelectThread(log, clientId++));
							}
							clietThreads.get(mapKey).addChannel(clientChannel);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}