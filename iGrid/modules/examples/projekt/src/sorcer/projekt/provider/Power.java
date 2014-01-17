package sorcer.projekt.provider;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;
import sorcer.service.ContextException;

@SuppressWarnings("rawtypes")
public interface Power extends Remote{

public Context pow(Context context) throws RemoteException, ContextException;

}