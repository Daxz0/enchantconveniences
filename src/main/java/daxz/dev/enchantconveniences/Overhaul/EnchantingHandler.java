package daxz.dev.enchantconveniences.Overhaul;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantingHandler implements Listener {

    private final Registry<Enchantment> enchantmentRegistry = RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT);
    @EventHandler
    public void preparesEnchant(PrepareItemEnchantEvent event) {

        Location loc = event.getEnchantBlock().getLocation();
        ItemStack item = event.getItem();
        World world = loc.getWorld();
        List<Block> nearbyModifiers = new ArrayList<>();
        List<Enchantment> chiseledEnchantments = new ArrayList<>();

        int radius = 6;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block target = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY(), loc.getBlockZ() + z);
                if (target.getType() == Material.BOOKSHELF || target.getType() == Material.CHISELED_BOOKSHELF) {
                    nearbyModifiers.add(target);
                    if (target.getState() instanceof ChiseledBookshelf shelf){
                        Inventory inv = shelf.getInventory();
                        for (ItemStack i : inv.getContents()){
                            if (i.getType() == Material.ENCHANTED_BOOK){
                                chiseledEnchantments.addAll(i.getItemMeta().getEnchants().keySet());
                            }
                        }

                    }

                }
            }
        }

        List<Enchantment> applicableFirst = this.getApplicableEnchantments(item);
        Map<Enchantment, Float> weighted = new HashMap<>();

        for (Enchantment enchant : applicableFirst){
            if (chiseledEnchantments.contains(enchant)){
                weighted.put(enchant, (float) Collections.frequency(chiseledEnchantments, enchant));
            }
            else weighted.put(enchant, 1.0f);
        }







    }

    public static List<Enchantment> getApplicableEnchantments(ItemStack item) {
        Map<Enchantment, Integer> existing = item.getEnchantments();

        var applicable = new ArrayList<>();

        for (var enchantment : RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)) {

            if (!enchantment.canEnchantItem(item))
                continue;

            var conflicts = false;

            for (var existingEnchant : existing.keySet()) {
                if (enchantment.conflictsWith(existingEnchant) || existingEnchant.conflictsWith(enchantment)) {
                    conflicts = true;
                    break;
                }
            }

            if (!conflicts)
                applicable.add(enchantment);
        }
        return applicable;
    }

}
