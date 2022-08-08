package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class BukkitListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerJoinEvent(PlayerJoinEvent event){
        VirtualWorld.Instance.AddPlayer(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuitEvent(PlayerQuitEvent event){
        VirtualWorld.Instance.RemovePlayer(event.getPlayer());
    }
    @EventHandler
    public void PlayerChangedWorld(PlayerChangedWorldEvent event) {
        VirtualWorld.GetPlayerView(event.getPlayer().getUniqueId()).RecalculateViewport();
    }

    @EventHandler
    public void PlayerTeleport(PlayerTeleportEvent event) {
        VirtualWorld.GetPlayerView(event.getPlayer().getUniqueId()).RefreshViewport();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        VirtualWorld.GetPlayerView(event.getPlayer().getUniqueId()).RefreshViewport();
    }
}
