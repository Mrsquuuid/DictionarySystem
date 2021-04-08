import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuzhe You (No.1159774)
 * Server that can send and receive message from clients.
 */

public class Server implements Runnable {

	private DataSource dictionary;
	private static volatile Server server;
	private AtomicInteger onlineUsersCount;
	private static final int DEFAULT_PORT = 8888;
	private static final String QUIT = "quit";
	private static final int BUFFER = 1024;//缓冲区大小
	private ServerSocketChannel serverSocketChannel;// Server side channel, using channel for communication
	private Selector selector;
	private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);// Buffer used to read messages
	private ByteBuffer wBeBuffer = ByteBuffer.allocate(BUFFER);// Buffer used to write information
	private Charset charset = Charset.forName("UTF-8");
	private int port;
	private Map<SocketChannel, String> onlineUsers;

	// Singleton mode, double check lock, ensure that the server is unique.
	public static Server getInstance(int port, String filePath) throws IOException, ParseException {
		if (server == null) {
			synchronized (Server.class) {
				if (server == null) {
					server = new Server(port, filePath);
				}
			}
		}
		return server;
	}

	public Server(int port, String filePath) throws FileNotFoundException, IOException, ParseException {
		//this.address = address;
		this.dictionary = new DataSource(filePath);
		this.port = port;
		//this.clients = new ArrayList<ServerThread>();
	}

	public DataSource getDictionary() {
		return dictionary;
	}

	/**
	 * run the server
	 */
	public void run() {
		try {
			System.out.println("Server started!");
			//server continues to wait and accept a connection
			try {
				// Create a serversocketchannel channel
				serverSocketChannel = ServerSocketChannel.open();
				// Set the channel to non blocking (blocking by default)
				serverSocketChannel.configureBlocking(false);
				// Bind to listening port
				System.out.println(InetAddress.getLocalHost());
				serverSocketChannel.socket().bind(new InetSocketAddress(port));
				// Create selector
				selector = Selector.open();
				// Register the accept client connection request events that need to be monitored on the server channel
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				System.out.println("Server started and listens to the port " + port + ".....");
				onlineUsers = new ConcurrentHashMap<>();
				onlineUsersCount = new AtomicInteger(0);
				// Enter monitor mode
				while (true) {
					// The select() function is blocking
					selector.select();
					// Get the listening event. Every triggered event and its related information are packaged
					// in the selectionkey object
					Set<SelectionKey> selectionKeys = selector.selectedKeys();
					for (SelectionKey selectionKey : selectionKeys) {
						// Handling triggered events
						handles(selectionKey);
					}
					// Manually clear the processed events
					selectionKeys.clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close(selector);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the information on the channel
	 * @param client
	 * @return data from clients
	 * @throws IOException
	 */
	private String receive(SocketChannel client) throws IOException {
		// Clean up the remaining content
		rBuffer.clear();
		while (client.read(rBuffer) > 0) ;
		// Read back mode switch
		rBuffer.flip();
		return String.valueOf(charset.decode(rBuffer));
	}

	/**
	 *  Close resource
	 */
	public void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handling triggered events
	 * It mainly deals with two kinds of events
	 * 1. Client connection request time accept event
	 * 2. Read event after the connected client sends a message
	 * @param selectionKey
	 * @throws IOException
	 */
	private void handles(SelectionKey selectionKey) throws IOException, ParseException {
		// Accept event - link with customer
		if (selectionKey.isAcceptable()) {
			// Get the server channel, which is the channel for which this key was created
			ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
			// Obtain the client channel and accept the client connection request
			SocketChannel clientChannel = server.accept();
			// Set non blocking
			clientChannel.configureBlocking(false);
			// Register the read events that need to be monitored on the client channel
			clientChannel.register(selector, SelectionKey.OP_READ);
			System.out.println("Client[" + clientChannel.socket().getPort() + "]" + "connect to this server!");
			InetAddress username = clientChannel.socket().getInetAddress();
			onlineUsers.put(clientChannel, String.valueOf(clientChannel.socket().getPort()));
			onlineUsersCount.getAndIncrement();
			for (SocketChannel user : onlineUsers.keySet()) {
				System.out.println("new client connect："+ username.toString()+ "-" + onlineUsers.get(user)+" ");
			}
			System.out.println("total clients："+ onlineUsersCount.get());
			// Read event --- when the customer sends a message, there is a readable event
		} else if (selectionKey.isReadable()) {
			// Get the client channel and read the message sent by the client
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			String fwMsg = receive(clientChannel);
			JSONParser jsonParser = new JSONParser();
			if (fwMsg.isEmpty()) {
				onlineUsers.remove(clientChannel);
				onlineUsersCount.getAndDecrement();
				// The client exception, no longer listen to the client may send messages
				selectionKey.cancel();
				// Event has changed. Update the event that the selector listens to
				selector.wakeup();
				InetAddress username = clientChannel.socket().getInetAddress();
				for (SocketChannel user : onlineUsers.keySet()) {
					System.out.println("new client connect："+ username.toString()+ "-" + onlineUsers.get(user)+" ");
				}
				System.out.println("total clients："+ onlineUsersCount.get());
			} else {
				JSONObject request = (JSONObject) jsonParser.parse(fwMsg);
				JSONObject result = resolveClientRequest(request);
				System.out.println(result.toString());
				forwardMessage(clientChannel, result.toString());
				// Check whether the user exits
				if (readyToQuit(fwMsg)) {
					selectionKey.cancel();
					selector.wakeup();
					System.out.println("Client[" + clientChannel.socket().getPort() + "]" + " disconnected!");
				}
			}
		}

	}

	/**
	 * Forward message to client
	 * @param client
	 * @param fwMsg
	 */
	private void forwardMessage(SocketChannel client, String fwMsg) throws IOException {
		// selector.keys () will return the collection of all selectionkeys registered on the selector,
		// We can think that the selectionkey registered on the selector is the current online client
		for (SelectionKey key : selector.keys()) {
			// Skip the server-side channel, serversocketchannel
			Channel connectedClient = key.channel();
			if (connectedClient instanceof ServerSocketChannel) {
				continue;
			}
			// The detection channel is not closed and the channel is itself
			if (key.isValid() && client.equals(connectedClient)) {
				wBeBuffer.clear();
				wBeBuffer.put(charset.encode(fwMsg));
				wBeBuffer.flip();
				while (wBeBuffer.hasRemaining()) {
					((SocketChannel) connectedClient).write(wBeBuffer);
				}
			}
		}

	}
	
	/**
	 * terminate the server
	 * @throws Exception
	 */
	public void shutDown() throws Exception {
		serverSocketChannel.close();
		selector.close();
	}

	public JSONObject resolveClientRequest(JSONObject clientRequest) {
		JSONObject result = null;
		String task = (String) clientRequest.get("category");
		switch (task) {
			case "query":
				result = dictionary.queryEncapsulate((String) clientRequest.get("Key"), (String) clientRequest.get("Value"));
				break;
			case "add":
				result = dictionary.addEncapsulate((String) clientRequest.get("Key"), (String) clientRequest.get("Value"));
				break;
			case "remove":
				result = dictionary.removeEncapsulate((String) clientRequest.get("Key"), (String) clientRequest.get("Value"));
				break;
			case "update":
				result = dictionary.updateEncapsulate((String) clientRequest.get("Key"), (String) clientRequest.get("Value"));
				break;
		}

		return result;
	}

	/**
	 * Check whether to exit
	 * @param msg
	 * @return
	 */
	public boolean readyToQuit(String msg) {
		return QUIT.equals(msg);
	}
}