package ca.encodeous.virtualedit.protocol;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.utils.PacketUtils;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.world.VirtualWorldView;
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class ProtocolListener extends PacketAdapter {

    private final AsynchronousManager manager;
    private final AsyncListenerHandler handler;

    public ProtocolListener(Plugin plugin) {
        super(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        this.manager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        handler = this.manager.registerAsyncHandler(this);
        handler.start((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2f));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        try{
            if(player == null) return;
            var world = player.getWorld();
            var vWorld = VirtualWorld.of(world);
            VirtualWorldView view = vWorld.getView(player);
            if(view == null) return;
            if(!player.getWorld().equals(view.world)) return;
            if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK){
                var packet = (ClientboundLevelChunkWithLightPacket) event.getPacket().getHandle();
                var pos = new ChunkPos(packet.getX(), packet.getZ());
                if(view.queuedChunkUpdates.containsKey(pos)){
                    view.queuedChunkUpdates.remove(pos);
                    return;
                }
                event.setPacket(PacketContainer.fromPacket(view.renderPacketForChunk(packet.getX(), packet.getZ())));
                view.markForChunk(packet.getX(), packet.getX(), packet.getZ(), packet.getZ(), Constants.NO_UPDATE);
            }
            else if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE){
                PacketContainer packet = event.getPacket();
                WrappedBlockData[] blockData = packet.getBlockDataArrays().readSafely(0);
                short[] blockLocations = packet.getShortArrays().readSafely(0);

                Vector v = PacketUtils.getChunkModified(packet);

                boolean changed = false;
                for(int i = 0; i < blockData.length; i++){
                    Vector q = PacketUtils.getShortLocation(blockLocations[i]);
                    var state = view.renderAt(q.add(v));
                    if(state != null){
                        blockData[i] = WrappedBlockData.fromHandle(state);
                        changed = true;
                    }
                }
                if(changed){
                    PacketUtils.setChangeData(blockData, packet);
                }
            }
            else if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE){
                var packet = event.getPacket();
                var pos = packet.getBlockPositionModifier();
                var vec = pos.read(0).toVector();
                var state = view.renderAt(vec);
                if(state != null){
                    packet.getBlockData().write(0, WrappedBlockData.fromHandle(state));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unregister() {
        this.manager.unregisterAsyncHandler(handler);
    }
}
