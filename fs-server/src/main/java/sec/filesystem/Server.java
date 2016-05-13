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

public class Server {

    private static final int PORT = 1099;
    private static final String SERVERLISTPATH = "../fs-utils/serverlist.dat";

    public static void main(String[] args) {
        String thisAddress;
        String servername = "server-0";
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
            if (!new File(SERVERLISTPATH).isFile()) {
                File f = new File(SERVERLISTPATH);
                f.getParentFile().mkdirs();
                f.createNewFile();
                fos = new FileOutputStream(SERVERLISTPATH);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(serverList);
            }

            //  Read the serverList file
            fin = new FileInputStream(SERVERLISTPATH);
            ois = new ObjectInputStream(fin);
            serverList = (ArrayList) ois.readObject();

            //  Default server name, should no name be provided
            if (args.length > 0) {
                servername = args[0];
            }

            //  (Java RMI) 
            //  If the list is empty, then this is the first server instance,
            //  meaning we must create the registry
            if (serverList.isEmpty()) {
                LocateRegistry.createRegistry(PORT);
            }

            //  Add the server name to the list
            if (!serverList.contains(servername)) {
                serverList.add(servername);
                //  ERROR CASE
                //  Two servers should not have the same name
            } else {
                String msg = "FileSystem." + servername + " Exception: Server Name already in use.";
                System.err.println(msg);
                System.exit(-1);
            }

            //  (Java RMI) 
            //  Rebinding of the server object
            ImplementationBlockServer obj = new ImplementationBlockServer(0);
            Naming.rebind("fs." + servername, obj);

            //  Writing the new server list to file
            fos = new FileOutputStream(SERVERLISTPATH);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(serverList);

            System.out.println("FileSystem." + servername + " is ready...");
            
            // Closing all open resources
            oos.close();
            ois.close();
            fos.close();
            fin.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("FileSystem." + servername + " Exception: " + ex.getMessage());
        }
    }
}
