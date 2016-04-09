package com.bdonvr.Chatter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GreeterGUI extends JFrame implements ActionListener {
private static final long serialVersionUID = 1L;
// server list goes here
private JList sl;
private JButton join, newServer;
public static JTextField nameServer;
private Server server;
public DefaultListModel servers = new DefaultListModel();

GreeterGUI() {
super("Chat Client");
JPanel mainPanel = new JPanel(new GridLayout(1,1));
sl = new JList(servers);
sl.addListSelectionListener(sel);
mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
mainPanel.add(new JScrollPane(sl));
JPanel buttonPanel = new JPanel(new GridLayout(1,2));
join = new JButton("Join Server");
join.addActionListener(this);
join.setEnabled(false);
newServer = new JButton("Create Server");
newServer.addActionListener(this);
nameServer = new JTextField("Unnamed Server");
nameServer.setBackground(Color.WHITE);
buttonPanel.add(join);
buttonPanel.add(newServer);
JPanel bottomPanel = new JPanel(new GridLayout(1,1));
bottomPanel.add(nameServer);
add(mainPanel, BorderLayout.CENTER);
mainPanel.add(buttonPanel);
mainPanel.add(bottomPanel);
setDefaultCloseOperation(EXIT_ON_CLOSE);
setSize(350, 200);
setVisible(true);
findServer();

}

ListSelectionListener sel = new ListSelectionListener() {

	@Override
	public void valueChanged(ListSelectionEvent e) {
		join.setEnabled(true);
	}};

void append(String str) {
	servers.addElement(str);
}
	public static void main(String[] args) {
		new GreeterGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o == join){
			int space = sl.getSelectedValue().toString().indexOf(" ");
			new ClientGUI((sl.getSelectedValue()).toString().substring(0, space), 5242);
			setVisible(false);
		}
		if(o == newServer){
			setVisible(false);
			new ClientGUI("localhost", 5242);
			System.out.println(nameServer.getText());
			server = new Server(5242, nameServer.getText());
			new ServerRunning().start();
		}
		
	}
	
	void findServer() {
		// Find the server using UDP broadcast
				try {
				  //Open a random port to send the package
				  DatagramSocket c = new DatagramSocket();
				  c.setBroadcast(true);

				  byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

				  //Try the 255.255.255.255 first
				  try {
				    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
				    c.send(sendPacket);
				    System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
				  } catch (Exception e) {
				  }

				  // Broadcast the message over all the network interfaces
				  Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
				  while (interfaces.hasMoreElements()) {
				    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

				    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				      continue; // Don't want to broadcast to the loopback interface
				    }

				    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
				      InetAddress broadcast = interfaceAddress.getBroadcast();
				      if (broadcast == null) {
				        continue;
				      }

				      // Send the broadcast package!
				      try {
				        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
				        c.send(sendPacket);
				      } catch (Exception e) {
				      }

				      System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
				    }
				  }

				  System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
				  final long start = System.nanoTime();
				  do {
				      for (int i=0; i<200; ++i) {
				  //Wait for a response
				  byte[] recvBuf = new byte[15000];
				  DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				  c.receive(receivePacket);

				  //We have a response
				  System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

				  //Check if the message is correct
				  String message = new String(receivePacket.getData()).trim();
				  if (message.startsWith("DISCOVER_FUIFSERVER_RESPONSE")) {
				    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
					if (!(servers.contains((receivePacket.getAddress().toString()).substring(1).concat(message.substring(28)))))
				    append((receivePacket.getAddress().toString()).substring(1).concat(message.substring(28)));
				  }}
				  } while (System.nanoTime()-start < 1L*1000L*1000L*1000L);

				  //Close the port!
				  c.close();
				} catch (IOException ex) {
				}
			}
		class ServerRunning extends Thread {
			public void run() {
				server.start(nameServer.getText());
			}
		}
	}


