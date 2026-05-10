package daxz.dev.enchantconveniences;

import org.bukkit.plugin.java.JavaPlugin;

public final class Enchantconveniences extends JavaPlugin {

    public static Enchantconveniences instance;


    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
    }


    public static Enchantconveniences getInstance() {
        return instance;
    }
}
