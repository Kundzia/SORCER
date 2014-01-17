/**
 * 
 */
package sorcer.projekt.junit;

import static sorcer.eo.operator.context;
import static sorcer.eo.operator.cxt;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.jobContext;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.pipe;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.srv;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.SorcerConstants;
import sorcer.core.provider.jobber.ServiceJobber;
import sorcer.projekt.provider.Adder;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Task;
import sorcer.util.Sorcer;

/**
 * @author Macha
 *
 */
@SuppressWarnings("unchecked")
public class AdderTest implements SorcerConstants {

	private final static Logger logger = Logger.getLogger(AdderTest.class.getName());
	
	static {
		ServiceExertion.debug = true;
		System.setProperty("java.security.policy", Sorcer.getHome()
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBase(new String[] { "AdderProvider.jar" });
		System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
		System.setProperty("java.protocol.handler.pkgs", "sorcer.util.url|org.rioproject.url");
	}
	
	public void adderTest() throws Exception {
		
		Task add = task("AdderTask", sig("add", Adder.class),
				context("add", in("a", 11.0), in("b", 8.0)));
		
		add = exert(add);

		logger.info("SUM: " +value(context(add), "sum"));
	}
	
	@Test
	public void projectTest() throws Exception {
		
		Task add3 = srv("add3", sig("add", Adder.class),
				cxt("add", in("a"), in("b"), out("sum")));
		
		Task add1 = task("add1", sig("add", Adder.class),
				context("add", in("a1", 2.0), in("b1", 2.0)));
		
		Task add2 = task("add2", sig("add", Adder.class),
				context("add", in("a2", 3.0), in("b2", 3.0)));
		
		Job job = srv("whole",
				sig("execute", ServiceJobber.class),
				srv("j2", sig("execute", ServiceJobber.class), add1, add2), add3,
				pipe(out(add1, "sum"), in(add3, "a")),
				pipe(out(add2, "sum"), in(add3, "b")));

		job = exert(job);
		logger.info("Job context: " + jobContext(job));
	}
	
}
