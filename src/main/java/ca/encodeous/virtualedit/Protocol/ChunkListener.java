package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.Utils.PacketUtils;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.imprex.orebfuscator.chunk.ChunkStruct;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.util.BlockPos;
import net.imprex.orebfuscator.util.ChunkPosition;
import net.imprex.orebfuscator.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Set;

public class ChunkListener extends PacketAdapter {

    private final AsynchronousManager manager;
    private final AsyncListenerHandler handler;
    private ChunkProcessor processor;

    public ChunkListener(Plugin plugin) {
        super(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        this.manager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        handler = this.manager.registerAsyncHandler(this);
        handler.start((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2f));
        processor = new ChunkProcessor();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;
        VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());
        if(view == null) return;
        if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK){
            ChunkStruct chunkStruct = new ChunkStruct(event.getPacket(), player.getWorld());
            try{
                processor.process(chunkStruct, view);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE){
            PacketContainer packet = event.getPacket();
            WrappedBlockData[] blockData = packet.getBlockDataArrays().readSafely(0);
            short[] blockLocations = packet.getShortArrays().readSafely(0);

            Vector v = PacketUtils.getChunkModified(packet);

            boolean changed = false;
            for(int i = 0; i < blockData.length; i++){
                Vector q = PacketUtils.getShortLocation(blockLocations[i]);
                Material mat = view.ProcessWorldView(q.add(v));
                if(mat != null){
                    blockData[i] = WrappedBlockData.createData(mat);
                    changed = true;
                }
            }
            if(changed){
                PacketUtils.setChangeData(blockData, blockLocations, packet);
                event.setPacket(packet);
            }
        }
        else if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE){
            WrapperPlayServerBlockChange pkt = new WrapperPlayServerBlockChange(event.getPacket());
            Material mat = view.ProcessWorldView(pkt.getLocation().toVector());
            if(mat != null){
                pkt.setBlockData(WrappedBlockData.createData(mat));
                event.setPacket(pkt.getHandle());
            }
        }
    }

    public void unregister() {
        this.manager.unregisterAsyncHandler(handler);
    }
}
