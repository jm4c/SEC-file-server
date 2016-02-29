package sec.filesystem;

import java.io.*;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.HashMap;
import types.*;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

	private static final long serialVersionUID = 1L;
	
	// Block table that will contain data blocks.
    private final HashMap<String, String> blockTable;

    public ImplementationBlockServer() throws RemoteException {
        blockTable = new HashMap<>();
    }

    private void storeBlock(Id_t id, String s) throws UnsupportedEncodingException {
        blockTable.put(new String(id.getValue(), "UTF-8"), s);
    }

    private String retrieveBlock(Id_t id) throws UnsupportedEncodingException {
        return blockTable.get(new String(id.getValue(), "UTF-8"));
    }

    private void validateSignature() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    private byte[] calculateBlockID() throws RemoteException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return bytes;
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

    //Temporary "put_k" method without Signature or PubKey support 
    @Override
    public Id_t put_k(Data_t data) throws RemoteException {
        //validateSignature();
        Id_t id = new Id_t(calculateBlockID());
        Block b = new Block(data);
        try {
            SecureRandom random = new SecureRandom();
            String s = new BigInteger(130, random).toString(32);
            FileOutputStream fout = null;
            
            new File("./files/" + s + "/").mkdirs();
        
            fout = new FileOutputStream("./files/" + s + "/" + s + ".dat");

            
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(b);
            oos.close();
            storeBlock(id, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
