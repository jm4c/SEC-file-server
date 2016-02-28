package sec.filesystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import sec.filesystem.InterfaceBlockServer;
import types.*;

public class Client {

    private Id_t clientID;
    
    private Client(){      
    }
    
    private void setClientID(Id_t id){
        this.clientID = id;
    }
    
    private Id_t getClientID(){
        return clientID;
    }
     
    public static void main(String[] args) {
        try {
            Registry myReg = LocateRegistry.getRegistry("localhost");
            InterfaceBlockServer obj = (InterfaceBlockServer) myReg.lookup("fs.Server");
            System.out.println(obj.greeting() + "\n");

            Client c = new Client();
            
            String data = "The quick brown fox jumps over the lazy dog";
            System.out.println("DATA: " + data + "\n");

            System.out.println("Storing a block on the block server..."); 
            c.setClientID(obj.put_k(new Data_t(data.getBytes("UTF-8"))));
            System.out.println("Done!\n");
            
            System.out.println("Retrieving a block on the block server...");
            data = new String(obj.get(c.getClientID()).getValue(), "UTF-8");
            System.out.println("Done!\n");

            System.out.println("DATA: " + data + "\n");

        } catch (Exception ex) {
            System.out.println("FileSystem.Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
