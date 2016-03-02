package sec.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Arrays;

import sec.filesystem.InterfaceBlockServer;
import types.*;
import utils.CryptoUtils;
import utils.HashUtils;

public class Client {
	
	private PrivateKey privateKey;
	
	private Pk_t publicKey;

    private Id_t clientID;
    
    private static InterfaceBlockServer server;
    
    private Client(){      
    }
    
    private void setClientID() throws NoSuchAlgorithmException, IOException{
//    	System.out.println(getPublicKey().getValue().toString());
    	byte[] hash = HashUtils.hash(getPublicKey().getValue().toString(), null);
        this.clientID = new Id_t(hash);
    }
    
    private void setPrivateKey(KeyPair kp){
    	this.privateKey = kp.getPrivate();
    }
    
    private void setPublicKey(KeyPair kp){
    	this.publicKey = new Pk_t(kp.getPublic());
    }
    
    private Id_t getClientID(){
        return clientID;
    }
    
	private PrivateKey getPrivateKey(){
        return privateKey;
    }
    
	private Pk_t getPublicKey(){
        return publicKey;
    }
	
    
    protected Id_t fs_init() throws NotBoundException, NoSuchAlgorithmException, IOException {
    	KeyPair kp = CryptoUtils.setKeyPair();
    	
    	setPrivateKey(kp);
    	setPublicKey(kp);
    	setClientID();

    	
    	Registry myReg = LocateRegistry.getRegistry("localhost");
        server = (InterfaceBlockServer) myReg.lookup("fs.Server");
        System.out.println(server.greeting() + "\n");
    	
        return getClientID();
    	
    }
     
    public static void main(String[] args) {
        try {
//            Registry myReg = LocateRegistry.getRegistry("localhost");
//            InterfaceBlockServer obj = (InterfaceBlockServer) myReg.lookup("fs.Server");
//            System.out.println(obj.greeting() + "\n");

            Client c = new Client();
            c.fs_init();
            
            String data = "The quick brown fox jumps over the lazy dog";
            System.out.println("DATA: " + data + "\n");

            System.out.println("Storing a block on the block server..."); 
//            c.setClientID(obj.put_k(new Data_t(data.getBytes("UTF-8"))));
            
            
//            System.out.println(c.getClientID().getValue().toString().substring(3));
            
            
            if (!c.getClientID().equals(server.put_k(new Data_t(data.getBytes("UTF-8")), null, c.getPublicKey())))
            	throw new Exception("Client's ID does not match main block ID!");
            System.out.println("Done!\n");
            
            
//            System.out.println("Retrieving a block on the block server...");
//            data = new String(obj.get(c.getClientID()).getValue(), "UTF-8");
//            System.out.println("Done!\n");
//
//            System.out.println("DATA: " + data + "\n");

        } catch (Exception ex) {
            System.out.println("FileSystem.Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
