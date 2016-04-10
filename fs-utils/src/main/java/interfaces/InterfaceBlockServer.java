package interfaces;

import java.rmi.Remote;
import java.security.cert.Certificate;
import java.util.List;

import types.*;

public interface InterfaceBlockServer extends Remote {
	
    public static final int BLOCK_MAX_SIZE = 4*1024; //4kB
    public static final int REPLICAS = 3; //Number of Server Replicas
    
    //Temporary greeting method for testing
    public String greeting() throws Exception;
    
    public List readPubKeys() throws Exception;
    
    public boolean storePubKey(Pk_t key) throws Exception;
    
    public boolean storePubKey(Certificate cert) throws Exception;
    
    public Data_t get(Id_t id) throws Exception;

    public Id_t getID(Pk_t pk) throws Exception;
    
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws Exception;

    public Id_t put_h(Data_t data) throws Exception;

}
