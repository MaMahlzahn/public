package org.cs3.prolog.internal.lifecycle;

import java.util.HashSet;

import org.cs3.prolog.PrologInterfaceException;

public class ShutdownState extends AbstractState {

	public ShutdownState(LifeCycle context) {
		super(context);
	
	}

	
	
	@Override
	public void enter() {
		
		HashSet<LifeCycleHookWrapper> done = new HashSet<LifeCycleHookWrapper>();
		context.getPrologInterface();
		context.clearWorkQueue(); //there may be afterINit hooks left. dump them.
		
		for (LifeCycleHookWrapper w : context.getHooks().values()) {			
			w.beforeShutdown(done);
		}
		
		context.enqueueWork(new NamedWorkRunnable("shutdown_server") {
			
			@Override
			public void run() throws PrologInterfaceException {
				try {
					context.disposeSessions();
					context.stopServer();
					context.workDone();
				} catch (Throwable e) {
					throw new PrologInterfaceException(e);					
				}
				
			}
		});
	}

	

	
	@Override
	public State workDone() {	
		return new DownState(context); //reset when hooks are through.
	}

	

}
