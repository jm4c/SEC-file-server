package sec.filesystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import sec.filesystem.InterfaceBlockServer;

public class Client {

    public static void main(String[] args) {
        try {
            Registry myReg = LocateRegistry.getRegistry("localhost");
            InterfaceBlockServer obj = (InterfaceBlockServer) myReg.lookup("fs.Server");
            System.out.println(obj.greeting());
        } catch (Exception ex) {
            System.out.println("FileSystem.Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
