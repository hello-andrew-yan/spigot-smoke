package io.github.helloandrewyan.smoke;

import io.github.helloandrewyan.smoke.listener.TestingListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Smoke extends JavaPlugin {

    private static Smoke instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new TestingListener(), this);
    }

    @Override
    public void onDisable() {

    }

    public static Smoke getInstance() {
        return instance;
    }
}
