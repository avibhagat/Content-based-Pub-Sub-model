import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This file is an interface used to communicate with the event service.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public interface EventServiceInterface extends Remote {

	void finalSubJoin(String subIP, ArrayList<String> keywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException;

	void connectToLoadBalancer() throws MalformedURLException, RemoteException,
			NotBoundException, UnknownHostException;

	public void addMoreKeywords(String subIP, ArrayList<String> moreKeywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException;

	public void deleteKeywords(String subIP, ArrayList<String> deleteKeywords)
			throws RemoteException, MalformedURLException,
			UnknownHostException, NotBoundException;

	public void unsubscribe(String subIP) throws RemoteException;

	void updateData(HashMap<String, HashSet<String>> keywordMapping,
			HashMap<String, ArrayList<String>> subIPMapping)
			throws RemoteException;

	void finalPubJoin(String hostIP, byte[] fileContent, String fileName)
			throws NotBoundException, IOException;

	public ArrayList<String> getK(String ip) throws RemoteException;

}
