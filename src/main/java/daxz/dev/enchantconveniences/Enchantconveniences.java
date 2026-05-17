package daxz.dev.enchantconveniences;

import daxz.dev.enchantconveniences.Registry.EventRegister;
import org.bukkit.plugin.java.JavaPlugin;

public final class Enchantconveniences extends JavaPlugin {

    public static Enchantconveniences instance;


    @Override
    public void onEnable() {
        instance = this;
        EventRegister.registerEvents();
    }

    @Override
    public void onDisable() {
    }


    public static Enchantconveniences getInstance() {
        return instance;
    }
}
