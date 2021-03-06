package openmods.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DelayedEntityLoadManager {
	public static final DelayedEntityLoadManager instance = new DelayedEntityLoadManager();

	private DelayedEntityLoadManager() {}

	private Multimap<Integer, IEntityLoadListener> delayedLoads = Multimaps.newSetMultimap(
			new HashMap<Integer, Collection<IEntityLoadListener>>(),
			() -> Collections.newSetFromMap(new WeakHashMap<IEntityLoadListener, Boolean>()));

	@SubscribeEvent
	public void onEntityCreate(EntityJoinWorldEvent evt) {
		final Entity entity = evt.getEntity();
		for (IEntityLoadListener callback : delayedLoads.removeAll(entity.getEntityId()))
			callback.onEntityLoaded(entity);
	}

	public void registerLoadListener(World world, IEntityLoadListener listener, int entityId) {
		Entity entity = world.getEntityByID(entityId);
		if (entity == null) delayedLoads.put(entityId, listener);
		else listener.onEntityLoaded(entity);
	}
}
