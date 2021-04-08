import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Yuzhe You (No.1159774)
 * Client login page.
 */

public class ClientLogin extends JFrame {
	public static JFrame window;
	private JTextField addressInput;
	private JTextField portInput;
	private JButton connectButton;
	private String address;
	private int port;
	private final int MIN_PORT_NUM = 1024;
	private final int MAX_PORT_NUM = 65535;
	private final String DEFAULT_BACKGROUND_PATH = "client.png";

	public void setAddressInput(String addressInput) {
		this.addressInput.setText(addressInput);
	}

	public void setPortInput(String portInput) {
		this.portInput.setText(String.valueOf(portInput));
	}

	/**
	 * Initialize Client login page.
	 */
	public ClientLogin() {
		window = new JFrame();
		window.setBounds(100, 100, 450, 300);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(null);

		JTextPane addressText = new JTextPane();
		addressText.setBackground(new Color(255, 255, 255));
		addressText.setText("Address:");
		addressText.setBounds(232, 83, 56, 21);
		window.getContentPane().add(addressText);

		//input address
		addressInput = new JTextField();
		addressInput.setBounds(289, 77, 130, 29);
		window.getContentPane().add(addressInput);
		addressInput.setColumns(10);

		JTextPane portText = new JTextPane();
		portText.setText("Port:");
		portText.setBackground(new Color(255, 255, 255));
		portText.setBounds(255, 113, 37, 21);
		window.getContentPane().add(portText);

		portInput = new JTextField();
		portInput.setColumns(10);
		portInput.setBounds(289, 107, 130, 29);
		window.getContentPane().add(portInput);

		connectButton = new JButton("connect");
		connectButton.addActionListener(new ConnectActionListener());
		connectButton.setBounds(270, 152, 130, 29);
		//connectButton.setForeground(Color.GREEN);
		//connectButton.setBackground(Color.GREEN);
		window.getContentPane().add(connectButton);

		JTextPane clientHead = new JTextPane();
		clientHead.setFont(new Font("American Typewriter", Font.PLAIN, 20));
		clientHead.setForeground(new Color(255, 255, 255));
		clientHead.setBackground(new Color(39, 63, 104));
		clientHead.setText("                              Dictionary Client");
		clientHead.setBounds(0, 0, 470, 26);
		window.getContentPane().add(clientHead);

		// Load picture
		String imagePath = ClientLogin.class.getResource("").toString() + DEFAULT_BACKGROUND_PATH;
		imagePath = imagePath.replace("file:", "");
		ImageIcon icon=new ImageIcon(imagePath);
		// Put the picture in the label
		JLabel label=new JLabel(icon);
		// Set the size of the label
		label.setBounds(0,0,icon.getIconWidth(),icon.getIconHeight());
		// Get the second layer of the window and put the label in the window
		window.getLayeredPane().add(label,new Integer(Integer.MIN_VALUE));
		// Get the top-level container of frame and make it transparent
		JPanel j=(JPanel)window.getContentPane();
		j.setOpaque(false);
		JPanel panel=new JPanel();
		// Must be set to transparent. Otherwise, you can't see the picture
		panel.setOpaque(false);

		window.add(panel);
		window.setResizable(false);
		window.setVisible(true);
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


	private class ConnectActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == connectButton) {
				address = addressInput.getText();
				if (!portInput.getText().equals("")) {
					port = Integer.parseInt(portInput.getText());
				}
				boolean validation = true;
				if (!validateIPv4(address).equals("IPv4") && !address.equals("localhost")) {
					validation = false;
					JOptionPane.showMessageDialog(window, "Error: Wrong IP address!");
				}
				if (!portInput.getText().matches("^[0-9]+$") || port < MIN_PORT_NUM || port > MAX_PORT_NUM) {
					validation = false;
					JOptionPane.showMessageDialog(window, "Error: Wrong port!");
				}
				if (validation == true) {
					try {
						Client client = new Client(address, port);
						new Thread(client).start();
					} catch (Exception invalidInputs) {
						JOptionPane.showMessageDialog(window, "Error: there is something wrong with IP address or port!");
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientLogin clientLogin = new ClientLogin();
					if(args.length==2) {
						clientLogin.setPortInput(args[0]);
						clientLogin.setAddressInput(args[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
