/*****************************************************************************
 * This file is part of the JTransformer Framework
 * 
 * Author: Tobias Rho (among others) 
 * E-mail: rho@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/jtransformer
 * Copyright (C): 2004-2008, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/
package pdt.y.focusview;

import java.util.ArrayList;
import java.util.List;

import org.cs3.pdt.runtime.DefaultSubscription;
import org.cs3.pl.prolog.LifeCycleHook;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologSession;

import pdt.y.main.PluginActivator;


public class FocusViewSubscription extends DefaultSubscription implements
			LifeCycleHook {

		private static final String YWORKS_BOOTSTRAPCONTRIBUTION_KEY = "yworks.contribution.key";
		private static List<String> demandedBootstrapContributionsKeys = new ArrayList<String>();
		
		static {
			demandedBootstrapContributionsKeys.add(YWORKS_BOOTSTRAPCONTRIBUTION_KEY);
		}
		
		private FocusViewSubscription(String id, String pifID,
				String descritpion, String name) {
			super(id, pifID, descritpion, name, PluginActivator.PLUGIN_ID, true);
			setPersistent(false);

		}

		/**
		 * Use this method to instantiate a YWorksSubscription. 
		 * @param project
		 * @param pifID
		 * @return
		 */
		public static FocusViewSubscription newInstance(String pifID) {
			FocusViewSubscription subscription = new FocusViewSubscription(
					"FocusViewSubscription",
					pifID,
					"Subscription of FocusView", 
					"FocusView"
					);
			return subscription;
		}

		
		@Override
		public List<String> getBootstrapConstributionKeys() {
			return demandedBootstrapContributionsKeys;
		}

		@Override
		public void lateInit(PrologInterface pif) {
		}

		@Override
		public void onError(PrologInterface pif) {
		}

		@Override
		public void setData(Object data) {
		}

		@Override
		public void afterInit(PrologInterface pif)
				throws PrologInterfaceException {
		}

		@Override
		public void beforeShutdown(PrologInterface pif, PrologSession session)
				throws PrologInterfaceException {
		}

		@Override
		public void onInit(PrologInterface pif, PrologSession initSession)
				throws PrologInterfaceException {
		}
		
		@Override
		public boolean isVisible() {
			return false;
		}
	}