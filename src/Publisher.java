import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

/**
 * This class implements all the functionality of the publisher.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public class Publisher extends UnicastRemoteObject implements Serializable {

	private static final long serialVersionUID = 1L;
	String publisherIP;
	String publisherName;
	final int LoadBalancerPort = 5122;
	String eventServiceIP;
	static String loadBalancerIP = "129.21.30.38";
	static String LBBackUpIP = "129.21.30.50";
	final int LBBackUpPort = 5233;

	final int ESPort = 1120;
	Publisher p;

	static Scanner sc = new Scanner(System.in);

	public Publisher(String publisherIP, String publisherName)
			throws RemoteException {

		this.publisherIP = publisherIP;
		this.publisherName = publisherName;

	}

	public void joinSystem() throws NotBoundException, IOException {

		try {
			LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
					.lookup("//" + loadBalancerIP + ":" + LoadBalancerPort
							+ "/LoadBalancer");

			eventServiceIP = loadremoteObj.getEventServiceIP();

			System.out.println();
			System.out.println("Enter name of file to publish");

			String fileName = sc.next();
			File aFile = new File(fileName);

			FileInputStream fip = new FileInputStream(aFile);
			byte[] fileContent = new byte[(int) aFile.length()];

			fip.read(fileContent);
			fip.close();

			// connect with Event Service
			EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
					.lookup("//" + eventServiceIP + ":" + ESPort
							+ "/EventService");

			String hostIP = InetAddress.getLocalHost().getHostAddress();
			String hostName = InetAddress.getLocalHost().getHostName();

			eventremoteObj.finalPubJoin(hostIP, fileContent, fileName);

		} catch (Exception e) {
			LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
					.lookup("//" + LBBackUpIP + ":" + LBBackUpPort
							+ "/LBBackUp");

			eventServiceIP = loadremoteObj.getEventServiceIP();

			System.out.println();
			System.out.println("Enter name of file to publish");

			String fileName = sc.next();
			File aFile = new File(fileName);

			FileInputStream fip = new FileInputStream(aFile);
			byte[] fileContent = new byte[(int) aFile.length()];

			fip.read(fileContent);
			fip.close();

			// connect with Event Service
			EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
					.lookup("//" + eventServiceIP + ":" + ESPort
							+ "/EventService");

			String hostIP = InetAddress.getLocalHost().getHostAddress();
			String hostName = InetAddress.getLocalHost().getHostName();

			eventremoteObj.finalPubJoin(hostIP, fileContent, fileName);
		}

	}

	public void start() throws NotBoundException, IOException {

		int startInput = 0;
		while (true) {
			System.out.println();
			System.out.println("--------------MENU----------------");
			System.out.println("1. Publish new item");
			System.out.println("2. Exit the system");

			startInput = sc.nextInt();
			switch (startInput) {
			case 1: {
				System.out.println("The files available are:");
				System.out
						.println("[free.txt, parking1.txt, parking2.txt, macbeth.txt]");
				joinSystem();
				break;
			}

			case 2: {
				System.exit(0);
				break;
			}
			}
		}

	}

	public static void main(String arg[]) throws NotBoundException, IOException {
		System.out.println();
		System.out.println("Connecting to Load Balancer");
		Publisher pub = new Publisher(InetAddress.getLocalHost()
				.getHostAddress(), InetAddress.getLocalHost().getHostName());
		pub.start();
	}
}