package sec.filesystem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import types.*;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

    // Basic Block Structure for Testing 
    private Block block;
    private void storeBlock(Block b) {
        block = b;
    }

    public ImplementationBlockServer() throws RemoteException {
    }

    @Override
    public Data_t get(Id_t id) throws RemoteException {
        return block.getData();
    }

    //Temporary "put_k" method without Signature or PubKey support 
    @Override
    public Id_t put_k(Data_t data) throws RemoteException {
        storeBlock(new Block(data));
        return block.getID();
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
