/* $LICENSE_MSG$(ld) */

package org.cs3.prolog.connector;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import org.cs3.prolog.pif.PrologInterface;
import org.cs3.prolog.pif.PrologInterfaceException;

/**
 * Central registry for managing PrologInterface instances and Subscriptions.
 * 
 * The registry keeps track of every PrologInterface created by the pdt.runtime
 * plugin. In addition, clients can register Subscriptions to particular
 * PrologInterface instances, thereby publically announcing what they intend to use them for.
 * 
 * This registry is ment to be a kind of "forum" for clients that need to share one and the same PrologInterface instance.
 * It is also intended to provide a model for ui components that need to provide the user with a choice of available Prolog runtimes.
 * 
 * Note that adding a Subscription means subscribing to a particular PrologInterface _KEY_ rather than to the instance itself.
 * Among other things this allows clients to express ther instance in a particular PrologInterface before it actually exists.
 * This is important since PrologInterface instances typically get created in a lazy fashion, whereas the ui should be able 
 * to reflect subscriptions much earlier to help the user understand her environment better. 
 * 
 * @author lukas
 */
public interface PrologInterfaceRegistry {

	/**
	 * @return all keys to which PrologInterfaces are registered.
	 */
	public Set<String> getRegisteredKeys();

	/**
	 * @return all pif keys that are known to the registry, including keys for which
	 *         no pif is registered.
	 */
	public Set<String> getAllKeys();

	/**
	 * @return the IDs of all subscriptions registered with the registry..
	 */
	public Set<String> getAllSubscriptionIDs();

	/**
	 * 
	 * @return all subscriptions to a given pif key
	 */
	public Set<Subscription> getSubscriptionsForPif(String key);

	

	
	/**
	 * retrieve the registry key of a registered PrologInterface. *
	 */
	public String getKey(PrologInterface prologInterface);

	/**
	 * retrieve the PrologInterface instance registered for the given key.
	 */
	public PrologInterface getPrologInterface(String key);

	/**
	 * add a listener to this registry.
	 * 
	 * Listeners get notified whenever a PrologInterface instance or
	 * Subscription is registered or unregistered.
	 * 
	 * @param l
	 */
	public void addPrologInterfaceRegistryListener(
			PrologInterfaceRegistryListener l);

	/**
	 * remove a listener from this registry.
	 * 
	 * @param l
	 */
	public void removePrologInterfaceRegistryListener(
			PrologInterfaceRegistryListener l);

	
	/**
	 * Register a PrologInterface with this registry.
	 * 
	 * If another PrologInterface is already registered with this key,
	 * it is removed first.
	 * 
	 * If the same PrologInterace is already registered with this key,
	 * this method has no effect.
	 * 
	 * This method will cause a call to the method configure() on any waiting
	 * subscriptions that are already registered for the given pifkey.
	 * 
	 * For each distinct tuple ( Data, Hook descriptor) where
	 *  - Data is the return value of the getData() method called on a subscription 
	 *    that is registered for the given key
	 *  - Hook Descriptor is a registered hook descriptor that has at least one
	 *    tag in common with this subscription
	 * a hook will be created using named hook descriptor, it will be parameterized with 
	 * the user data and will be registered with the prolog interface instance.
	 * @param key
	 * @param pif
	 * @throws PrologInterfaceException 
	 */
	public void addPrologInterface(String key, PrologInterface pif) ;

	/**
	 * Remove a PrologInterface from this registry.
	 * 
	 * Removes the PrologInterface with the given key. If no PrologInterface was
	 * registered for that key, this method has no effect. Subscriptions will
	 * NOT be removed.
	 * 
	 * This method will cause a call to the method deconfigure() on any
	 * subscription registered for the given pif key.
	 * All hooks that were added by the registry are removed from the prolog interface.
	 * @param key
	 * 
	 */
	public void removePrologInterface(String key) throws PrologInterfaceException;

	/**
	 * Add a subscription to the registry.
	 * 
	 * If there is already a Subscription with the same key, it will be removed
	 * first.
	 * 
	 * If the same subscription is already registered, this method has no effect.
	 * 
	 * If there is already a PrologInterface instance registered for the
	 * subscriptions pifKey, this method will cause a call to the method
	 * configure() on the argument Subscription instance. 
	 * In addition, for each registered Hook Descriptor that has at least one
	 * tag in common with the subscription, said descriptor will be used to 
	 * create a hook, parameterize it with the return value of the method
	 * getData() called on the subscription and add register it with the pif. 
	 * 
	 * If the argument Subscription is an instance of PersistableSubscription,
	 * the registry will take the neccesary steps to save the subscription on
	 * workbench shutdown and restore it on the next startup.
	 * 
	 * @param s
	 * @throws PrologInterfaceException 
	 */
	public void addSubscription(Subscription s) ;

	/**
	 * Remove a subscription from the registry.
	 * 
	 * If there is currently a PrologInterface instance registered for the
	 * subscriptions pifKey, this method will cause a call to the method
	 * deconfigure() on the argument Subscription instance.
	 * In addition all hooks that were registered to this prolog interface 
	 * because of this subscription will be removed, unless they are still
	 * required because of some other registered subscription.
	 * 
	 * @param s
	 * @throws PrologInterfaceException 
	 */
	public void removeSubscription(Subscription s);

	/**
	 * Remove a subscription from the registry.
	 * 
	 * Removes the subscription with the given subscription id.
	 * 
	 * @see removeSubscription(Subscription)
	 * @param id
	 * @throws PrologInterfaceException 
	 */
	public void removeSubscription(String id);
	
	

	
	/**
	 * Find the Subscription for a given subscription id;
	 * 
	 * @param id
	 * @return the Subscription or null if none was registered with this id.
	 */
	public Subscription getSubscription(String id);

	/**
	 * Load a saved registry.
	 * @param r
	 */
	void load(Reader r) throws IOException;

	/**
	 * Save the current registry.
	 * @param w
	 */
	public void save(Writer w) throws IOException;
}

