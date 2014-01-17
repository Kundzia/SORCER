package sorcer.projekt.provider;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;
import sorcer.service.ContextException;

@SuppressWarnings("rawtypes")
public interface Random5 extends Remote{
	
	public Context random(Context context) throws RemoteException, ContextException;

}
