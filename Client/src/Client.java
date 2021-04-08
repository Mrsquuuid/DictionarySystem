
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import javax.swing.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;


/**
 * @author Yuzhe You (No.1159774)
 * Client that can receive and send message.
 */

public class Client implements Runnable {

	private static final String QUIT = "quit";
	private static final int BUFFER = 1024;//Buffer size
	private String address;
	private int port;
	private SocketChannel clientChannel;
	private Selector selector;// 选择器
	private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);//Buffer used to read messages
	private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);//Buffer used to write information
	private Charset charset = Charset.forName("UTF-8");
	//	private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
	//	private static final int DEFAULT_SERVER_PORT = 8888;

	ClientRequest requestPage = null;

	public Client() {
	}

	public Client(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * start the client channel
	 */
	public void run() {
		try {
			// Create client channel
			System.out.println("Connecting to the server...");
			clientChannel = SocketChannel.open();
			clientChannel.configureBlocking(false);
			selector = Selector.open();
			// Register the connection events that the client needs to listen for connect
			clientChannel.register(selector, SelectionKey.OP_CONNECT);
			// Send connection request to server
			clientChannel.connect(new InetSocketAddress(address, port));
			while (true) {
				selector.select();
				// Gets the triggered event
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				for (SelectionKey selectionKey : selectionKeys) {
					handles(selectionKey);
				}
				selectionKeys.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClosedSelectorException e) {
			// The user exits normally
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param selectionKey
	 * @return Handle triggered events
	 * @throws IOException, ParseException
	 */
	private void handles(SelectionKey selectionKey) throws IOException, ParseException {
		// Connect event -- connection ready event
		if (selectionKey.isConnectable()) {
			// Get the corresponding client channel on the selectionkey
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			// Is the request linked
			if (clientChannel.isConnectionPending()) {
				// Officially establish a link
				boolean finishConnect = false;
				try {
					finishConnect = clientChannel.finishConnect();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(ClientLogin.window, "Error: Server has not started yer!");
				}
				if (finishConnect == true) {
					// This thread handles four control requests
					JOptionPane.showMessageDialog(ClientLogin.window, "Client started!");
					ClientLogin.window.setVisible(false);
					System.out.println("Connection established");
					requestPage = new ClientRequest(this);
					//new Thread(new UserInputHandler(this)).start();
				}
			}
			// Register read events and receive messages forwarded by other clients
			clientChannel.register(selector, SelectionKey.OP_READ);
		}
		// Read event -- server forwarding message event
		else if (selectionKey.isReadable()) {
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			String msg = receive(clientChannel);

			JSONParser jsonParser = new JSONParser();
			JSONObject result = (JSONObject) jsonParser.parse(msg);
			//System.out.println(result.toString());
			if (requestPage != null) {
				System.out.println(result.toString());
				if (result.get("method").equals("query")) {
					System.out.println("query " + result.get("status"));
					System.out.println(result.get("feedback"));
					String str = result.get("feedback").toString();
					String finalResult = str.replace("[", "").replace("]", "").replace("\"", "");
					requestPage.answerBox.setText(finalResult);
				} else if (result.get("method").equals("add")) {
					System.out.println("add " + result.get("status"));
					System.out.println(result.get("feedback"));
					String str = result.get("feedback").toString();
					String finalResult = str.replace("[", "").replace("]", "").replace("\"", "");
					requestPage.answerBox.setText(finalResult);
				} else if (result.get("method").equals("remove")) {
					System.out.println("remove " + result.get("status"));
					System.out.println(result.get("feedback"));
					String str = result.get("feedback").toString();
					String finalResult = str.replace("[", "").replace("]", "").replace("\"", "");
					requestPage.answerBox.setText(finalResult);
				} else if (result.get("method").equals("update")) {
					System.out.println("update " + result.get("status"));
					System.out.println(result.get("feedback"));
					String str = result.get("feedback").toString();
					String finalResult = str.replace("[", "").replace("]", "").replace("\"", "");
					requestPage.answerBox.setText(finalResult);
				}
			}
			if (msg.isEmpty()) {
				// Server exception, close selector, client exit
				close(selector);
			} else {
				// System.out.println("Client[" + this.clientChannel.socket().getPort() + "] " + msg);
			}
		}
	}



	/**
	 * @param clientChannel
	 * @return data from server
	 * @throws IOException
	 */
	private String receive(SocketChannel clientChannel) throws IOException {
		rBuffer.clear();
		while (clientChannel.read(rBuffer) > 0) ;
		rBuffer.flip();
		//System.out.println("receive"+rBuffer.toString());
		return String.valueOf(charset.decode(rBuffer));
	}
	
	/**
	 * terminate the client
	 * @throws Exception
	 */
	public void shutDown() throws Exception {
		try {
			close(selector);
			clientChannel.close();
		}catch(Exception e) {
			throw new Exception("Error: socket cannot be closed.");
		}
	}
	
	/**
	 * Client sends data to the server
	 * @param request
	 * @throws Exception
	 */
	public void sendToServer(JSONObject request) throws Exception {
		try {
			send(request.toString()+'\n');
		}catch(Exception e) {
			JOptionPane.showMessageDialog(ClientLogin.window, "Error: server failed, please terminate client!");
			this.shutDown();
			throw new Exception("Error: connection loss!");
		}
	}



	/**
	 * Close resource
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
	 * Send message
	 * @param msg
	 * @throws IOException
	 */
	public void send(String msg) throws IOException{
		if (msg.isEmpty()) {
			return;
		}
		wBuffer.clear();
		wBuffer.put(charset.encode(msg));
		wBuffer.flip();
		while (wBuffer.hasRemaining()) {
			clientChannel.write(wBuffer);
		}
		System.out.println("Request has been sent!");
		// Check if the user is ready to launch
		if (readyToQuit(msg)) {
			close(selector);
		}
	}

	/**
	 * Check whether to exit
	 * @param msg
	 * @return Boolean
	 */
	public boolean readyToQuit(String msg) {
		return QUIT.equals(msg);
	}
}