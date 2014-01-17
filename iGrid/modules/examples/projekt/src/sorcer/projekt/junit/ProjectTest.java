package sorcer.projekt.junit;

import static sorcer.eo.operator.context;
import static sorcer.eo.operator.cxt;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.srv;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.SorcerConstants;
import sorcer.core.exertion.ObjectJob;
import sorcer.projekt.provider.Max;
import sorcer.projekt.provider.Power;
import sorcer.projekt.provider.Random5;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Task;
import sorcer.util.Sorcer;

@SuppressWarnings("unchecked")
public class ProjectTest implements SorcerConstants {

	private final static Logger logger = Logger.getLogger(ProjectTest.class.getName());
	
	static {
		ServiceExertion.debug = true;
		System.setProperty("java.security.policy", Sorcer.getHome()
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBase(new String[] { "Random5Provider.jar", "MaxProvider.jar", "PowerProvider.jar"  });
		System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
		System.setProperty("java.protocol.handler.pkgs", "sorcer.util.url|org.rioproject.url");
	}
	
	@Test
	public void proj() throws Exception {
		
		double minimum = 1;
		double maximum = 6;
		
		double toPower = 3;
		
		Task power = srv("power", sig("pow", Power.class),
				cxt("pow", in("first"), in("second", toPower)));
		
		Task random = task("random", sig("random", Random5.class),
				context("random", in("min", minimum), in("max", maximum)));
		
		Task max = srv("max", sig("max", Max.class),
				context("max", in("num1"), in("num2"), in("num3"), in("num4"), in("num5")));

		Exertion job = new ObjectJob("Job");
		job.addExertion(random);
		job.addExertion(max);
		job.addExertion(power);
		
		random.getContext().connect("random1", "num1", max.getContext());
		random.getContext().connect("random2", "num2", max.getContext());
		random.getContext().connect("random3", "num3", max.getContext());
		random.getContext().connect("random4", "num4", max.getContext());
		random.getContext().connect("random5", "num5", max.getContext());
		
		max.getContext().connect("max", "first", power.getContext());
		
		job = job.exert();

		logger.info("job context: " + ((Job)job).getJobContext());
	}
	
	@Test
	public void random() throws Exception {
		
		double min = 0;
		double max = 7;
		
		Task random = task("random", sig("random", Random5.class),
		context("random", in("min", min), in("max", max)));
		
		random = exert(random);

		logger.info("Val1: " + value(context(random), "random1"));
		logger.info("Val2: " + value(context(random), "random2"));
		logger.info("Val3: " + value(context(random), "random3"));
		logger.info("Val4: " + value(context(random), "random4"));
		logger.info("Val5: " + value(context(random), "random5"));
	}
	
	@Test
	public void max() throws Exception {
		
		double num1 = 0;
		double num2 = 1;
		double num3 = 5;
		double num4 = 3;
		double num5 = 4;
		
		Task random = task("max", sig("max", Max.class),
		context("max", in("num1", num1), in("num2", num2), in("num3", num3), in("num4", num4), in("num5", num5)));
		
		random = exert(random);

		logger.info("Max: " + value(context(random), "max"));
	}
	
	@Test
	public void power() throws Exception {
		
		double first = 2;
		double second = 3;
		
		Task power = srv("power", sig("pow", Power.class),
				cxt("pow", in("first", first), in("second", second)));
		
		power = exert(power);

		logger.info("Max: " + value(context(power), "value"));
	}
}
