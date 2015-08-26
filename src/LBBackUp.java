import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class acts as a backup for the Gateway. Acts as the Gateway when the
 * primary Gateway fails
 *
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public class LBBackUp extends UnicastRemoteObject implements LBBackUpInterface,
		Serializable, Runnable {

	private static final long serialVersionUID = 1L;
	String eventServiceIP;
	static int stateOfES = 0;
	ArrayList<String> eventServices = new ArrayList<String>();
	HashMap<String, HashSet<String>> keywordMapping;
	HashMap<String, ArrayList<String>> subIPMapping;
	ArrayList<Boolean> eventServicesUpdate = new ArrayList<Boolean>();
	final int LBBackUpPort = 5233;
	final int LoadBalancerPort = 5122;
	static String loadBalancerIP = "129.21.30.38";
	static boolean flag = true;

	final int ESPort = 1120;

	public LBBackUp() throws RemoteException, UnknownHostException {
		// super();

		Registry registry = null;
		try {

			registry = LocateRegistry.getRegistry(LBBackUpPort);
			registry.list();
			registry.rebind("LBBackUp", this);
			System.out.println("The IP Address of the Load Balancer Server is "
					+ InetAddress.getLocalHost().getHostAddress());

		} catch (Exception e) {
			registry = LocateRegistry.createRegistry(LBBackUpPort);
			registry.rebind("LBBackUp", this);
			System.out.println("The IP Address of the Load Balancer Server is "
					+ InetAddress.getLocalHost().getHostAddress());
		}
	}

	public void addNewEventService(String eventServiceIP)
			throws RemoteException {

		eventServices.add(eventServiceIP);
		eventServicesUpdate.add(false);
	}

	public String getEventServiceIP() throws RemoteException,
			MalformedURLException, NotBoundException {
		if (eventServicesUpdate.get((stateOfES) % eventServices.size()) == true) {
			eventServicesUpdate.set((stateOfES) % eventServices.size(), false);

			EventServiceInterface eventremoteObj = (EventServiceInterface) Naming
					.lookup("//"
							+ eventServices.get((stateOfES)
									% eventServices.size()) + ":" + ESPort
							+ "/EventService");
			eventremoteObj.updateData(keywordMapping, subIPMapping);
		}

		return eventServices.get((stateOfES++) % eventServices.size());

	}

	public void run() {
		while (true) {
			try {
				LoadBalancerInterface loadremoteObj = (LoadBalancerInterface) Naming
						.lookup("//" + loadBalancerIP + ":" + LoadBalancerPort
								+ "/LoadBalancer");

				if (flag) {
					stateOfES = loadremoteObj.getStateOfES();
					keywordMapping = loadremoteObj.getKeywordMapping();
					subIPMapping = loadremoteObj.getSubIPMapping();
					eventServices = loadremoteObj.getEventServices();
					eventServicesUpdate = loadremoteObj
							.getEventServicesUpdate();
				} else {
					flag = true;
					loadremoteObj.sendUpdates(stateOfES, keywordMapping,
							subIPMapping, eventServices, eventServicesUpdate);
				}
				Thread.sleep(10000);
			} catch (Exception e) {

				flag = false;
			}
		}
	}

	public static void main(String arg[]) throws RemoteException,
			UnknownHostException {

		LBBackUp lb = new LBBackUp();
		new Thread(lb).start();
	}

	public void getUpdate(HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subscriberIPMapping)
			throws RemoteException {

		System.out.println("Received Updated List");

		this.keywordMapping = keywordMapping;
		subIPMapping = subscriberIPMapping;
		for (int i = 0; i < eventServicesUpdate.size(); i++) {
			eventServicesUpdate.set(i, true);
		}

	}

}
