package sec.filesystem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import types.Data_t;
import types.Id_t;
import types.Pk_t;
import types.Sig_t;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

    public ImplementationBlockServer() throws RemoteException {
    }

    @Override
    public Data_t get(Id_t id) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
