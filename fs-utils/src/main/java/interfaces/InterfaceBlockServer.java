package interfaces;

import types.Data_t;
import types.Id_t;
import types.Pk_t;
import types.Sig_t;

import java.rmi.Remote;
import java.security.cert.Certificate;
import java.util.List;

public interface InterfaceBlockServer extends Remote {

    int BLOCK_MAX_SIZE = 4 * 1024; //4kB
    int REPLICAS = 3; //Number of Server Replicas
    int PORT = 1099;

    //Temporary greeting method for testing
    String greeting() throws Exception;

    List readPubKeys() throws Exception;

    boolean storePubKey(Pk_t key) throws Exception;

    boolean storePubKey(Certificate cert) throws Exception;

    Data_t get(Id_t id) throws Exception;

    Id_t getID(Pk_t pk) throws Exception;

    Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws Exception;

    Id_t put_h(Data_t data) throws Exception;

}
