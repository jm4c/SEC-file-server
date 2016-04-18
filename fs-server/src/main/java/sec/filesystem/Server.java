package sec.filesystem;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {

    private static final int PORT = 1099;

    public static void main(String[] args) {
        String thisAddress;
        String servername = "server-0";
        try {
            thisAddress = (InetAddress.getLocalHost()).toString();
            System.out.println("IP Address:" + thisAddress + " ---- Port: " + PORT);
            if (args.length > 0) {
                servername = args[0];
            }
            if (servername.equalsIgnoreCase("server-0")) {
                LocateRegistry.createRegistry(PORT);
            }
            ImplementationBlockServer obj = new ImplementationBlockServer();
            Naming.rebind("fs." + servername, obj);
            System.out.println("FileSystem." + servername + " is ready...");
        } catch (Exception ex) {
            System.out.println("FileSystem." + servername + " exceptions: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
