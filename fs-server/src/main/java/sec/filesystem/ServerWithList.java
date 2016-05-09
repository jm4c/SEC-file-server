package sec.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class ServerWithList {

    private static final int PORT = 1099;
    private static final String SERVER_LIST_PATH = "../fs-utils/serverlist.dat";

    public static void main(String[] args) {
        String thisAddress;
        String serverName = "server-0";
        ArrayList<String> serverList = new ArrayList<>();

        // Required resources
        FileInputStream fin;
        ObjectInputStream ois;
        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            //  Print IP and Port
            thisAddress = (InetAddress.getLocalHost()).toString();
            System.out.println("IP Address:" + thisAddress + " ---- Port: " + PORT);

            //  If serverList doesn't exist, create an empty list and store it
            if (!new File(SERVER_LIST_PATH).isFile()) {
                File f = new File(SERVER_LIST_PATH);
                f.getParentFile().mkdirs();
                f.createNewFile();
                fos = new FileOutputStream(SERVER_LIST_PATH);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(serverList);
            }
            System.out.println(new File(SERVER_LIST_PATH).getAbsolutePath());


            //  Read the serverList file
            fin = new FileInputStream(SERVER_LIST_PATH);
            ois = new ObjectInputStream(fin);
            serverList = (ArrayList) ois.readObject();

            //  Default server name, should no name be provided
            if (args.length > 0) {
                serverName = args[0];
            }

            //  (Java RMI) 
            //  If the list is empty, then this is the first server instance,
            //  meaning we must create the registry
            if (serverList.isEmpty()) {
                LocateRegistry.createRegistry(PORT);
            }

            //  Add the server name to the list
            if (!serverList.contains(serverName)) {
                serverList.add(serverName);
                //  ERROR CASE
                //  Two servers should not have the same name
            } else {
                String msg = "FileSystem." + serverName + " Exception: Server Name already in use.";
                System.err.println(msg);
                System.exit(-1);
            }

            //  (Java RMI) 
            //  Rebinding of the server object
            ImplementationBlockServer obj = new ImplementationBlockServer();
            Naming.rebind("fs." + serverName, obj);

            //  Writing the new server list to file
            fos = new FileOutputStream(SERVER_LIST_PATH);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(serverList);

            System.out.println("FileSystem." + serverName + " is ready...");
            
            // Closing all open resources
            oos.close();
            ois.close();
            fos.close();
            fin.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("FileSystem." + serverName + " Exception: " + ex.getMessage());
        }
    }
}
