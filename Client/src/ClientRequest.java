
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * @author Yuzhe You (No.1159774)
 * Client request page.
 */

public class ClientRequest extends JFrame{
	private JFrame window;
	private JButton terminateButton;
	private JButton clearButton;
	private JButton queryButton;
	private JButton addButton;
	private JButton removeButton;
	private JButton updateButton;
	public JTextField wordBox;
	private Client client;
	public JTextArea answerBox;
	private JScrollPane jsp;
	private final String DEFAULT_BACKGROUND_PATH = "client.png";

	public ClientRequest(Client client) {
		this.client = client;
		initRequestPage();
	}

	public void initRequestPage() {
		window = new JFrame();
		window.setBounds(100, 100, 450, 300);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(null);
		
		JTextPane wordText = new JTextPane();
		wordText.setBackground(new Color(255, 255, 255));
		wordText.setOpaque(false);
		wordText.setText("Word:");
		wordText.setBounds(225, 35, 42, 22);
		window.getContentPane().add(wordText);

		wordBox = new JTextField();
		wordBox.setBounds(225, 50, 200, 26);
		window.getContentPane().add(wordBox);
		wordBox.setColumns(10);

		answerBox= new JTextArea();
		jsp = new JScrollPane(answerBox);
		jsp.setBounds(225, 90, 200, 60);
		//jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		window.getContentPane().add(jsp);
		
		JTextPane answerText = new JTextPane();
		answerText.setText("Answer:");
		answerText.setBackground(new Color(255, 255, 255));
		answerText.setOpaque(false);
		answerText.setBounds(225, 75, 50, 22);
		window.getContentPane().add(answerText);

		clearButton = new JButton("clear");
		clearButton.addActionListener(new ClearActionListener());
		clearButton.setBounds(342, 213, 80, 30);
		window.getContentPane().add(clearButton);

		queryButton = new JButton("query");
		queryButton.addActionListener(new QueryActionListener());
		queryButton.setBounds(245, 153, 80, 30);
		window.getContentPane().add(queryButton);

		addButton = new JButton("add");
		addButton.addActionListener(new AddActionListener());
		addButton.setBounds(245, 183, 80, 30);
		window.getContentPane().add(addButton);

		removeButton = new JButton("remove");
		removeButton.addActionListener(new RemoveActionListener());
		removeButton.setBounds(342, 153, 80, 30);
		window.getContentPane().add(removeButton);

		updateButton = new JButton("update");
		updateButton.addActionListener(new UpdateActionListener());
		updateButton.setBounds(342, 183, 80, 30);
		window.getContentPane().add(updateButton);

		terminateButton = new JButton("quit");
		terminateButton.addActionListener(new DisconnectActionListener());
		terminateButton.setBounds(342, 243, 80, 30);
		terminateButton.setForeground(Color.RED);
		window.getContentPane().add(terminateButton);


		String imagePath = ClientLogin.class.getResource("").toString() + DEFAULT_BACKGROUND_PATH;
		imagePath = imagePath.replace("file:", "");
		ImageIcon icon=new ImageIcon(imagePath);
		JLabel label=new JLabel(icon);
		label.setBounds(0,0,icon.getIconWidth(),icon.getIconHeight());
		window.getLayeredPane().add(label,new Integer(Integer.MIN_VALUE));
		JPanel j=(JPanel)window.getContentPane();
		j.setOpaque(false);
		JPanel panel=new JPanel();
		panel.setOpaque(false);

		JTextPane txtpnDictionaryServer = new JTextPane();
		txtpnDictionaryServer.setFont(new Font("American Typewriter", Font.PLAIN, 20));
		txtpnDictionaryServer.setForeground(new Color(255, 255, 255));
		txtpnDictionaryServer.setBackground(new Color(39, 63, 104));
		txtpnDictionaryServer.setText("                              Dictionary Client");
		txtpnDictionaryServer.setBounds(0, 0, 470, 26);
		window.getContentPane().add(txtpnDictionaryServer);

		window.add(panel);
		window.setResizable(false);
		window.setVisible(true);
	}

	private class DisconnectActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == terminateButton) {
				try {
					//System.out.println("disconnect");
					client.shutDown();
				} catch (Exception exception) {
					exception.printStackTrace();
				}finally {
					window.dispatchEvent(new WindowEvent(window,WindowEvent.WINDOW_CLOSING));
					window.setVisible(false);
				}
			}
		}
	}

	private class QueryActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == queryButton) {
				//System.out.println("query");
				JSONObject queryBody = new JSONObject();
				queryBody.put("category","query");
				queryBody.put("Key", wordBox.getText());
				System.out.println(wordBox.getText());
				try {
					client.sendToServer(queryBody);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	private class AddActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == addButton) {
				//System.out.println("add");
				JSONObject addBody = new JSONObject();
				addBody.put("category","add");
				addBody.put("Key", wordBox.getText());
				addBody.put("Value", answerBox.getText());
				try {
					client.sendToServer(addBody);
				} catch (Exception exception) {
					exception.printStackTrace();
				}		
				wordBox.setText("");
			}
		}
	}

	private class RemoveActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == removeButton) {
				//System.out.println("remove");
				JSONObject removeBody = new JSONObject();
				removeBody.put("category","remove");
				removeBody.put("Key", wordBox.getText());
				try {
					client.sendToServer(removeBody);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				wordBox.setText("");
			}
		}
	}

	private class ClearActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == clearButton) {
				wordBox.setText("");
				answerBox.setText("");
			}
		}
	}

	private class UpdateActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == updateButton) {
				JSONObject updateBody = new JSONObject();
				updateBody.put("category","update");
				updateBody.put("Key", wordBox.getText());
				updateBody.put("Value", answerBox.getText());
				try {
					client.sendToServer(updateBody);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				wordBox.setText("");
			}
		}
	}
}
