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
import utils.HashUtils;

public class Client {

    private PrivateKey privateKey;
    private Pk_t publicKey;
    private Id_t clientID;

    private static InterfaceBlockServer server;

    protected Client() {
   
    }

    protected void setClientID(Id_t headerID) throws NoSuchAlgorithmException, IOException {
        this.clientID = headerID;
    }

    private void setPrivateKey(KeyPair kp) {
        this.privateKey = kp.getPrivate();
    }

    private void setPublicKey(KeyPair kp) {
        this.publicKey = new Pk_t(kp.getPublic());
    }

    protected Id_t getClientID() {
        return clientID;
    }

    private PrivateKey getPrivateKey() {
        return privateKey;
    }

    private Pk_t getPublicKey() {
        return publicKey;
    }

    private byte[][] splitContent(Buffer_t content) {

        byte[][] filesArray = new byte[(int) Math.ceil(content.getValue().length / (double) InterfaceBlockServer.BLOCK_MAX_SIZE)][InterfaceBlockServer.BLOCK_MAX_SIZE];

        int ptr = 0;

        for (int i = 0; i < filesArray.length; i++) {
            filesArray[i] = Arrays.copyOfRange(content.getValue(), ptr, ptr + InterfaceBlockServer.BLOCK_MAX_SIZE);
            ptr += InterfaceBlockServer.BLOCK_MAX_SIZE;
        }

        return filesArray;

    }

    private Buffer_t joinContent(byte[][] filesArray) {

        byte[] b = new byte[filesArray.length * InterfaceBlockServer.BLOCK_MAX_SIZE];
        int ptr = 0;

        for (int i = 0; i < filesArray.length; i++) {
            System.arraycopy(filesArray[i], 0, b, ptr, InterfaceBlockServer.BLOCK_MAX_SIZE);
            ptr += InterfaceBlockServer.BLOCK_MAX_SIZE;
        }
        Buffer_t content = new Buffer_t(b);

        return content;

    }

    protected Id_t fs_init() throws Exception {
        KeyPair kp = CryptoUtils.setKeyPair();

        setPrivateKey(kp);
        setPublicKey(kp);

        //current (empty) header file
        List<Id_t> header = new ArrayList<Id_t>();
        Data_t headerData = new Data_t(CryptoUtils.serialize(header));
        Sig_t signature = new Sig_t(CryptoUtils.sign(headerData.getValue(), getPrivateKey()));

        Registry myReg = LocateRegistry.getRegistry("localhost");
        server = (InterfaceBlockServer) myReg.lookup("fs.Server");
        System.out.println(server.greeting() + "\n");

        System.out.println("DATA SENT (empty header): " + header.toString() + "\n");
        setClientID(server.put_k(headerData, signature, getPublicKey()));

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
    	System.out.println("\nNew FS write");
    	//TODO	fs_write must write "size" bytes in position "pos" of the file
        try {
        	if (contents == null)
        		throw new Exception("Content is null");
        	
        	//Client's ID can only be a header file
        	Data_t data = server.get(this.getClientID());
        	
        	//Header file's data is always a list of other files' IDs
        	@SuppressWarnings("unchecked")
			List<Id_t> originalFileList = (List<Id_t>) CryptoUtils.deserialize(data.getValue());
			
			Buffer_t base = null;
			
			if (originalFileList.isEmpty()){
				base = new Buffer_t(new byte[pos+size]);
			}else{
				byte[][] originalContentParts = new byte[originalFileList.size()][InterfaceBlockServer.BLOCK_MAX_SIZE];
				for(int i = 0; i < originalFileList.size(); i++){
					originalContentParts[i] = server.get(originalFileList.get(i)).getValue();
				}
				base = joinContent(originalContentParts);
				
				
				//	puts old content into a bigger file
				if(base.getValue().length < pos + size)
				{
					Buffer_t auxBase = new Buffer_t(new byte[pos+size]);
					System.arraycopy(base.getValue(), 0, auxBase.value, 0, size);
					base = auxBase;
				}
			}
			System.arraycopy(contents.getValue(), 0, base.value, pos, size);        	
        	
        	
        	byte[][] filesArray = splitContent(contents);
        	
        	List<Id_t> newFileList = new ArrayList<Id_t>();
        	for (int i = 0; i < filesArray.length; i++) {
        		newFileList.add(new Id_t (HashUtils.hash(filesArray[i], null)));
        	}
        	
        	Data_t headerData = new Data_t(CryptoUtils.serialize(newFileList));
        	Sig_t signature = new Sig_t(CryptoUtils.sign(headerData.getValue(), getPrivateKey()));
        	
        	
        	
        	
        	
			
			
			//uploads header first to check signature
			if (!getClientID().equals(server.put_k(headerData, signature, getPublicKey()))) {
                throw new Exception("Client's ID does not match main block ID!");
            }
        	
        	
        	//uploads contents
        	if (originalFileList.isEmpty()){
        		System.out.println("Original it's empty");
        		for (int i = 0; i < newFileList.size() ; i++)
        		{
        			System.out.println("new block! (" + i + ")");
        			System.out.println(server.put_h(new Data_t(filesArray[i])).getValue());
        		}
        	}else{
        		boolean addBlockFlag = true;
	        	for (int i = 0; i < newFileList.size() ; i++) {
	        		addBlockFlag = true;
	        		System.out.println("\nNEW[" + i + "]:" + newFileList.get(i).getValue());
	        		
	        		for(int j = 0; j < originalFileList.size(); j++){
	        			System.out.println("OLD[" + j + "]:" + originalFileList.get(j).getValue());
	        			if (originalFileList.get(j).equals(newFileList.get(i))){
	        				addBlockFlag = false;
	        				break;
	        			}				
	        		}
	        		
	        		if (addBlockFlag){
	        			System.out.println("new block!");
	            		server.put_h(new Data_t(filesArray[i]));
	        		}
	        		
	        		
	        	}
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
//            buffer.setValue(CryptoUtils.serialize(new byte[9999]));
            buffer.setValue(CryptoUtils.serialize("The quick brown fox jumps over the lazy dog"));
            c.fs_write(50, buffer.getValue().length, buffer);
            buffer.setValue(CryptoUtils.serialize("The quick brown fox jumps over the lazy do"));
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
