/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Tobias Rho, Lukas Degener, Andreas Becker, Fabian Noth
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.pdt.connector.registry;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.cs3.pdt.connector.subscription.Subscription;
import org.cs3.prolog.pif.PrologInterface;

abstract public class DefaultPrologInterfaceRegistry implements PrologInterfaceRegistry {

	private HashMap<String, PrologInterface> pifs = new HashMap<String, PrologInterface>();
	private HashMap<String, Subscription> subscriptions = new HashMap<String, Subscription>();
	private HashMap<String, HashSet<Subscription>> subscriptionLists = new HashMap<String, HashSet<Subscription>>();
	private Vector<PrologInterfaceRegistryListener> listeners = new Vector<PrologInterfaceRegistryListener>();
	private HashMap<PrologInterface, String> pifKeys = new HashMap<PrologInterface, String>();

	@Override
	public void addPrologInterfaceRegistryListener(
			PrologInterfaceRegistryListener l) {
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}

	@Override
	public void removePrologInterfaceRegistryListener(
			PrologInterfaceRegistryListener l) {
		synchronized (listeners) {
			if (listeners.contains(l)) {
				listeners.remove(l);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void firePrologInterfaceAdded(String key) {
		PrologInterfaceRegistryEvent e = new PrologInterfaceRegistryEvent(this,
				key);
		Vector<PrologInterfaceRegistryListener> clone = null;
		synchronized (listeners) {
			clone = (Vector<PrologInterfaceRegistryListener>) listeners.clone();
		}
		for (PrologInterfaceRegistryListener l : clone) {				
			l.prologInterfaceAdded(e);
		}
	}


	@SuppressWarnings("unchecked")
	public void firePrologInterfaceRemoved(String key) {
		PrologInterfaceRegistryEvent e = new PrologInterfaceRegistryEvent(this,
				key);
		Vector<PrologInterfaceRegistryListener> clone = null;
		synchronized (listeners) {
			clone = (Vector<PrologInterfaceRegistryListener>) listeners.clone();
		}
		for (PrologInterfaceRegistryListener l : clone) {
			l.prologInterfaceRemoved(e);
		}
	}


	@SuppressWarnings("unchecked")
	public void fireSubscriptionAdded(Subscription s) {
		PrologInterfaceRegistryEvent e = new PrologInterfaceRegistryEvent(this,
				s);
		Vector<PrologInterfaceRegistryListener> clone = null;
		synchronized (listeners) {
			clone = (Vector<PrologInterfaceRegistryListener>) listeners.clone();
		}
		for (PrologInterfaceRegistryListener l : clone) {
			l.subscriptionAdded(e);
		}
	}


	@SuppressWarnings("unchecked")
	public void fireSubscriptionRemoved(Subscription s) {
		PrologInterfaceRegistryEvent e = new PrologInterfaceRegistryEvent(this,
				s);
		Vector<PrologInterfaceRegistryListener> clone = null;
		synchronized (listeners) {
			clone = (Vector<PrologInterfaceRegistryListener>) listeners.clone();
		}
		for (PrologInterfaceRegistryListener l : clone) {
			l.subscriptionRemoved(e);
		}
	}

	@Override
	public Set<String> getRegisteredKeys() {

		return pifs.keySet();
	}

	@Override
	public String getKey(PrologInterface prologInterface) {
		return pifKeys.get(prologInterface);
	}

	@Override
	public PrologInterface getPrologInterface(String key) {
		return pifs.get(key);
	}

	@Override
	public Subscription getSubscription(String key) {
		return subscriptions.get(key);

	}

	@Override
	public Set<String> getAllKeys() {
		Set<String> s = new HashSet<String>(getRegisteredKeys());
		s.addAll(subscriptionLists.keySet());
		return s;
	}

	@Override
	public Set<String> getAllSubscriptionIDs() {
		return new HashSet<String>(subscriptions.keySet());
	}

	public Set<Subscription> getAllSubscriptions() {
		return new HashSet<Subscription>(subscriptions.values());
	}
	
	@Override
	public Set<Subscription> getSubscriptionsForPif(String key) {
		Collection<Subscription> coll = subscriptionLists.get(key);
		HashSet<Subscription> subscripitions = new HashSet<Subscription>();
		if(coll != null){
			subscripitions.addAll(coll);
		}
		return subscripitions;
	}

	@Override
	public void addPrologInterface(String key, PrologInterface pif) {
		Object old = pifs.get(key);
		if (old == pif) {
			return;
		}
		if (old != null) {
			removePrologInterface(key);
		}
		pifs.put(key, pif);
		pifKeys.put(pif, key);
		Set<Subscription> l = getSubscriptionsForPif(key);
		for (Subscription s: l) {
			s.configure(pif);
		}
		firePrologInterfaceAdded(key);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void removePrologInterface(String key) {
		PrologInterface pif = pifs.get(key);
		if (pif == null) {
			return;
		}
		HashSet<Subscription> keySet =  subscriptionLists.get(key);
		if (keySet != null) {
			keySet =  (HashSet<Subscription>) keySet.clone();
			for (Subscription s : keySet) {
				s.deconfigure(pif);
			}
		}
		firePrologInterfaceRemoved(key);
		pifKeys.remove(pif);
		pifs.remove(key);

	}

	@Override
	public void addSubscription(Subscription s) {
		// do not add anonymous subscriptions
		String sid = s.getId();
		if (sid == null) {
			return;
		}

		Object old = subscriptions.get(sid);
		if (old == s) {
			return;
		}
		if (old != null) {
			removeSubscription(sid);
		}
		HashSet<Subscription> l = subscriptionLists.get(s.getPifKey());
		if (l == null) {
			l = new HashSet<Subscription>();
			subscriptionLists.put(s.getPifKey(), l);
		}
		l.add(s);
		subscriptions.put(sid, s);

		if (this.pifs.containsKey(s.getPifKey())) {
			s.configure(getPrologInterface(s.getPifKey()));
		}
		fireSubscriptionAdded(s);
	}

	@Override
	public void removeSubscription(String id) {
		removeSubscription(getSubscription(id));
	}

	@Override
	public void removeSubscription(Subscription subscription) {
		// do not remove anonymous subscriptions
		if (subscription == null) {
			return;
		}
		String id = subscription.getId();
		if (id == null) {
			return;
		}
		if (!subscriptions.containsKey(id)) {
			return;
		}
		String pifKey = subscription.getPifKey();
		if (pifs.containsKey(pifKey)) {
			subscription.deconfigure(getPrologInterface(pifKey));
			Set<Subscription> otherSubscriptions = getSubscriptionsForPif(pifKey);
			otherSubscriptions.remove(subscription);
		}
		subscriptions.remove(id);

		Set<Subscription> keySet = subscriptionLists.get(pifKey);
		if (keySet == null) {
			return;
		}
		if (keySet.contains(subscription)) {
			keySet.remove(subscription);
			fireSubscriptionRemoved(subscription);

		}

	}
}


