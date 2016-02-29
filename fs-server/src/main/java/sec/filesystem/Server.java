package sec.filesystem;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {

    public static void main(String[] args) throws RemoteException{
    	
    	int port = 1099;
		String thisAddress;
		try {
			thisAddress = (InetAddress.getLocalHost()).toString();
		} catch (Exception e) {
			throw new RemoteException("Couldn't get this address.");
		}
		System.out.println("IP Address:" + thisAddress + " ---- Port: " + port);
		
        try {
            LocateRegistry.createRegistry(port);
            ImplementationBlockServer obj = new ImplementationBlockServer();
            Naming.rebind("fs.Server", obj);
            System.out.println("FileSystem.Server is ready...");
        } catch (Exception ex) {
            System.out.println("FileSystem.Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
