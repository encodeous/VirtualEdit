package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.Utils.PacketUtils;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
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
            VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());
            if(view == null) return;
            if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK){
                var packet = (ClientboundLevelChunkWithLightPacket) event.getPacket().getHandle();
                var cWorld = (CraftWorld)player.getWorld();
                var world = cWorld.getHandle();
                LevelChunk chunk = world.getChunk(packet.getX(), packet.getZ());
                var processed = view.processChunk(chunk, world);
                var procPacket = new ClientboundLevelChunkWithLightPacket(processed, world.getLightEngine(), null, null, true, false);
                event.setPacket(PacketContainer.fromPacket(procPacket));
            }
            else if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE){
                PacketContainer packet = event.getPacket();
                WrappedBlockData[] blockData = packet.getBlockDataArrays().readSafely(0);
                short[] blockLocations = packet.getShortArrays().readSafely(0);

                Vector v = PacketUtils.getChunkModified(packet);

                boolean changed = false;
                for(int i = 0; i < blockData.length; i++){
                    Vector q = PacketUtils.getShortLocation(blockLocations[i]);
                    Material mat = view.processWorldView(q.add(v));
                    if(mat != null){
                        blockData[i] = WrappedBlockData.createData(mat);
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
                Material mat = view.processWorldView(vec);
                if(mat != null){
                    packet.getBlockData().write(0, WrappedBlockData.createData(mat));
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
