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
/**
 * @author Macha
 *
 */
@SuppressWarnings("rawtypes")
public class AdderProvider extends ServiceTasker implements Adder  {

	public static final String SUM = "sum";

	public AdderProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}
	
	
	@SuppressWarnings("unchecked")
	public Context add(Context context) throws RemoteException, ContextException {

		PositionalContext cxt = (PositionalContext) context;
		try {

			List<Double> inputs = (List<Double>)Contexts.getNamedInValues(context);
			if (inputs == null || inputs.size() == 0) {
				inputs = (List<Double>)Contexts.getPrefixedInValues(context);
			}
			if (inputs == null || inputs.size() == 0) inputs = (List<Double>)cxt.getInValues();
		
			double result = (Double)revalue(inputs.get(0));
			for (int i = 1; i < inputs.size(); i++)
				result *= (Double)revalue(inputs.get(i));

				cxt.putValue(SUM, result);
				
		} catch (Exception ex) {
			ex.printStackTrace();
			context.reportException(ex);
			throw new ContextException(" calculate exception", ex);
		}
		return (Context) context;
	}

}
