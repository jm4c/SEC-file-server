package sec.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import sec.filesystem.InterfaceBlockServer;
import types.*;
import utils.CryptoUtils;

public class Client {

    private PrivateKey privateKey;

    private Pk_t publicKey;

    private Id_t clientID;

    private static InterfaceBlockServer server;

    //TEMPORARY. Used to test integration with the GUI
    private final String clientFileName = "testfile";

    private Client() {
    }

    //TEMPORARY. Used to test integration with the GUI
    private String getFileName() {
        return clientFileName;
    }

    private void setClientID(Id_t headerID) throws NoSuchAlgorithmException, IOException {
        this.clientID = headerID;
    }

    private void setPrivateKey(KeyPair kp) {
        this.privateKey = kp.getPrivate();
    }

    private void setPublicKey(KeyPair kp) {
        this.publicKey = new Pk_t(kp.getPublic());
    }

    private Id_t getClientID() {
        return clientID;
    }

    private PrivateKey getPrivateKey() {
        return privateKey;
    }

    private Pk_t getPublicKey() {
        return publicKey;
    }

    protected Id_t fs_init() throws Exception {
        KeyPair kp = CryptoUtils.setKeyPair();

        setPrivateKey(kp);
        setPublicKey(kp);

        String data = "File System Header initialization";
        Data_t serializedData = new Data_t(CryptoUtils.serialize(data));
        Sig_t signature = new Sig_t(CryptoUtils.sign(serializedData.getValue(), getPrivateKey()));

        Registry myReg = LocateRegistry.getRegistry("localhost");
        server = (InterfaceBlockServer) myReg.lookup("fs.Server");
        System.out.println(server.greeting() + "\n");

        System.out.println("DATA SENT: " + data + "\n");
        setClientID(server.put_k(serializedData, signature, getPublicKey()));

        //TEMPORARY. Used to test integration with the GUI
        new File("./files/").mkdirs();
        FileOutputStream fout = new FileOutputStream("./files/" + getFileName() + ".dat");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(new String("Hello!"));
        oos.close();

        return getClientID();
    }

    //TEMPORARY. Used to test integration with the GUI
    private void readFile(String s) throws IOException {
        try {
            FileInputStream fin = new FileInputStream("./files/" + s + ".dat");
            ObjectInputStream ois = new ObjectInputStream(fin);
            String contents = (String) ois.readObject();
            ois.close();
            System.out.println("File Contents: " + contents + "\n");
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TEMPORARY. Used to test integration with the GUI
    private void writeFile(String s) throws IOException {
        try {
            FileInputStream fin = new FileInputStream("./files/" + getFileName() + ".dat");
            ObjectInputStream ois = new ObjectInputStream(fin);
            String curr = (String) ois.readObject();
            ois.close();
            curr = curr.concat(s);
            FileOutputStream fout = new FileOutputStream("./files/" + getFileName() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(curr);
            oos.close();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fs_read(Id_t id, int pos, int size, Buffer_t contents) {
        try {
            //TODO  Read only "size" bytes, starting at position "pos"
            //TODO  Return # of bytes read
            //TODO  When files are stored in various blocks, method will need 
            //      to go retrieve all the blocks, and construct the full file, 
            //      before reading.
            Data_t data = server.get(id);
            contents.setValue(data.getValue());
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void fs_write(int pos, int size, Buffer_t contents) {
        try {
            //TODO  Write "contents" of size "size" at position "pos"
            //TODO  If filesize smaller than "pos"+"size", increase it.
            //TODO  If filesize smaller than "pos", pad with zeroes
            //TODO  As is, this is creating a new file with each write. Maybe 
            //      put should not be used ???? 
            Id_t id = this.getClientID();
            Data_t data = server.get(this.getClientID());
            byte[] a = data.getValue();
            byte[] b = contents.getValue();
            byte[] c = new byte[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
            if (!id.getValue().equals(server.put_k(new Data_t(c), new Sig_t(CryptoUtils.sign(c, this.getPrivateKey())), this.getPublicKey()).getValue())) {
                throw new Exception("Client's ID does not match main block ID!");
            }
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
    	Client c = new Client();
        try {
//            Registry myReg = LocateRegistry.getRegistry("localhost");
//            InterfaceBlockServer obj = (InterfaceBlockServer) myReg.lookup("fs.Server");
//            System.out.println(obj.greeting() + "\n");

            
            c.fs_init();

            //TEMPORARY. Used to test integration with the GUI
            System.out.println("TEMPORARY. Used to test integration with the GUI");
            System.out.println("-------------------------------------------------------------");
            c.readFile(c.getFileName());
            c.writeFile("new stuff!!!");
            c.readFile(c.getFileName());
            c.writeFile("mroe new stuff!!!");
            System.out.println("-------------------------------------------------------------");

            String data = "The quick brown fox jumps over the lazy dog";
            System.out.println("DATA TO SEND: " + data + "\n");
            byte[] serializedData = CryptoUtils.serialize(data);

            String unsignedData = "Server must refuse, wrong signature used";

            System.out.println("Storing a block on the block server...");

            System.out.println(c.getClientID().getValue());

            //TODO put_k must be inside fs init to set Client's ID
            if (!c.getClientID().equals(server.put_k(new Data_t(serializedData/*CryptoUtils.serialize(unsignedData)*/), new Sig_t(CryptoUtils.sign(serializedData, c.getPrivateKey())), c.getPublicKey())))
            	throw new Exception("Client's ID does not match main block ID!");
            
            System.out.println("Done!\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try{
            //TODO retrieve missing block
            System.out.println("Retrieving a block on the block server...");
            String data = (String) CryptoUtils.deserialize(server.get(c.getClientID()).getValue());
            System.out.println("Done!\n");

            System.out.println("DATA RECEIVED: " + data + "\n");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
