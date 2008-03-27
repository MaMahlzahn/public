package org.cs3.pl.prolog.internal.lifecycle;

import java.util.HashMap;
import java.util.HashSet;

import org.cs3.pl.prolog.LifeCycleHook;
import org.cs3.pl.prolog.LifeCycleHook3;
import org.cs3.pl.prolog.PrologInterfaceException;

public class InitState extends AbstractState {

	protected InitState(LifeCycle context) {
		super(context);

	}

	@Override
	public void enter() {
		HashSet<LifeCycleHookWrapper> done = new HashSet<LifeCycleHookWrapper>();
		HashMap<String, LifeCycleHookWrapper> hooks = context.getHooks();				
		context.enqueueWork(new NamedWorkRunnable("startServer"){
			@Override
			public void run() throws PrologInterfaceException {
				try {
					context.startServer();
				} catch (Throwable e) {
					throw new PrologInterfaceException(e);
				}
			}	
		});
		
		for (LifeCycleHookWrapper h : hooks.values()) {
			h.onInit(done);
		}
		
		context.enqueueWork(new NamedWorkRunnable("workDoneInit") {
			@Override
			public void run() throws PrologInterfaceException {
				context.workDone();

			}
		});
		
	}

	@Override
	public State workDone() {
		return new AfterInitState(context);
	}

	@Override
	public State addLifeCycleHook(final LifeCycleHook hook, String id,
			String[] dependencies) {
		if (hook instanceof LifeCycleHook3 && isNewHook(hook,id)) {
			context.enqueueWork(new NamedWorkRunnable("lateInit on"+id) {
				public void run() throws PrologInterfaceException {
					((LifeCycleHook3)hook).lateInit(context.getPrologInterface());
				}
			});
		}
		return super.addLifeCycleHook(hook, id, dependencies);
	}

	private boolean isNewHook(LifeCycleHook hook,String id) {
		LifeCycleHookWrapper w= context.getHooks().get(id);
		return w==null || ! w.hooks.contains(hook);		
	}
}