package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.*;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.*;

/**
 * This is used to store the MRU list of perspectives.
 */
public class PerspectiveHistory {

	final static private int DEFAULT_DEPTH = 7;
	private ArrayList shortcuts;
	private ArrayList sortedShortcuts;
	private IPerspectiveRegistry reg; 
	private ListenerList listeners = new ListenerList();

	private Comparator comparator = new Comparator() {
		private Collator collator = Collator.getInstance();
		
		public int compare(Object ob1, Object ob2) {
			IPerspectiveDescriptor d1 = (IPerspectiveDescriptor)ob1;
			IPerspectiveDescriptor d2 = (IPerspectiveDescriptor)ob2;
			return collator.compare(d1.getLabel(), d2.getLabel());
		}
	};
	
	public PerspectiveHistory(IPerspectiveRegistry reg) {
		shortcuts = new ArrayList(DEFAULT_DEPTH);
		sortedShortcuts = new ArrayList(DEFAULT_DEPTH);
		this.reg = reg;
	}

	public void addListener(IPropertyListener l) {
		listeners.add(l);
	}	
	
	public void removeListener(IPropertyListener l) {
		listeners.remove(l);
	}	
	
	public void fireChange() {
		Object [] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			IPropertyListener element = (IPropertyListener)array[i];
			element.propertyChanged(this, 0);
		}
	}
	
	public void restoreState(IMemento memento) {
		// Read the shortcuts.
		IMemento [] children = memento.getChildren("desc");
		for (int x = 0; x < children.length; x ++) {
			IMemento childMem = children[x];
			String id = childMem.getID();
			IPerspectiveDescriptor desc = reg.findPerspectiveWithId(id);
			if (desc != null) 
				shortcuts.add(desc);
		}
		
		// Create sorted shortcuts.
		sortedShortcuts = (ArrayList)shortcuts.clone();
		Collections.sort(sortedShortcuts, comparator);
	}
	
	public void saveState(IMemento memento) {
		// Save the shortcuts.
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor)iter.next();
			memento.createChild("desc", desc.getId());
		}
	}

	public void add(String id) {
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId(id);
		if (desc != null) 
			add(desc);
	}
	
	public void add(IPerspectiveDescriptor desc) {
		// If the new desc is already in the shortcut list, just return.
		if (sortedShortcuts.contains(desc))
			return;
			
		// Add desc to shortcut lists.
		shortcuts.add(0, desc); // insert at top as most recent

		// If the shortcut list is too long then remove the oldest ones.
		int size = shortcuts.size();
		int preferredSize = DEFAULT_DEPTH;
		while (size > preferredSize) {
			shortcuts.remove(size - 1);
			-- size;
		}

		updateSortedShortcuts();
		fireChange();
	}
	
	public void refreshFromRegistry() {
		boolean change = false;
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor)iter.next();
			if (reg.findPerspectiveWithId(desc.getId()) == null) {
				iter.remove();
				change = true;
			}
		}
		if (change) {
			updateSortedShortcuts();
			fireChange();
		}
	}

	private void updateSortedShortcuts() {
		sortedShortcuts = (ArrayList)shortcuts.clone();
		Collections.sort(sortedShortcuts, comparator);
	}
			
	/**
	 * Returns an array list of IPerspectiveDescriptor objects.
	 */
	public ArrayList getItems() {
		return sortedShortcuts;
	}
}

