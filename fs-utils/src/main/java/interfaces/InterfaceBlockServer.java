package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.cert.Certificate;
import java.util.List;

import exceptions.RevokedCertificateException;
import types.*;

public interface InterfaceBlockServer extends Remote {
	
    public static final int BLOCK_MAX_SIZE = 4*1024; //4kB

    //Temporary greeting method for testing
    public String greeting() throws RemoteException;
    
    public List readPubKeys() throws RemoteException;
    
    public boolean storePubKey(Pk_t key) throws RemoteException;
    
    public boolean storePubKey(Certificate cert) throws RemoteException, RevokedCertificateException;
    
    public Data_t get(Id_t id) throws RemoteException;

    public Id_t getID(Pk_t pk) throws RemoteException;
    
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws Exception;

    public Id_t put_h(Data_t data) throws RemoteException;

}
