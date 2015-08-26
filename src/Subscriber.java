import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * This class implements all the functionality of the subscriber.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public class Subscriber extends UnicastRemoteObject implements
		SubscriberInterface, Serializable {

	private static final long serialVersionUID = 1L;
	String subscriberIP;
	String subscriberName;
	int port = 9110;

	final int LoadBalancerPort = 5122;
	static String loadBalancerIP = "129.21.30.38";
	static String LBBackUpIP = "129.21.30.50";
	final int LBBackUpPort = 5233;
	final int subPort = 2120;

	final int ESPort = 1120;

	String eventServiceIP;
	// Subscriber s;
	static Scanner sc = new Scanner(System.in);
	ArrayList<String> keywords, moreKeywords, deleteKeywords;

	public Subscriber() throws RemoteException, UnknownHostException {
		super();

		Registry registry = null;
		try {

			registry = LocateRegistry.getRegistry(subPort);
			registry.list();
			registry.rebind("Subscriber", this);
			System.out.println("The IP Address of the Subscriber Server is "
					+ InetAddress.getLocalHost().getHostAddress());

		} catch (Exception e) {
			registry = LocateRegistry.createRegistry(subPort);
			registry.rebind("Subscriber", this);
			System.out.println("The IP Address of the Subscriber Server is "
					+ InetAddress.getLocalHost().getHostAddress());
		}
	}

	public Subscriber(String subscriberIP, String subscriberName)
			throws RemoteException {

		this.subscriberIP = subscriberIP;
		this.subscriberName = subscriberName;

	}

	public void joinSystem() throws RemoteException, NotBoundException,
			UnknownHostException, MalformedURLException {
		try {
			LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
					.lookup("//" + loadBalancerIP + ":" + LoadBalancerPort
							+ "/LoadBalancer");

			eventServiceIP = loadremoteObj.getEventServiceIP();
		} catch (Exception e) {
			LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
					.lookup("//" + LBBackUpIP + ":" + LBBackUpPort
							+ "/LBBackUp");

			eventServiceIP = loadremoteObj.getEventServiceIP();
		} finally {

			System.out.println();
			System.out.println("Enter the number of keywords");
			int number = sc.nextInt();
			System.out.println();
			System.out.println("Enter the keywords you are interested in");
			keywords = new ArrayList<String>();
			sc = new Scanner(System.in);
			for (int i = 0; i < number; i++) {
				String temp = sc.nextLine();
				temp = temp.toLowerCase();

				if (temp != null) {
					keywords.add(temp);
				}
			}

//			System.out.println(Arrays.toString(keywords.toArray()));
			// connect with Event Service
			EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
					.lookup("//" + eventServiceIP + ":" + ESPort
							+ "/EventService");

			String hostIP = InetAddress.getLocalHost().getHostAddress();
			String hostName = InetAddress.getLocalHost().getHostName();

			// this.s=new Subscriber(hostIP, hostName);
			eventremoteObj.finalSubJoin(hostIP, keywords);
		}
	}

	public void getFile(byte[] b, String fileName) throws RemoteException,
			IOException {
		File aFile = new File(fileName);

		FileOutputStream fop = new FileOutputStream(aFile);

		if (!aFile.exists()) {
			aFile.createNewFile();
		}

		fop.write(b);
		fop.flush();
		fop.close();

		System.out.println();
		System.out.println("File received at subscriber:" + fileName);

		System.out.println("It's content is:");
		System.out.println("---------------------------");
		Scanner sc1 = new Scanner(aFile);
		while (sc1.hasNext()) {
			String line = sc1.nextLine();
			System.out.println(line);
		}
		System.out.println("---------------------------");
	}

	public void subUpdate(ArrayList<String> temp) throws RemoteException {
		keywords = temp;
	}

	public void start() throws RemoteException, NotBoundException,
			UnknownHostException, MalformedURLException {

		int startInput = 0;
		while (startInput != 4) {

			System.out.println();
			System.out.println("--------------MENU----------------");
			System.out.println("1. Select keywords of interest ");
			System.out.println("2. Edit keywords ");
			System.out.println("3. View current keywords ");
			System.out.println("4. Unsubscribe ");
			System.out.println("5. Exit ");

			startInput = sc.nextInt();
			switch (startInput) {
			case 1: {

				joinSystem();
				break;
			}

			case 2: {
				EventServiceInterface eventremoteObj1 = (EventServiceInterface) Naming
						.lookup("//" + eventServiceIP + ":"
								+ ESPort + "/EventService");
				
				ArrayList<String> keys = eventremoteObj1.getK(InetAddress.getLocalHost().getHostAddress());
				System.out.println("The current keywords are:");
				System.out.println(keys);
				int startInput1 = 0;
				while (startInput1 != 3) {
					System.out.println();
					System.out.println("1. Add keywords ");
					System.out.println("2. Delete keywords ");
					System.out.println("3. Go back ");

					startInput1 = sc.nextInt();

					switch (startInput1) {
					case 1: {

						sc = new Scanner(System.in);
						System.out.println();
						System.out
								.println("Enter the number of keywords to add ");
						int number1 = sc.nextInt();
						moreKeywords = new ArrayList<String>();
						System.out.println();
						System.out
								.println("Enter the keywords you are interested in ");
						sc = new Scanner(System.in);
						for (int i = 0; i < number1; i++) {
							String temp = sc.nextLine();
							temp = temp.toLowerCase();

							if (temp != null) {
								moreKeywords.add(temp);
							}

						}

						try {
							LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
									.lookup("//" + loadBalancerIP + ":"
											+ LoadBalancerPort
											+ "/LoadBalancer");

							eventServiceIP = loadremoteObj.getEventServiceIP();
							EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
									.lookup("//" + eventServiceIP + ":"
											+ ESPort + "/EventService");
							String hostIP = InetAddress.getLocalHost()
									.getHostAddress();
							eventremoteObj
									.addMoreKeywords(hostIP, moreKeywords);

							break;

						} catch (Exception e) {
							LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
									.lookup("//" + LBBackUpIP + ":"
											+ LBBackUpPort + "/LBBackUp");

							eventServiceIP = loadremoteObj.getEventServiceIP();

							EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
									.lookup("//" + eventServiceIP + ":"
											+ ESPort + "/EventService");
							String hostIP = InetAddress.getLocalHost()
									.getHostAddress();
							eventremoteObj
									.addMoreKeywords(hostIP, moreKeywords);

							break;

						}
					}

					case 2: {

						sc = new Scanner(System.in);
						System.out.println();
						System.out
								.println("Enter the number of keywords to delete ");
						int number1 = sc.nextInt();

						deleteKeywords = new ArrayList<String>();
						System.out.println();
						System.out.println("Enter the keywords to be deleted ");
						sc = new Scanner(System.in);
						for (int i = 0; i < number1; i++) {
							String temp = sc.nextLine();
							temp = temp.toLowerCase();

							if (temp != null) {
								deleteKeywords.add(temp);
							}

						}

						try {
							LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
									.lookup("//" + loadBalancerIP + ":"
											+ LoadBalancerPort
											+ "/LoadBalancer");

							eventServiceIP = loadremoteObj.getEventServiceIP();
							EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
									.lookup("//" + eventServiceIP + ":"
											+ ESPort + "/EventService");
							String hostIP = InetAddress.getLocalHost()
									.getHostAddress();
							eventremoteObj.deleteKeywords(hostIP,
									deleteKeywords);

							break;

						} catch (Exception e) {
							LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
									.lookup("//" + LBBackUpIP + ":"
											+ LBBackUpPort + "/LBBackUp");

							eventServiceIP = loadremoteObj.getEventServiceIP();

							EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
									.lookup("//" + eventServiceIP + ":"
											+ ESPort + "/EventService");
							String hostIP = InetAddress.getLocalHost()
									.getHostAddress();
							eventremoteObj.deleteKeywords(hostIP,
									deleteKeywords);

							break;

						}

					}

					}

				}
				break;
			}
			
			case 3: {
				EventServiceInterface eventremoteObj2 = (EventServiceInterface) Naming
						.lookup("//" + eventServiceIP + ":"
								+ ESPort + "/EventService");
				
				ArrayList<String> keys = eventremoteObj2.getK(InetAddress.getLocalHost().getHostAddress());
				System.out.println("The current keywords are:");
				System.out.println(keys);
				break;
			}
			
			case 4: {
				System.out.println();
				System.out
						.println("Are you sure you want to unsubscribe?(Y/N)");
				sc = new Scanner(System.in);
				String decision = sc.next();
				if (decision.equals("Y") || decision.equals("y")) {
					EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
							.lookup("//" + eventServiceIP + ":" + ESPort
									+ "/EventService");
					String hostIP = InetAddress.getLocalHost().getHostAddress();
					eventremoteObj.unsubscribe(hostIP);
					System.out.println();
					System.out.println("Unsubscribed");
					System.exit(0);
				}
				break;

			}

			case 5: {
				System.exit(0);
				break;
			}
			}
		}

	}

	public static void main(String arg[]) throws RemoteException,
			UnknownHostException, NotBoundException, MalformedURLException {
		Subscriber s = new Subscriber();
		System.out.println();
		System.out.println("Connecting to server");
		Subscriber sub = new Subscriber(InetAddress.getLocalHost()
				.getHostAddress(), InetAddress.getLocalHost().getHostName());
		sub.start();
	}

}
