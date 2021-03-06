/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.invoker;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.co.tuple.Entry;
import sorcer.core.context.model.par.Par;
import sorcer.core.context.model.par.ParModel;
import sorcer.core.context.model.par.ParSet;
import sorcer.service.Arg;
import sorcer.service.ArgList;
import sorcer.service.ArgSet;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Evaluator;
import sorcer.service.Identifiable;
import sorcer.service.Invocation;
import sorcer.service.InvocationException;
import sorcer.service.Scopable;
import sorcer.vfe.Observable;
import sorcer.vfe.Observer;

/**
 * @author Mike Sobolewski
 */

/**
 * The Invoker defines context driven invocations on a parameter context
 * (invoke context) containing its parameter names (paths) and arguments
 * (values). The requested invocation is specified by the own invoke context and
 * eventual context of parameters (Par).
 * 
 * The semantics for how parameters can be declared and how the arguments get
 * passed to the parameters of callable unit are defined by the language, but
 * the details of how this is represented in any particular computing system
 * depend on the calling conventions of that system. A context-driven computing
 * system defines callable unit called invokers used within a scope of service
 * contexts, data structures defined in SORCER.
 * 
 * An invoke context is dictionary (associative array) composed of a collection
 * of (key, value) pairs, such that each possible key appears at most once in
 * the collection. Keys are considered as parameters and values as arguments of
 * the service invokers accepting service contexts as their input data. A key is
 * expressed by a path of attributes like directories in paths of a file system.
 * Paths define a namespace of the context parameters. A context argument is any
 * object referenced by its path or returned by a context invoker referenced by
 * its path inside the context. An ordered list of parameters is usually
 * included in the definition of an invoker, so that, each time the invoker is
 * called, the context arguments for that call can be assigned to the
 * corresponding parameters of the invoker. The context values for all paths
 * inside the context are defined explicitly by corresponding objects or
 * calculated by corresponding invokers. Thus, requesting a value for a path in
 * a context is a computation defined by a invoker composition within the scope
 * of the context.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Invoker<T> extends Observable implements Identifiable, Scopable, Evaluator<T>, Invocation<T>, Observer, Serializable {

	private static final long serialVersionUID = -2007501128660915681L;
	
	protected String name;
	
	protected String defaultName = "invoker-";
	
	// counter for unnamed instances
	protected static int count;
	
	protected Uuid id = UuidFactory.generate();

	//the cached value
	protected T value;
		
	// invocation delegate to
	Evaluator evaluator;

	// indication that value has been calculated with recent arguments
	protected boolean valueIsValid = false;
		
	protected ParModel invokeContext;

	// set of dependent variables for this evaluator
	protected ArgSet pars = new ArgSet();

	/** Logger for logging information about instances of this type */
	static final Logger logger = Logger.getLogger(Invoker.class
			.getName());

	public Invoker() {
		this.name = defaultName + count++;
		invokeContext = new ParModel("model/par");
	}
	
	public Invoker(String name) {
		if (name == null)
			this.name = defaultName + count++;
		else
			this.name = name;
	}
	
	public Invoker(ParModel context) {
		this(context.getName());
		invokeContext = context;
	}
	
	public Invoker(ParModel context, Evaluator evaluator, Par... pars) {
		this(context);
		this.evaluator = evaluator;
		this.pars = new ArgSet(pars);
	}
	
	public Invoker(ParModel context, Evaluator evaluator, ArgSet pars) {
		this(context);
		this.evaluator = evaluator;
		this.pars = pars;
	}
	
	public Invoker(Evaluator evaluator, ArgSet pars) {
		this(((Identifiable)evaluator).getName());
		this.evaluator = evaluator;
		this.pars = pars;
	}
	
	public Invoker(Evaluator evaluator, Par... pars) {
		this(((Identifiable)evaluator).getName());
		this.evaluator = evaluator;
		this.pars = new ArgSet(pars);
	}
	
	/**
	 * <p>
	 * Returns a set of parameters (pars) of this invoker that are a a subset of
	 * parameters of its invokeContext.
	 * </p>
	 * 
	 * @return the pars of this invoker
	 */
	public ArgSet getPars() {
		return pars;
	}

	/**
	 * <p>
	 * Assigns a set of parameters (pars) for this invoker. 
	 * </p>
	 * 
	 * @param pars
	 *            the pars to set
	 */
	public Invoker setPars(ArgSet pars) {
		this.pars = pars;
		return this;
	}

	public Invoker setPars(Arg[] pars) {
		this.pars = new ArgSet(pars);
		return this;
	}
	
	/**
	 * <p>
	 * Return the valid value
	 * </p>
	 * 
	 * @return the valid value
	 * @throws EvaluationException 
	 * @throws RemoteException 
	 */
	@Override
	public T getValue(Arg... entries) throws EvaluationException, RemoteException {
			if (evaluator != null)
				return (T) invokeEvaluator(entries);
			else
				throw new EvaluationException("Not implemented by: " + this.getClass().getName());
			
	}

	public void valueValid(boolean state) {
		valueIsValid = state;
	}
	
	public boolean valueValid() {
		return valueIsValid;
	}
	
	public void valueChanged() throws EvaluationException {
		setChanged();
		try {
			notifyObservers(this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new EvaluationException(e.toString());
		}
	}
	
	@Override 
	public void update(Observable observable, Object obj) throws EvaluationException, RemoteException {
		// one of my dependent pars changed
		// the 'observable' is the dependent invoker that has changed as indicated by 'obj'
		// ignore updates from itself
		valueValid(false);
		
		// set value to null so getValueAsIs returns null
		value = null;
		setChanged();
		notifyObservers(this);
	}
	
	/**
	 * Adds a new par to the invoker. This must be done before calling
	 * {@link #invoke} so the invoker is aware that the new par may be added to
	 * the model.
	 * 
	 * @param name
	 *            Name of the variable to be added
	 * @param value
	 *            Initial value or new value for the variable
	 * @throws RemoteException
	 * @throws EvaluationException
	 * @throws VarException
	 */
	public Invoker addPar(Object par) throws EvaluationException,
			RemoteException {
		if (par instanceof Par) {
			invokeContext.put(((Par)par).getName(), par);
			if (((Par)par).asis() instanceof Invoker) {
			((Invoker) ((Par)par).getValue()).addObserver(this);
			pars.add((Par)par);
			value = null;
			setChanged();
			notifyObservers(this);
			valueValid(false);
			}
		} else if (par instanceof Identifiable) {
			Par p = new Par(((Identifiable)par).getName(), par, invokeContext);
			try {
				invokeContext.putValue(p.getName(), p);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}
		return this;
	}

	synchronized public void addPars(ArgSet parSet) throws EvaluationException,
			RemoteException {
		for (Arg p : parSet) {
			addPar(p);
		}
	}
	
	synchronized public void addPars(List<Par> parList)
			throws EvaluationException, RemoteException {
		for (Par p : parList) {
			addPar(p);
		}
	}
	
	synchronized public void addPars(Par... pars) throws EvaluationException,
			RemoteException {
		for (Par p : pars) {
			addPar(p);
		}
	}

	synchronized public void addPars(ArgList args) throws EvaluationException,
			RemoteException {
		if (args != null)
			for (Arg p : args) {
				addPar(p);
			}
	}

	synchronized public void addPars(ArgList... parLists)
			throws EvaluationException, RemoteException {
		for (ArgList pl : parLists) {
			addPars(pl);
		}
	}
	
	public T invoke(Context context, Arg... entries)
			throws RemoteException, InvocationException {
		try {
			if (invokeContext == null)
				invokeContext = (ParModel) context;
			else {
				invokeContext.append(context);
			}
		} catch (ContextException e) {
			throw new InvocationException(e);
		}
		if (evaluator != null)
			return (T) invokeEvaluator();
		else
			return invoke(entries);
	}
	
	public T invoke(Arg... entries) throws RemoteException,
			InvocationException {
		try {
			if (entries != null && entries.length > 0) {
				valueIsValid = false;
				if (invokeContext == null)
					invokeContext = new ParModel("model/par");
					
				invokeContext.substitute(entries);
			}
			if (invokeContext.isContextChanged()) {
				valueIsValid = false;
				pars.clearArgs();
			}
			if (valueIsValid)
				return value;
			else {
				if (evaluator != null)
					value = (T) invokeEvaluator(entries);
				else
					value = getValue(entries);

				valueValid(true);
			}
		} catch (Exception e) {
			throw new InvocationException(e);
		}
		return value;
	}
	
	private Object invokeEvaluator(Arg... entries) throws InvocationException {
		init(pars);
		try {
			evaluator.addArgs(pars);
			return evaluator.getValue(entries);
		} catch (Exception e) {
			throw new InvocationException(e);
		}
	}
	
	private void init(ArgSet set){
		for (Arg p : set) {
			if (((Par)p).getScope() == null)
				((Par)p).setScope(invokeContext);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public T asis() throws EvaluationException, RemoteException {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#substitute(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Evaluation substitute(Arg... entries)
			throws InvocationException, RemoteException {
		for (Arg e : entries) {
			if (e instanceof Entry<?>) {
				try {
					invokeContext.putValue(((Entry<T>) e)._1,
							((Entry<T>) e)._2);
				} catch (ContextException ex) {
					throw new InvocationException(ex);
				}
			}

		}
		return this;
	}

	public Context getScope() {
		return invokeContext;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Scopable#setScope(java.lang.Object)
	 */
	@Override
	public void setScope(Object scope) throws RemoteException {
		this.invokeContext = (ParModel)scope;
		
	}
	
	/**
	 * <p>
	 * Returns this invoker's evaluator.
	 * </p>
	 * 
	 * @return the evaluator
	 */
	public Evaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * <p>
	 * Assigns an evaluator used by this invoker.
	 * </p>
	 * 
	 * @param evaluator
	 *            the evaluator to set
	 */
	public Invoker setEvaluator(Evaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	public void clearPars() throws EvaluationException {
		for (Arg p : pars) {
			((Par) p).setValue(null);

		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + name;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#getId()
	 */
	@Override
	public Object getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluator#evaluate(sorcer.service.Arg[])
	 */
	@Override
	public T evaluate(Arg... entries) throws EvaluationException,
			RemoteException {
		return invoke(entries);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluator#addArgs(sorcer.core.context.model.par.ParSet)
	 */
	@Override
	public void addArgs(ArgSet set) throws EvaluationException, RemoteException {
		addPars(set);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluator#getArgs()
	 */
	@Override
	public ArgSet getArgs() {
		return pars;
	}

}
