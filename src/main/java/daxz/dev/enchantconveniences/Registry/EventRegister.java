package daxz.dev.enchantconveniences.Registry;

import daxz.dev.enchantconveniences.Enchantconveniences;
import daxz.dev.enchantconveniences.Overhaul.EnchantingHandler;
import org.bukkit.plugin.PluginManager;

public class EventRegister {

    public static void registerEvents() {
        PluginManager pm = Enchantconveniences.getInstance().getServer().getPluginManager();
        pm.registerEvents(new EnchantingHandler(), Enchantconveniences.getInstance());

    }
}
