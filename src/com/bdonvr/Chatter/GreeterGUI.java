package com.bdonvr.Chatter;

import java.awt.BorderLayout;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

public class GreeterGUI extends JFrame implements ActionListener {
private static final long serialVersionUID = 1L;
// server list goes here
private JTextArea sl;
GreeterGUI() {
super("Chat Client");
JPanel mainPanel = new JPanel(new GridLayout(1,1));
sl = new JTextArea("Welcome to the Chat room\n",80, 80);
mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
mainPanel.add(new JScrollPane(sl));
add(mainPanel, BorderLayout.CENTER);
setDefaultCloseOperation(EXIT_ON_CLOSE);
setSize(600, 600);
setVisible(true);
findServer();

}
void append(String str) {
	sl.append(str);
	sl.setCaretPosition(sl.getText().length() - 1);
}
	public static void main(String[] args) {
		new GreeterGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
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

				  //Wait for a response
				  byte[] recvBuf = new byte[15000];
				  DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				  c.receive(receivePacket);

				  //We have a response
				  System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

				  //Check if the message is correct
				  String message = new String(receivePacket.getData()).trim();
				  if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
				    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
				    append((receivePacket.getAddress().toString()));
				  }

				  //Close the port!
				  c.close();
				} catch (IOException ex) {
				}
			}
	}


