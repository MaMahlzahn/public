/*
 */
package org.cs3.jtransformer.internal.preferences;

import java.io.IOException;

import org.cs3.jtransformer.JTDebug;
import org.cs3.jtransformer.JTransformerPlugin;
import org.cs3.pl.common.Option;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * this class tries to guess sensible defaults for most property values.
 */
public class Initializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        try {
            initializeDefaultPreferences_impl();
        } catch (Throwable t) {
            JTDebug.report(t);
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    
    private void initializeDefaultPreferences_impl()
            throws BackingStoreException, IOException, InterruptedException {
        JTransformerPlugin plugin =JTransformerPlugin.getDefault();

        String qualifier = plugin.getBundle().getSymbolicName();

        IScopeContext context = new DefaultScope();

        IEclipsePreferences node = context.getNode(qualifier);
        if (node == null) {
            JTDebug.error("H�h?!");
        } else {
            Option[] options = plugin.getOptions();
            for (int i = 0; i < options.length; i++) {
                String id = options[i].getId();
                String def = System.getProperty(id, options[i].getDefault());
                node.put(id, def);
            }
            
            String[] strings = node.keys();
            for (int i = 0; i < strings.length; i++) {
                JTDebug.info(strings[i] + " --> " + node.get(strings[i], "n.a."));
            }
        }
        
    }
}