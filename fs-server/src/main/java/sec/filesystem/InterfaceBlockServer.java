package sec.filesystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import types.*;

public interface InterfaceBlockServer extends Remote {
	
	public Data_t get(Id_t id) throws RemoteException;
	
	public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws RemoteException;
	
	public Id_t put_h(Data_t data) throws RemoteException;
	
}
