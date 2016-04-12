import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ClientSelectThread implements Runnable {

	private Thread thread;
	private Log log;
	private int id;
	private Selector selector;
	private Integer keyCounter = 0;

	public ClientSelectThread(Log log, int id) throws IOException {
		this.log = log;
		this.id = id * 1000;
		selector = Selector.open();

		thread = new Thread(this);
		thread.start();
	}

	public void addChannel(SocketChannel clientChannel) throws ClosedChannelException {
		selector.wakeup();
		synchronized (selector) {
			if (selector.isOpen()) {
				SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
				clientKey.attach(new ClientManager(clientChannel, log, id++));

				selector.notify();

				synchronized (keyCounter) {
					keyCounter++;
				}
			}
		}
	}

	@Override
	public void run() {
		int n = 0;

		try {
			do {
				synchronized (selector) {
					do {
						n = selector.select();
						if (n == 0) {
							// selector return due to a wakeup
							selector.wait();
						}
					} while (n == 0);

				}
				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = (SelectionKey) keyIterator.next();
					keyIterator.remove();

					if (key.isValid()) {
						if (key.isReadable()) {
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
								if (cm.writeNext() > 0) {
									SelectionKey clientKey = key.channel().register(selector,
											SelectionKey.OP_WRITE);
									clientKey.attach(cm);
								} else {
									cm.close();
									synchronized (keyCounter) {
										keyCounter--;
									}
								}
							}
						}
					}
				}
			} while (keyCounter > 0);

			synchronized (selector) {
				selector.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isAlive() {
		return thread.isAlive();
	}
}
