package openmods.utils;

import com.google.common.collect.ImmutableSet;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import openmods.Log;

public class NetUtils {

	public static NetworkDispatcher getPlayerDispatcher(EntityPlayerMP player) {
		NetworkDispatcher dispatcher = player.connection.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
		return dispatcher;
	}

	public static Set<EntityPlayerMP> getPlayersWatchingEntity(WorldServer server, Entity entity) {
		final EntityTracker tracker = server.getEntityTracker();

		@SuppressWarnings("unchecked")
		final Set<? extends EntityPlayerMP> trackingPlayers = (Set<? extends EntityPlayerMP>)tracker.getTrackingPlayers(entity);
		return ImmutableSet.copyOf(trackingPlayers);
	}

	public static Set<EntityPlayerMP> getPlayersWatchingChunk(WorldServer world, int chunkX, int chunkZ) {
		final PlayerChunkMapEntry playerChunkMap = world.getPlayerChunkMap().getEntry(chunkX, chunkZ);

		if (playerChunkMap == null)
			return ImmutableSet.of();

		return ImmutableSet.copyOf(playerChunkMap.getWatchingPlayers());
	}

	public static Set<EntityPlayerMP> getPlayersWatchingBlock(WorldServer world, int blockX, int blockZ) {
		return getPlayersWatchingChunk(world, blockX >> 4, blockZ >> 4);
	}

	public static final ChannelFutureListener LOGGING_LISTENER = future -> {
		if (!future.isSuccess()) {
			Throwable cause = future.cause();
			Log.severe(cause, "Crash in pipeline handler");
		}
	};

	public static void executeSynchronized(ChannelHandlerContext ctx, Runnable runnable) {
		final IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
		if (!thread.isCallingFromMinecraftThread()) {
			thread.addScheduledTask(runnable);
		} else {
			runnable.run();
		}
	}

}
