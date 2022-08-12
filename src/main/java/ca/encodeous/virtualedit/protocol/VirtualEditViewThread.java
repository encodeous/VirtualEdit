package ca.encodeous.virtualedit.protocol;

import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.entity.Player;

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
                var action = VirtualWorld.PlayerUpdateQueue.poll();
                var player = action.getA();
                try {
                    if (player == null || !player.isOnline()) {
                        continue;
                    }
                    var world = player.getWorld();
                    if(VirtualWorld.views.containsKey(world)){
                        var vWorld = VirtualWorld.of(player.getWorld());
                        var view = vWorld.getView(player);
                        view.sendChunksInRange();
                    }
                } finally {
                    VirtualWorld.PlayerUpdateQueue.unlock(action);
                    if(action.getB() != null){
                        action.getB().run();
                    }
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
