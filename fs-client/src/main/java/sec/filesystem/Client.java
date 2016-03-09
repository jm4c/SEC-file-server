package sec.filesystem;

import interfaces.InterfaceBlockServer;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import types.*;
import utils.CryptoUtils;

public class Client {

    private PrivateKey privateKey;
    private Pk_t publicKey;
    private Id_t clientID;

    private static InterfaceBlockServer server;
    private List filesList;

    private Client() {
    	filesList = new ArrayList();
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
    
    private byte[][] splitContent(Buffer_t content){
    	
    	byte[][] filesArray = new byte[(int)Math.ceil(content.getValue().length / (double)InterfaceBlockServer.BLOCK_MAX_SIZE)][InterfaceBlockServer.BLOCK_MAX_SIZE];

        int ptr = 0;

        for(int i = 0; i < filesArray.length; i++) {
            filesArray[i] = Arrays.copyOfRange(content.getValue(), ptr, ptr + InterfaceBlockServer.BLOCK_MAX_SIZE);
            ptr += InterfaceBlockServer.BLOCK_MAX_SIZE ;
        }

        return filesArray;
    
    }

    protected Id_t fs_init() throws Exception {
        KeyPair kp = CryptoUtils.setKeyPair();

        setPrivateKey(kp);
        setPublicKey(kp);

        //current (empty) header file
        Data_t serializedData = new Data_t(CryptoUtils.serialize(this.filesList));
        Sig_t signature = new Sig_t(CryptoUtils.sign(serializedData.getValue(), getPrivateKey()));

        Registry myReg = LocateRegistry.getRegistry("localhost");
        server = (InterfaceBlockServer) myReg.lookup("fs.Server");
        System.out.println(server.greeting() + "\n");

        System.out.println("DATA SENT (empty header): " + this.filesList.toString() + "\n");
        setClientID(server.put_k(serializedData, signature, getPublicKey()));

        return getClientID();
    }

    protected int fs_read(Id_t id, int pos, int size, Buffer_t contents) {
        try {
            //TODO  When files are stored in various blocks, method will need 
            //      to go retrieve all the content blocks, and construct the 
            //      full file, before reading.        
            Data_t data = server.get(id);
            byte[] src = data.getValue();
            byte[] buff = new byte[size];

            //Adjusting buffer size for out of bound reads.
            int newLength = pos + size;
            if (src.length < newLength) {
                buff = new byte[src.length - pos];
            }

            System.arraycopy(src, pos, buff, 0, buff.length);
            contents.setValue(buff);
            return buff.length;

        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    protected void fs_write(int pos, int size, Buffer_t contents) {
        //TODO  When files are stored in various blocks, method will need 
        //      to as the server to only write the content blocks that suffer 
        //      alterations.  
        try {
        	
        	
            Id_t id = this.getClientID();
            Data_t data = server.get(this.getClientID());
            byte[] src = data.getValue();
            byte[] buff = contents.getValue();
            byte[] dest = new byte[src.length];

            //If file size smaller than pos+size, then it must be increased.
            //If file size smaller than pos, then it must be padded with zeroes.
            int newLength = pos + size;
            if (src.length < newLength) {
                dest = new byte[newLength];
            }

            System.arraycopy(src, 0, dest, 0, src.length);
            System.arraycopy(buff, 0, dest, pos, size);

//            System.out.println("\n---------------------------------------------------------");
//            System.out.println("LENGTH(src,buff,dest):" + src.length + "," + buff.length + "," + dest.length);
//            System.out.println("src: " + printHexBinary(src));
//            System.out.println("buff: " + printHexBinary(buff));
//            System.out.println("dest: " + printHexBinary(dest));
//            System.out.println("---------------------------------------------------------\n");
            Sig_t signature = new Sig_t(CryptoUtils.sign(dest, getPrivateKey()));
            if (!id.equals(server.put_k(new Data_t(dest), signature, getPublicKey()))) {
                throw new Exception("Client's ID does not match main block ID!");
            }
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            Client c = new Client();
            c.fs_init();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            //writing to the file
            buffer.setValue(CryptoUtils.serialize("The quick brown fox jumps over the lazy dog"));
            c.fs_write(50, buffer.getValue().length, buffer);

            //reading from the file
            int bytesRead = c.fs_read(c.getClientID(), 90, 20, buffer);
//            System.out.println("\n---------------------------------------------------------");
//            System.out.println("buff: " + printHexBinary(buffer.getValue()));
//            System.out.println("bytesRead: " + bytesRead);
//            System.out.println("---------------------------------------------------------\n");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
