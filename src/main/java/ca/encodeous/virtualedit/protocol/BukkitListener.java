package ca.encodeous.virtualedit.protocol;

import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class BukkitListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerLoginEvent(PlayerLoginEvent event){
        VirtualWorld.Instance.AddPlayer(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuitEvent(PlayerQuitEvent event){
        VirtualWorld.Instance.RemovePlayer(event.getPlayer());
    }
}
