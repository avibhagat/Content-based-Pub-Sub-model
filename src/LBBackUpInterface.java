import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This is an interface for communication with the backup gateway.
 *
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public interface LBBackUpInterface extends Remote {

	String getEventServiceIP() throws RemoteException, MalformedURLException,
			NotBoundException;

	void addNewEventService(String eventServiceIP) throws RemoteException;

	public void getUpdate(HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subscriberIPMapping)
			throws RemoteException;
}
