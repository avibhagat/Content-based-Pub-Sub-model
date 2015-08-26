import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This is an interface which is used to communicate with the subscriber.
 * 
 * @author Avi Bhagat
 * @author Rohit Giyanani
 * @author Mohita Jethwani
 *
 */

public interface SubscriberInterface extends Remote {

	public void getFile(byte[] b, String fileName) throws RemoteException,
			IOException;

	public void subUpdate(ArrayList<String> temp) throws RemoteException;
}
