package org.cs3.prolog.internal.lifecycle;

import java.util.HashMap;
import java.util.HashSet;

import org.cs3.prolog.LifeCycleHook;
import org.cs3.prolog.PrologInterfaceException;

public class AfterInitState extends AbstractState {

	protected AfterInitState(LifeCycle context) {
		super(context);
	}

	
	@Override
	public void enter() {
		HashSet<LifeCycleHookWrapper> done = new HashSet<LifeCycleHookWrapper>();
		HashMap<String, LifeCycleHookWrapper> hooks = context.getHooks();
		
		for (LifeCycleHookWrapper h : hooks.values()) {
			h.afterInit( done);
		}
		context.enqueueWork(new NamedWorkRunnable("workDoneAfterInit") {
			
			@Override
			public void run() throws PrologInterfaceException {
				context.workDone();

			}
		});

	}
	
	
	@Override
	public boolean isUp() {
		return true;
	}

	
	@Override
	public State workDone() {
		return new UpState(context);
	}

	
	@Override
	public State addLifeCycleHook(final LifeCycleHook hook, String id,
			String[] dependencies) {
		if (isNewHook(hook, id)) {
			context.enqueueWork(new NamedWorkRunnable("lateInit on "+id) {
				@Override
				public void run() throws PrologInterfaceException {
					hook.lateInit(context
							.getPrologInterface());
				}
			});
		}
		return super.addLifeCycleHook(hook, id, dependencies);
	}

	private boolean isNewHook(LifeCycleHook hook, String id) {
		LifeCycleHookWrapper w = context.getHooks().get(id);
		return w == null || !w.hooks.contains(hook);
	}

	
	@Override
	public State stop() {
		return new ShutdownState(context);
	}
}
