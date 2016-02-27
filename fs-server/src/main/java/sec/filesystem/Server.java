package sec.filesystem;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ImplementationBlockServer obj = new ImplementationBlockServer();
            Naming.rebind("fs.Server", obj);
            System.out.println("FileSystem.Server is ready...");
        } catch (Exception ex) {
            System.out.println("FileSystem.Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
