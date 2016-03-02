package sec.filesystem;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import exception.InvalidSignatureException;
import types.*;
import utils.HashUtils;
import utils.CryptoUtils;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

	private static final long serialVersionUID = 1L;
	
	// Block table that will contain data blocks.
    private final HashMap<String, String> blockTable;

    public ImplementationBlockServer() throws RemoteException {
        blockTable = new HashMap<>();
    }

    private void storeBlock(Id_t id, String s) throws UnsupportedEncodingException {
        blockTable.put(id.getValue(), s);
    }

    private String retrieveBlock(Id_t id) throws UnsupportedEncodingException {
        return blockTable.get(id.getValue());
    }

    private Id_t calculateBlockID(Pk_t publicKey) throws NoSuchAlgorithmException, IOException {
    	
    	byte[] hash = HashUtils.hash(publicKey.getValue().toString(), null);

        return new Id_t(hash);
    }

    @Override
    public Data_t get(Id_t id) throws RemoteException {
        Block b = null;
        try {
            String s = retrieveBlock(id);
            FileInputStream fin = null;

            fin = new FileInputStream("./files/" + s + "/" + s + ".dat");

            ObjectInputStream ois = new ObjectInputStream(fin);
            b = (Block) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        //TODO verifyIntegrity(b);
        return b.getData();
    }

    @Override
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws RemoteException, InvalidSignatureException {

        	try {
				if(!CryptoUtils.verify(data.getValue(), public_key.getValue(), signature.getValue()))
					throw new InvalidSignatureException("Invalid signature.");

				System.out.println("signature is valid");
				
				Id_t id = calculateBlockID(public_key);
				System.out.println(id.getValue());
				Block b = new Block(data, signature, public_key);
				

				String s = id.getValue();

				FileOutputStream fout = null;
				
				new File("./files/" + s + "/").mkdirs();
      
				fout = new FileOutputStream("./files/" + s + "/" + s + ".dat");

				
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(b);
				oos.close();
				storeBlock(id, s);
				return id;
			} catch (InvalidSignatureException ise){
				ise.printStackTrace();
				throw ise;
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
