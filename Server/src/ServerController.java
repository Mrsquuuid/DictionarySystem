import TreadPoolUtil.BasicThreadPool;
import TreadPoolUtil.ThreadPool;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * @author Yuzhe You (No.1159774)
 * Server control page.
 */

public class ServerController extends JFrame {
	private JFrame window;
	private JTextField portInput;
	private JTextArea dataSourceInput;
	private String filePath;
	private JButton connectButton;
	private JButton terminateButton;
	private JScrollPane jsp;
	private int port;
	private Server server;
	private final int MIN_PORT_NUM = 1024;
	private final int MAX_PORT_NUM = 65535;
	private final ThreadPool threadPool = new BasicThreadPool(2,2,3,5);

	public final String DEFAULT_SOURCE_PATH = "dictionary.json";
	//public final String DEFAULT_SOURCE_PATH = "src/dictionary.json";
	public final String DEFAULT_BACKGROUND_PATH = "server.png";
	///Users/mac/IdeaProjects/DictinaryYuzyou/Server/src/server.png

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ServerController() {
		initServerPage();
	}

	private void initServerPage() {
		window = new JFrame();

		window.setBounds(100, 100, 450, 300);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(null);

		JTextPane portText = new JTextPane();
		portText.setText("port:");
		portText.setForeground(new Color(255, 255, 255));
		//portText.setBackground(SystemColor.window);
		portText.setOpaque(false);
		portText.setBounds(27, 65, 37, 21);
		window.getContentPane().add(portText);

		portInput = new JTextField();
		portInput.setColumns(10);
		//portInput.setBackground(new Color(204, 204, 204));
		portInput.setBounds(59, 60, 104, 26);
		window.getContentPane().add(portInput);
		
		//when the connect button is pressed, the server will be ready.
		connectButton = new JButton("start");
		connectButton.addActionListener(new ConnectActionListener());
		connectButton.setBounds(315, 65, 110, 29);
		window.getContentPane().add(connectButton);
		
		//when the disconnect button is pressed, the server will be unavailable.
		terminateButton = new JButton("terminate");
		terminateButton.addActionListener(new DisconnectActionListener());
		terminateButton.setBounds(315, 110, 110, 29);
		window.getContentPane().add(terminateButton);
		
		JTextPane sourceText = new JTextPane();
		sourceText.setText("source:");
		sourceText.setForeground(new Color(255, 255, 255));
		sourceText.setOpaque(false);
		sourceText.setBounds(10, 100, 56, 21);
		window.getContentPane().add(sourceText);

		dataSourceInput = new JTextArea();
		String jsonPath = ServerController.class.getResource("").toString() + DEFAULT_SOURCE_PATH;
		jsonPath = jsonPath.replace("file:", "");
		dataSourceInput.setText(jsonPath);
		jsp = new JScrollPane(dataSourceInput);
		jsp.setBounds(61, 100, 100, 40);
		window.getContentPane().add(jsp);


		JTextPane serverHead = new JTextPane();
		serverHead.setFont(new Font("American Typewriter", Font.PLAIN, 19));
		serverHead.setForeground(new Color(255, 255, 255));
		serverHead.setBackground(new Color(0, 0, 0));
		serverHead.setText("                              Dictionary Server");
		serverHead.setBounds(0, 0, 470, 26);
		window.getContentPane().add(serverHead);

		String imagePath = ServerController.class.getResource("").toString() + DEFAULT_BACKGROUND_PATH;
		imagePath = imagePath.replace("file:", "");
//		path = path.replace("/", "//");
		ImageIcon icon=new ImageIcon(imagePath);
		JLabel label=new JLabel(icon);
		label.setBounds(0,0,icon.getIconWidth(),icon.getIconHeight());
		window.getLayeredPane().add(label,new Integer(Integer.MIN_VALUE));
		JPanel j=(JPanel)window.getContentPane();
		j.setOpaque(false);
		JPanel panel=new JPanel();
		panel.setOpaque(false);
		window.add(panel);

		window.setResizable(false);
	}


	public static String validateIPv4(String IP) {
		String[] nums = IP.split("\\.", -1);
		for (String x : nums) {
			// Validate integer in range (0, 255):
			// 1. length of chunk is between 1 and 3
			if (x.length() == 0 || x.length() > 3) return "Neither";
			// 2. no extra leading zeros
			if (x.charAt(0) == '0' && x.length() != 1) return "Neither";
			// 3. only digits are allowed
			for (char ch : x.toCharArray()) {
				if (!Character.isDigit(ch)) return "Neither";
			}
			// 4. less than 255
			if (Integer.parseInt(x) > 255) return "Neither";
		}
		return "IPv4";
	}
	

	private class ConnectActionListener implements ActionListener{
    	public void actionPerformed(ActionEvent e) {
        	if(e.getSource() == connectButton) {
        		filePath = dataSourceInput.getText();
				boolean validatePort = false;
				if (portInput.getText().matches("^[0-9]+$")&&!portInput.getText().equals("")) {
					port = Integer.parseInt(portInput.getText());
					if (port <= MAX_PORT_NUM && port >= MIN_PORT_NUM) {
						validatePort = true;
					} else {
						JOptionPane.showMessageDialog(window, "Error: Wrong port!");
					}
				} else {
					JOptionPane.showMessageDialog(window, "Error: Wrong port!");
				}
				if (validatePort == true) {
					try {
						server = Server.getInstance(port, filePath);
						threadPool.execute(server);
						//new Thread(server).start();
						//addressInput.setEnabled(false);
						portInput.setEnabled(false);
						dataSourceInput.setEnabled(false);
						connectButton.setForeground(Color.GREEN);
						connectButton.setEnabled(false);
						terminateButton.setForeground(Color.RED);
					} catch (IOException ioException) {
						JOptionPane.showMessageDialog(window, "Dictionary source file does not exist!");
						ioException.printStackTrace();
					} catch (ParseException parseException) {
						JOptionPane.showMessageDialog(window, "Dictionary source file is corrupted!");
						parseException.printStackTrace();
					}
	    		}
        	}
    	}
	}

	private class DisconnectActionListener implements ActionListener{
    	public void actionPerformed(ActionEvent e) {
        	if(e.getSource() == terminateButton) {
        		try {
        			if (server!=null){
						String jsonPath = ServerController.class.getResource("").toString() + DEFAULT_SOURCE_PATH;
						jsonPath = jsonPath.replace("file:", "");
						server.getDictionary().writeJsonFile(server.getDictionary().getDictionaryJsonObj(), jsonPath);
        				threadPool.shutdown();
        				server.shutDown();
        			}
        			JOptionPane.showMessageDialog(window, "Server terminated!");
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(window, "Error 500!");
				}
        		finally {
					window.dispatchEvent(new WindowEvent(window,WindowEvent.WINDOW_CLOSING));
					window.setVisible(false);
				}
        	}
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerController serverController = new ServerController();
					if(args.length==2) {
						serverController.setPort(Integer.parseInt(args[0]));
						serverController.setFilePath(args[1]);
					}
					serverController.window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
