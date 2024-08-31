package io.github.helloandrewyan.smoke.listener;

import io.github.helloandrewyan.smoke.entity.SmokeSphere;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TestingListener implements Listener {
		@EventHandler
		public void onClickEvent(PlayerInteractEvent event) {
				if (!event.getAction().equals(Action.LEFT_CLICK_AIR)) return;
				if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.LIME_WOOL)) {
						SmokeSphere.castSmoke(event.getPlayer().getEyeLocation(), 2.5, 20);
						return;
				}
				if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.RED_WOOL)) {
						SmokeSphere.clearAllSmokeInstances();
				}
		}
}
