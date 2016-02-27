package sec.filesystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import sec.filesystem.InterfaceBlockServer;
import types.*;

public class Client {

    public static void main(String[] args) {
        try {
            Registry myReg = LocateRegistry.getRegistry("localhost");
            InterfaceBlockServer obj = (InterfaceBlockServer) myReg.lookup("fs.Server");
            System.out.println(obj.greeting() + "\n");

            String data = "The quick brown fox jumps over the lazy dog";
            System.out.println("DATA: " + data + "\n");

            System.out.println("Storing a block on the block server...");
            Id_t id = obj.put_k(new Data_t(data.getBytes()));
            System.out.println("Done!\n");
            
            System.out.println("Retrieving a block on the block server...");
            data = new String(obj.get(id).getValue());
            System.out.println("Done!\n");

            System.out.println("DATA: " + data + "\n");

        } catch (Exception ex) {
            System.out.println("FileSystem.Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
