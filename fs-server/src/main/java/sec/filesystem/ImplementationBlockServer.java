package sec.filesystem;

import java.io.*;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import types.*;
import utils.HashUtils;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

	private static final long serialVersionUID = 1L;
	
	// Block table that will contain data blocks.
    private final HashMap<String, String> blockTable;

    public ImplementationBlockServer() throws RemoteException {
        blockTable = new HashMap<>();
    }

    private void storeBlock(Id_t id, String s) throws UnsupportedEncodingException {
        blockTable.put(id.getValue().toString(), s);
    }

    private String retrieveBlock(Id_t id) throws UnsupportedEncodingException {
        return blockTable.get(id.getValue().toString());
    }

    private void validateSignature() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");

    }


    private Id_t calculateBlockID(Pk_t publicKey) throws NoSuchAlgorithmException, IOException {
//        SecureRandom random = new SecureRandom();
//        byte bytes[] = new byte[20];
//        random.nextBytes(bytes);
//        return bytes;
//    	System.out.println(publicKey.getValue().toString());
    	byte[] hash = HashUtils.hash(publicKey.getValue().toString(), null);

        return new Id_t(hash);
    }

    @Override
    public Data_t get(Id_t id) throws RemoteException {
        Block b = null;
        try {
            String s = retrieveBlock(id);
            FileInputStream fin = null;
            
            //fixes file not found exception in eclipse/windows
//            try {
            	fin = new FileInputStream("./files/" + s + "/" + s + ".dat");
//            }catch (FileNotFoundException fnfe){
//            	fin = new FileInputStream(".\\files\\" + s + ".dat");
//            }
            
            ObjectInputStream ois = new ObjectInputStream(fin);
            b = (Block) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //verifyIntegrity(b);
        return b.getData();
    }

    @Override
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws RemoteException {
        
        try {
        	//validateSignature();
            Id_t id = calculateBlockID(public_key);
            Block b = new Block(data, signature, public_key);
            
//            SecureRandom random = new SecureRandom();
//            String s = new BigInteger(130, random).toString(32);
            String s = id.getValue().toString();
            s = s.substring(3); // removes [B@ from the beginning of the string
            FileOutputStream fout = null;
            
            new File("./files/" + s + "/").mkdirs();
        
            fout = new FileOutputStream("./files/" + s + "/" + s + ".dat");

            
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(b);
            oos.close();
            storeBlock(id, s);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Id_t put_h(Data_t data) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String greeting() throws RemoteException {
        return "Hello There!";
    }

}
