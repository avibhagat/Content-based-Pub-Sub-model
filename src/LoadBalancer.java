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
 * This is the primary Gateway which balances the load and stores the updated
 * information. Piggyback concept is implemented here.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public class LoadBalancer extends UnicastRemoteObject implements
		LoadBalancerInterface, Serializable {

	private static final long serialVersionUID = 1L;
	String eventServiceIP;
	static int stateOfES = 0;
	ArrayList<String> eventServices = new ArrayList<String>();
	HashMap<String, HashSet<String>> keywordMapping;
	HashMap<String, ArrayList<String>> subIPMapping;
	ArrayList<Boolean> eventServicesUpdate = new ArrayList<Boolean>();

	final int ESPort = 1120;
	final int LoadBalancerPort = 5122;

	public LoadBalancer() throws RemoteException, UnknownHostException {
		// super();

		Registry registry = null;
		try {

			registry = LocateRegistry.getRegistry(LoadBalancerPort);
			registry.list();
			registry.rebind("LoadBalancer", this);
			System.out.println("The IP Address of the Load Balancer Server is "
					+ InetAddress.getLocalHost().getHostAddress());

		} catch (Exception e) {
			registry = LocateRegistry.createRegistry(LoadBalancerPort);
			registry.rebind("LoadBalancer", this);
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

	public int getStateOfES() throws RemoteException {

		return stateOfES;
	}

	public HashMap<String, HashSet<String>> getKeywordMapping()
			throws RemoteException {

		return keywordMapping;
	}

	public HashMap<String, ArrayList<String>> getSubIPMapping()
			throws RemoteException {

		return subIPMapping;
	}

	public ArrayList<String> getEventServices() throws RemoteException {

		return eventServices;
	}

	public ArrayList<Boolean> getEventServicesUpdate() throws RemoteException {

		return eventServicesUpdate;
	}

	public void sendUpdates(int stateOfES,
			HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subIPMapping,
			ArrayList<String> eventServices,
			ArrayList<Boolean> eventServicesUpdate) throws RemoteException {

		this.stateOfES = stateOfES;
		this.keywordMapping = keywordMapping;
		this.subIPMapping = subIPMapping;
		this.eventServices = eventServices;
		this.eventServicesUpdate = eventServicesUpdate;

	}

	public static void main(String arg[]) throws RemoteException,
			UnknownHostException {

		LoadBalancer lb = new LoadBalancer();
	}

}
