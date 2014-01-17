package sorcer.projekt.provider;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;
import sorcer.service.ContextException;

/**
 * @author Macha
 *
 */
@SuppressWarnings("rawtypes")
public interface AdderService extends Remote {

public Context add(Context context) throws RemoteException, ContextException;

}
