package sec.filesystem;

import java.net.InetAddress;
import java.rmi.Naming;
//import java.rmi.registry.LocateRegistry;

public class Server2 {

    private static final String SERVERNAME = "server-2";
    private static final int PORT = 1099;

    public static void main(String[] args) {
        try {
            String thisAddress;
            thisAddress = (InetAddress.getLocalHost()).toString();
            System.out.println("IP Address:" + thisAddress + " ---- Port: " + PORT);
            //LocateRegistry.createRegistry(PORT);
            ImplementationBlockServer obj = new ImplementationBlockServer();
            Naming.rebind("fs." + SERVERNAME, obj);
            System.out.println("FileSystem." + SERVERNAME + " is ready...");
        } catch (Exception ex) {
            System.out.println("FileSystem." + SERVERNAME + " exceptions: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
