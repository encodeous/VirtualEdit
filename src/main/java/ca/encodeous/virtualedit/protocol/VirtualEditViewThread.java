package ca.encodeous.virtualedit.protocol;

import ca.encodeous.virtualedit.utils.MCVersion;
import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualEditViewThread extends Thread{
    private final AtomicBoolean running = new AtomicBoolean(true);
    public VirtualEditViewThread() {
        super("virtual-edit-view-thread");
    }

    @Override
    public void run() {
        while (this.running.get()) {
            try {
                Player player = VirtualWorld.Instance.PlayerUpdateQueue.poll();
                try {
                    if (player == null || !player.isOnline()) {
                        continue;
                    }
                    var view = VirtualWorld.GetPlayerView(player.getUniqueId());
                    view.sendChunksInRange();
                } finally {
                    VirtualWorld.Instance.PlayerUpdateQueue.unlock(player);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        this.running.set(false);
        this.interrupt();
    }
}
