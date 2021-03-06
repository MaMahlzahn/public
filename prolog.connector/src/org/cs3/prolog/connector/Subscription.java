/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others)
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.prolog.connector;

import java.util.List;

import org.cs3.prolog.pif.PrologInterface;

/**
 * Subscription for a PrologInterface instance.
 * 
 * Subscriptions can best be thought of as describing a particular use some
 * client makes of a given PrologInterface instance. The pdt.core for example
 * makes use of Subscriptions to describe the relation of a IPrologProject to
 * the PrologInterface instances it uses. On the one hand, a PrologInterface is
 * used as the default runtime for the user application developed in the
 * project. On the other hand, the core itself internally needs a
 * PrologInterface to store metadata on the prolog source code and other
 * information associated to the project. So there are two Subscriptions
 * associated with each Prolog project.
 * 
 * Note that several subscriptions may be to one and the same PrologInterface.
 * In the above example, this is configurable by the user. On the other hand the
 * relation between projects and Subscriptions is hard coded. This does make sense: every 
 * prolog project makes use of prolog in exactly the ways described above - there is no need to
 * configure anything. 
 * 
 * @author lukas
 * 
 */
public interface Subscription {
	/**
	 * @return a unique identifier for this subscription, or null for anonymous
	 *         subscriptions.
	 */
	public abstract String getId();

	/**
	 * @return a key identifying the PrologInterface instance to subscribe to.
	 *         May NOT be null.
	 */
	public abstract String getPifKey();

	/**
	 * @return a short human readable text describing what the subscription is
	 *         used for. E.g. "Used to store metadata about prolog code found in
	 *         project FooBar." Maybe null.
	 */
	public abstract String getDescritpion();

	/**
	 * @return The name of this Subscription. Will be used as label string in
	 *         the UI. maybe null. This should be something short and catchy
	 *         like "FooBar - metadata".
	 */
	public abstract String getName();

	/**
	 * @return true of the subscription should be visible by the user.
	 *         false otherwise.
	 */
	public abstract boolean isVisible();

	
	/**
	 * "configure-your-pif-here"-hook. Called by the runtime at the earliest
	 * possible point in time that does satisfy both of the following
	 * conditions:
	 *  - the pif has been instantiated. 
	 *  - this subscription has been registered
	 * with the PrologInterfaceRegistry.
	 * 
	 * Note that this method is never called for anonymous subscriptions. This
	 * is implied by the second condition.
	 * 
	 * Implementation should make no assumptions on the life cycle state of the pif
	 * argument. It should also not contain calls that would alter the
	 * state. Note that a call to PrologInterface.getSession() DOES alter the state of the 
	 * pif (it may start it, if it is not already up!).
	 * 
	 * Why not? 
	 * - We do not want the pif to start up before it is actual needed.
	 * - We do not want to care about possible PrologInterfaceExceptions during configuration.
	 * 
	 * A commonly faced problem is the fact that when subscribing to a pif, you do not know 
	 * whether it has already been started or even created. E.g. if you add startup hooks 
	 * from within the configure call back, you do not know if they will be executed within the 
	 * same life cycle period. A solution that seems convenient at first glance is to check 
	 * the life cycle state and, if the pif is already up, just call the hook methods "manually".
	 * 
	 * The problem however is, that any exceptions thrown by the hook code cannot be correctly 
	 * propagated. You either have to catch them in the configure method, which only makes sense 
	 * if can locally recover from them (unlikely in the case of PrologInterfaceExceptions!),
	 * or you have to throw a RuntimeException, which is rather impolite because it leaves the
	 * upper tiers little chance to handle the problem gracefully.
	 * 
	 * A better approach is to leave these "late" initialization to the code that actually
	 * requested to add the subscription, because it is typically part of the operation, whose 
	 * context is finally violated by the thrown exceptions, and it is also more likely to have
	 * access to enough context to adequately handle the situation.  
	 * 
	 * @param pif
	 */
	public abstract void configure(PrologInterface pif) ;

	/**
	 * "clean-up-your-mess"-hook.
	 * Called by the runtime when the subscription is removed from the registry, or
	 * when a pif to which the client subscribed is removed from the registry
	 * 
	 * not called on anonymous subscriptions (see above)
	 * 
	 * 
	 * Implementation should make no assumptions on the state of the pif
	 * argument: It may be up and running. It may be not. Best is to check the
	 * state. If the pif is down, there is typically not much to clean up anyway.
	 * @param pif
	 */
	public abstract void deconfigure(PrologInterface pif);
	
	/**
	 * Return the tags associated with this Subscriptions.
	 * 
	 * Currently, the main use of tags is to associate hooks to the subscription that 
	 * were defined in the plugin.xml file.^
	 * 
	 * See getData() on how to parametrize the hooks associated with this subscription.
	 */
	public abstract String[] getTags();
	
	/**
	 * return the user data associated with this subscription.
	 * 
	 * The returned object will be used to parameterize hooks associated with this subscription.
	 * @return
	 */
	public abstract Object getData();

	/**
	 * Bootstrap library contribution (key) demanded by this subscription.
	 * Several plug-ins/fragments may contribute to a contribution key. 
	 * 
	 * In case the list is empty the default ("") contribution key is assumed, otherwise 
	 * only the given keys are processed. Meaning the default key ("") must be added in 
	 * case its corresponding bootstrap contributions should be added.
	 *    
	 * @return
	 */
	public abstract List<String> getBootstrapConstributionKeys();
}


