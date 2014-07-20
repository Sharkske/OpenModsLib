package openmods.gui.logic;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ValueUpdateDispatcher {

	private Multimap<Object, IValueUpdateAction> listeners = HashMultimap.create();

	public void addAction(IValueUpdateAction listener) {
		for (Object trigger : listener.getTriggers())
			listeners.put(trigger, listener);
	}

	public void trigger(Iterable<?> triggers) {
		Set<IValueUpdateAction> actionsToTrigger = Sets.newIdentityHashSet();

		for (Object trigger : triggers)
			actionsToTrigger.addAll(listeners.get(trigger));

		for (IValueUpdateAction action : actionsToTrigger)
			action.execute();
	}

}