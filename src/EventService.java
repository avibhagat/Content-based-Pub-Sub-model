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
import java.util.regex.Pattern;
import java.rmi.server.*;

/**
 * This class is used to store information about the subscriber when it is not
 * online.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

class SubInfo {
	String ipAddress;
	int pingNo;
	byte[] b;
	String fileName;

	SubInfo(String ipAddress, int pingNo, byte[] b, String fileName) {
		this.ipAddress = ipAddress;
		this.pingNo = pingNo;
		this.b = b;
		this.fileName = fileName;
	}
}

/**
 * Event Service class. All the functionality of the event service is in this
 * class.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public class EventService extends UnicastRemoteObject implements
		EventServiceInterface, Serializable, Runnable {

	static Scanner sc = new Scanner(System.in);
	private static final long serialVersionUID = 1L;
	static ArrayList<String> publishersInTheSystem = new ArrayList<String>();
	static ArrayList<String> subscribersInTheSystem = new ArrayList<String>();
	static String loadBalancerIP = "129.21.30.38";
	HashMap<String, HashSet<String>> keywordMapping = new HashMap<String, HashSet<String>>();
	HashMap<String, ArrayList<String>> subscriberIPMapping = new HashMap<String, ArrayList<String>>();
	final int ESPort = 1120;
	final int LoadBalancerPort = 5122;
	final int subPort = 2120;
	static String LBBackUpIP = "129.21.30.50";
	final int LBBackUpPort = 5233;
	ArrayList<SubInfo> buffer = new ArrayList<SubInfo>();

	public EventService() throws RemoteException, UnknownHostException {
		super();

		Registry registry = null;
		try {

			registry = LocateRegistry.getRegistry(ESPort);

			registry.list();
			registry.rebind("EventService", this);
			System.out.println("The IP Address of the Event Service Server is "
					+ InetAddress.getLocalHost().getHostAddress());

		} catch (Exception e) {
			registry = LocateRegistry.createRegistry(ESPort);
			registry.rebind("EventService", this);
			System.out.println("The IP Address of the Event Service Server is "
					+ InetAddress.getLocalHost().getHostAddress());
		}
	}

	public void sendFile(byte[] b, String fileName,
			HashSet<String> subscriberList) throws NotBoundException,
			IOException {

		Iterator it = subscriberList.iterator();
		while (it.hasNext()) {
			String ip = it.next().toString();
			try {
				SubscriberInterface sendFileObj = (SubscriberInterface) Naming
						.lookup("//" + ip + ":" + subPort + "/Subscriber");
				sendFileObj.getFile(b, fileName);
			} catch (Exception e) {
				SubInfo temp = new SubInfo(ip, 0, b, fileName);
				buffer.add(temp);
			}

		}
	}

	public void run() {
		while (true) {
			if (buffer.size() != 0) {
				Iterator it = buffer.iterator();
				while (it.hasNext()) {
					SubInfo t = (SubInfo) it.next();
					try {
						SubscriberInterface sendFileObj = (SubscriberInterface) Naming
								.lookup("//" + t.ipAddress + ":" + subPort
										+ "/Subscriber");
						sendFileObj.getFile(t.b, t.fileName);
						ArrayList<String> temp = subscriberIPMapping
								.get(t.ipAddress);
						sendFileObj.subUpdate(temp);
						it.remove();
					} catch (Exception e) {
						t.pingNo++;
						if (t.pingNo >= 10) {
							it.remove();
						}
					}
				}
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	public void sendUpdatetoLoadBalancer() throws MalformedURLException,
			RemoteException, NotBoundException, UnknownHostException {
		try {
			LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
					.lookup("//" + loadBalancerIP + ":" + LoadBalancerPort
							+ "/LoadBalancer");

			loadremoteObj.getUpdate(keywordMapping, subscriberIPMapping);
		} catch (Exception e) {
			LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
					.lookup("//" + LBBackUpIP + ":" + LBBackUpPort
							+ "/LBBackUp");
			loadremoteObj.getUpdate(keywordMapping, subscriberIPMapping);
		}
	}

	public HashSet<String> parseText(File aFile) throws FileNotFoundException {
		Scanner sc2 = new Scanner(aFile);
		HashSet<String> subscriberList = new HashSet<String>();
		while (sc2.hasNext()) {
			String line = sc2.nextLine();
			String words[] = line.split(" ");
			for (int i = 0; i < words.length; i++) {
				words[i] = words[i].replaceAll("[^a-zA-Z0-9]", "");
				words[i] = words[i].toLowerCase();
				if (keywordMapping.containsKey(words[i])) {
					HashSet<String> temp = keywordMapping.get(words[i]);
					Iterator it = temp.iterator();
					while (it.hasNext()) {
						subscriberList.add((String) it.next());
					}
				}
			}
		}
		sc2.close();
		return subscriberList;

	}

	public void finalPubJoin(String pubIP, byte[] b, String fileName)
			throws NotBoundException, IOException {

		publishersInTheSystem.add(pubIP);

		File aFile = new File(fileName);

		FileOutputStream fop = new FileOutputStream(aFile);

		if (!aFile.exists()) {
			aFile.createNewFile();
		}

		fop.write(b);
		fop.flush();
		fop.close();

		HashSet<String> subscriberList = new HashSet<String>();
		subscriberList = parseText(aFile);
		Iterator it = subscriberList.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}

		byte[] toSend = new byte[(int) aFile.length()];

		FileInputStream fip = new FileInputStream(aFile);
		fip.read(toSend);
		fip.close();

		if (subscriberList.size() != 0) {
			sendFile(toSend, fileName, subscriberList);
		}
	}

	public void deleteKeywords(String subIP, ArrayList<String> deleteKeywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException {

		Iterator it1 = deleteKeywords.iterator();
		while (it1.hasNext()) {
			String keyword = (String) it1.next();
			if (!keywordMapping.containsKey(keyword)) {
				System.out.println("Keyword " + keyword + " not found ");
				it1.remove();
			}

		}

		for (int j = 0; j < deleteKeywords.size(); j++) {

			// gets all the subscribers interested in that keyword
			HashSet<String> temp = keywordMapping.get(deleteKeywords.get(j));

			Iterator it = temp.iterator();
			while (it.hasNext()) {
				String iP = (String) it.next();
				if (iP.equals(subIP)) {

					// only 1 subscriber interested
					if (temp.size() == 1) {
						// remove the keyword from the hashMap
						keywordMapping.remove(deleteKeywords.get(j));
					}

					else {
						// remove only the subscriber
						it.remove();
					}
				}
			}

			if (keywordMapping.containsKey(deleteKeywords.get(j))) {
				keywordMapping.put(deleteKeywords.get(j), temp);
			}

		}

		ArrayList<String> temp1 = new ArrayList<String>();
		for (int i = 0; i < deleteKeywords.size(); i++) {

			// gets all keywords of that particular subscriber
			temp1 = subscriberIPMapping.get(subIP);

			Iterator it = temp1.iterator();
			while (it.hasNext()) {
				String t = it.next().toString();
				if (t.equals(deleteKeywords.get(i))) {

					// that subscriber has only 1 keyword
					if (temp1.size() == 1) {

						// remove that subscriber from hashMap
						subscriberIPMapping.remove(subIP);
					}

					else {
						// remove keyword
						it.remove();
					}
				}
			}
		}

		if (subscriberIPMapping.containsKey(subIP)) {
			subscriberIPMapping.put(subIP, temp1);
		}

		System.out.println();
		System.out.println("Keyword Mapping");

		Iterator it = keywordMapping.entrySet().iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}

		System.out.println();
		System.out.println("Subscribers interests");

		Iterator it2 = subscriberIPMapping.entrySet().iterator();
		while (it2.hasNext()) {
			System.out.println(it2.next());
		}

		sendUpdatetoLoadBalancer();

	}

	public void addMoreKeywords(String subIP, ArrayList<String> moreKeywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException {

		Iterator it = moreKeywords.iterator();
		while (it.hasNext()) {
			String keyword = (String) it.next();
			if (!keywordMapping.containsKey(keyword)) {
				HashSet<String> temp = new HashSet<String>();
				temp.add(subIP);
				keywordMapping.put(keyword, temp);
			}

			else if (keywordMapping.containsKey(keyword)) {
				HashSet<String> temp = new HashSet<String>();
				temp = keywordMapping.get(keyword);
				temp.add(subIP);
				keywordMapping.put(keyword, temp);

			}
		}

		ArrayList<String> temp1 = new ArrayList<String>();
		if (subscriberIPMapping.containsKey(subIP)) {
			temp1 = subscriberIPMapping.get(subIP);

		}
		Iterator it2 = moreKeywords.iterator();
		while (it2.hasNext()) {
			String keyword = (String) it2.next();

			temp1.add(keyword);
		}

		subscriberIPMapping.put(subIP, temp1);

		System.out.println();
		System.out.println("Keyword Mapping");

		Iterator it1 = keywordMapping.entrySet().iterator();
		while (it1.hasNext()) {
			System.out.println(it1.next());
		}

		System.out.println();
		System.out.println("Subscribers interests");

		Iterator it3 = subscriberIPMapping.entrySet().iterator();
		while (it3.hasNext()) {
			System.out.println(it3.next());
		}

		sendUpdatetoLoadBalancer();

	}

	public void finalSubJoin(String subIP, ArrayList<String> keywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException {

		subscribersInTheSystem.add(subIP);

		Iterator it1 = keywords.iterator();
		while (it1.hasNext()) {
			String keyword = (String) it1.next();
			if (!keywordMapping.containsKey(keyword)) {
				HashSet<String> temp = new HashSet<String>();
				temp.add(subIP);
				keywordMapping.put(keyword, temp);
			}

			else {

				HashSet<String> temp = keywordMapping.get(keyword);
				temp.add(subIP);
				keywordMapping.put(keyword, temp);
			}
		}

		subscriberIPMapping.put(subIP, keywords);

		System.out.println();
		System.out.println("Keyword Mapping");

		Iterator it = keywordMapping.entrySet().iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}

		System.out.println();
		System.out.println("Subscribers interests");

		Iterator it2 = subscriberIPMapping.entrySet().iterator();
		try {
			while (it2.hasNext()) {
				System.out.println(it2.next());
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		sendUpdatetoLoadBalancer();

	}

	public void connectToLoadBalancer() throws MalformedURLException,
			RemoteException, NotBoundException, UnknownHostException {

		System.out.println();
		System.out.println("Connecting to Load Balancer");
		try {
			LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
					.lookup("//" + loadBalancerIP + ":" + LoadBalancerPort
							+ "/LoadBalancer");

			loadremoteObj.addNewEventService(InetAddress.getLocalHost()
					.getHostAddress());
		} catch (Exception e) {
			LBBackUpInterface loadremoteObj = (LBBackUpInterface) Naming
					.lookup("//" + LBBackUpIP + ":" + LBBackUpPort
							+ "/LBBackUp");
			loadremoteObj.addNewEventService(InetAddress.getLocalHost()
					.getHostAddress());
		}
	}

	public void unsubscribe(String subIP) throws RemoteException {
		if (!subscriberIPMapping.containsKey(subIP)) {
			return;
		} else {
			ArrayList<String> temp = subscriberIPMapping.get(subIP);
			for (int i = 0; i < temp.size(); i++) {
				if (keywordMapping.containsKey(temp.get(i))) {
					HashSet<String> temp1 = keywordMapping.get(temp.get(i));
					temp1.remove(subIP);

				}
			}
			subscriberIPMapping.remove(subIP);
		}
	}

	public void updateData(HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subIPMapping)
			throws RemoteException {
		System.out.println("Received Updated data from Load Balancer");
		this.keywordMapping = keywordMapping;
		subscriberIPMapping = subIPMapping;
	}

	@Override
	public ArrayList<String> getK(String ip) throws RemoteException {
		ArrayList<String> re = subscriberIPMapping.get(ip);
		return re;
	}

	public static void main(String arg[]) throws RemoteException,
			UnknownHostException, MalformedURLException, NotBoundException {
		EventService es = new EventService();
		es.connectToLoadBalancer();
		new Thread(es).start();
	}
}
