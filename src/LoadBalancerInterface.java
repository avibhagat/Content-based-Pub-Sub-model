import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This is an interface to communicating with the primary Gateway.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public interface LoadBalancerInterface extends Remote {

	String getEventServiceIP() throws RemoteException, MalformedURLException,
			NotBoundException;

	void addNewEventService(String eventServiceIP) throws RemoteException;

	public void getUpdate(HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subscriberIPMapping)
			throws RemoteException;

	public int getStateOfES() throws RemoteException;

	public HashMap<String, HashSet<String>> getKeywordMapping()
			throws RemoteException;

	public HashMap<String, ArrayList<String>> getSubIPMapping()
			throws RemoteException;

	public ArrayList<String> getEventServices() throws RemoteException;

	public ArrayList<Boolean> getEventServicesUpdate() throws RemoteException;

	void sendUpdates(int stateOfES,
			HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subIPMapping,
			ArrayList<String> eventServices,
			ArrayList<Boolean> eventServicesUpdate) throws RemoteException;
}
