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
public class Random5Provider extends ServiceTasker implements Random5  {

	public static final String ARRAY = "array";

	public Random5Provider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}
	
	
	@SuppressWarnings("unchecked")
	public Context random(Context context) throws RemoteException, ContextException {

		PositionalContext cxt = (PositionalContext) context;
		try {

			List<Double> inputs = (List<Double>)Contexts.getNamedInValues(context);
			if (inputs == null || inputs.size() == 0) {
				inputs = (List<Double>)Contexts.getPrefixedInValues(context);
			}
			if (inputs == null || inputs.size() == 0) inputs = (List<Double>)cxt.getInValues();
		
			double min = (Double)revalue(inputs.get(0));
			double max = (Double)revalue(inputs.get(1));
			
			double random = 0;
			double[] array = new double[5];
			
			for(int i = 0; i<5; i++){
				random = min + (Double)(Math.random() * ((max - min) + 1));
				random = (int)random;
				array[i] = random;
			}
			
			cxt.putValue("random1", array[0]);
			cxt.putValue("random2", array[1]);
			cxt.putValue("random3", array[2]);
			cxt.putValue("random4", array[3]);
			cxt.putValue("random5", array[4]);
			
				
		} catch (Exception ex) {
			ex.printStackTrace();
			context.reportException(ex);
			throw new ContextException(" calculate exception", ex);
		}
		return (Context) context;
	}

}
