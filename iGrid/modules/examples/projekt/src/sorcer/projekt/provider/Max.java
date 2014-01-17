package sorcer.projekt.provider;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;
import sorcer.service.ContextException;


@SuppressWarnings("rawtypes")
public interface Max extends Remote{

	public Context max(Context context) throws RemoteException, ContextException;

}