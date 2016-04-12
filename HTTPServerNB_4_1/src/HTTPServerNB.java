//HttpServer.java
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

class HTTPServerNB {

	ServerSocketChannel serverSocketChannel;
	private ServerSocket serverSocket;
	private Selector selector;

	private Log log;
	private int clientId = 0;

	public HTTPServerNB() {
		this.log = new Log();
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

							// insert new keys in the selector
							//serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
							//serverKey.attach(null);
							
							SelectionKey clientKey = clientChannel.register(selector,
									SelectionKey.OP_READ);
							clientKey.attach(new ClientManager(clientChannel, log, clientId++));
						} else if (key.isReadable()) {
							if (key.attachment() != null) {
								// read the client request
								ClientManager cm = (ClientManager) key.attachment();
								cm.readRequest();

								// insert a new key in the selector
								SelectionKey clientKey = key.channel().register(selector,
										SelectionKey.OP_WRITE);
								clientKey.attach(cm);
							}
						} else if (key.isWritable()) {
							// read the client request
							if (key.attachment() != null) {
								ClientManager cm = (ClientManager) key.attachment();
								if(cm.writeNext() > 0){
									SelectionKey clientKey = key.channel().register(selector,
											SelectionKey.OP_WRITE);
									clientKey.attach(cm);
								}else{
									cm.close();
								}
							}
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