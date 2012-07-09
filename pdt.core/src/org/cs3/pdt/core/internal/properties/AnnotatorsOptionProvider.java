package org.cs3.pdt.core.internal.properties;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cs3.pdt.core.IPrologProject;
import org.cs3.pl.common.Option;
import org.cs3.pl.common.OptionProvider;
import org.cs3.pl.common.OptionProviderEvent;
import org.cs3.pl.common.OptionProviderExtension;
import org.cs3.pl.common.OptionProviderListener;
import org.cs3.pl.common.SimpleOption;
import org.cs3.pl.common.logging.Debug;
import org.cs3.pl.prolog.AsyncPrologSession;
import org.cs3.pl.prolog.DefaultAsyncPrologSessionListener;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologInterfaceEvent;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologInterfaceListener;
import org.cs3.pl.prolog.PrologSession;

public class AnnotatorsOptionProvider implements OptionProvider,OptionProviderExtension,PrologInterfaceListener {
	public static final String SUBJECT = "annotator_enabled";
	IPrologProject prologProject = null;
	private Option[] options;
	private Vector<OptionProviderListener> listeners = new Vector<OptionProviderListener>();
	private boolean itsMe;
	
	public  AnnotatorsOptionProvider(IPrologProject plProject)  throws PrologInterfaceException {
		this.prologProject=plProject;
		PrologInterface pif = prologProject.getMetadataPrologInterface();
		
		PrologSession session = pif.getSession(PrologInterface.NONE);
		List<Map<String,Object>> l = session.queryAll("pdt_annotator_enabled(Annotator,Enabled)");
		options = new Option[l.size()];
		
		int i=0;
		Vector<String> ids = new Vector<String>();
		Vector<String> values = new Vector<String>();
		Iterator<Map<String,Object>> iter = l.iterator();
		while(iter.hasNext()) {
			Map<String,Object> map = iter.next();
			String annotator = map.get("Annotator").toString();
			String enabled = map.get("Enabled").toString();
			options[i++] = new SimpleOption(annotator,annotator,"no description",Option.FLAG,"true");
			
			String persisted = prologProject.getPreferenceValue("enabled."+annotator, enabled);
			if(!persisted.equals(enabled)){
				ids.add(annotator);
				values.add(persisted);
			}
		}
		updatePrologBackend(
				ids.toArray(new String[ids.size()]), 
				values.toArray(new String[values.size()]));
	}
	
	@Override
	public void addOptionProviderListener(OptionProviderListener l) {
		synchronized (listeners) {
			if(!listeners.contains(l)){
				listeners.add(l);
			}	
		}
	}

	@Override
	public void removeOptionProviderListener(OptionProviderListener l) {
		if(listeners.contains(l)){
			listeners.remove(l);
		}
	}

	@Override
	public synchronized void setPreferenceValues(String[] ids, String[] values) {
		for (int i = 0; i < values.length; i++) {
			String value= values[i];
			String id = ids[i];
			prologProject.setPreferenceValue("enabled."+id, value);
		}
		
		updatePrologBackend(ids,values);
		fireValuesChanged(ids);
	}

	private void updatePrologBackend(String[] ids, String[] values) {
		itsMe=true;
		try {
			AsyncPrologSession s = (prologProject.getMetadataPrologInterface()).getAsyncSession(PrologInterface.NONE);
			s.addBatchListener(new DefaultAsyncPrologSessionListener());
			for (int i = 0; i < values.length; i++) {
				s.queryOnce("setup annotators", "pdt_set_annotator_enabled("+ids[i]+", "+values[i]+")");
			}
			s.join();
			s.dispose();
		} catch (PrologInterfaceException e) {
			Debug.report(e);			
		}
		itsMe=false;
	}

	private void fireValuesChanged(String[] ids) {
		OptionProviderEvent e = new OptionProviderEvent(this,ids);
		Vector<OptionProviderListener> clone = new Vector<OptionProviderListener>();
		synchronized (listeners) {
			clone.addAll(listeners);
		}
		for (Iterator<OptionProviderListener> it = clone.iterator(); it.hasNext();) {
			OptionProviderListener l = it.next();
			l.valuesChanged(e);
		}
	}

	@Override
	public Option[] getOptions() {
		return options;
	}

	@Override
	public synchronized String getPreferenceValue(String id, String string) {		
		return prologProject.getPreferenceValue("enabled."+id, getDefault(id));
	}

	private String getDefault(String id) {
		for (int i = 0; i < options.length; i++) {
			Option option = options[i];
			if(option.getId().equals(id)){
				return option.getDefault();
			}
		}
		return null;
	}

	@Override
	public void reconfigure() {
		;
	}

	@Override
	public void setPreferenceValue(String id, String value) {
		setPreferenceValues(new String[]{id}, new String[]{value});	
	}

	@Override
	public synchronized void update(PrologInterfaceEvent e) {
		if(itsMe){
			return;
		}
		if(!SUBJECT.equals(e.getSubject())){
			return;
		}
		String[] eventdata=e.getEvent().split("-");
		setPreferenceValue(eventdata[0], eventdata[1]);
	}
}
