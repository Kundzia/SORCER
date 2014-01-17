package sorcer.projekt.provider;

import static sorcer.eo.operator.revalue;

import java.rmi.RemoteException;
import java.util.List;

import com.sun.jini.start.LifeCycle;

import sorcer.core.context.Contexts;
import sorcer.core.context.PositionalContext;
import sorcer.core.provider.ServiceTasker;
import sorcer.service.Context;
import sorcer.service.ContextException;


@SuppressWarnings("rawtypes")
public class PowerProvider extends ServiceTasker implements Power  {

	public static final String VALUE = "value";

	public PowerProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}
	
	
	@SuppressWarnings("unchecked")
	public Context pow(Context context) throws RemoteException, ContextException {

		PositionalContext cxt = (PositionalContext) context;
		try {

			List<Double> inputs = (List<Double>)Contexts.getNamedInValues(context);
			if (inputs == null || inputs.size() == 0) {
				inputs = (List<Double>)Contexts.getPrefixedInValues(context);
			}
			if (inputs == null || inputs.size() == 0) inputs = (List<Double>)cxt.getInValues();
		
			double first = (Double)revalue(inputs.get(1));
			double second = (Double)revalue(inputs.get(0));
			
			double value = Math.pow(first, second);
			
			cxt.putValue(VALUE, value);
				
		} catch (Exception ex) {
			ex.printStackTrace();
			context.reportException(ex);
			throw new ContextException(" calculate exception", ex);
		}
		return (Context) context;
	}

}
