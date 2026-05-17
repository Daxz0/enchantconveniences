package daxz.dev.enchantconveniences.Overhaul;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
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
        Player player = event.getEnchanter();

        List<Enchantment> chiseledEnchantments = new ArrayList<>();
        int bookshelfCount = 0;

        int radius = 4;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 1; y++) {
                    Block target = world.getBlockAt(
                            loc.getBlockX() + x,
                            loc.getBlockY() + y,
                            loc.getBlockZ() + z
                    );

                    if (target.getType() == Material.BOOKSHELF) {
                        bookshelfCount++;
                    } else if (target.getType() == Material.CHISELED_BOOKSHELF
                            && target.getState() instanceof ChiseledBookshelf shelf) {
                        bookshelfCount++;
                        for (ItemStack i : shelf.getInventory().getContents()) {
                            if (i != null && i.getType() == Material.ENCHANTED_BOOK) {
                                chiseledEnchantments.addAll(
                                        i.getItemMeta().getEnchants().keySet()
                                );
                            }
                        }
                    }
                }
            }
        }

        bookshelfCount = Math.min(bookshelfCount, 15);

        List<Enchantment> applicableEnchants = getApplicableEnchantments(item);

        Map<Enchantment, Float> weighted = new HashMap<>();
        for (Enchantment enchant : applicableEnchants) {
            if (enchant == Enchantment.MENDING) {
                if (chiseledEnchantments.contains(enchant)) {
                    weighted.put(enchant, (float) Collections.frequency(chiseledEnchantments, enchant) * 2);
                }
            } else if (chiseledEnchantments.contains(enchant)) {
                weighted.put(enchant, 1.0f + Collections.frequency(chiseledEnchantments, enchant));
                player.sendMessage(Component.text(weighted.get(enchant)));
            } else {
                weighted.put(enchant, 1.0f);
            }
        }

        // Re-derive applicable list from weight map (respects mending exclusion)
        List<Enchantment> eligiblePool = new ArrayList<>(weighted.keySet());

        int[] slotsXp = computeSlotLevels(bookshelfCount);
        var offers = event.getOffers();
        Random random = new Random();

        for (int i = 0; i < Math.min(offers.length, slotsXp.length); i++) {
            int xpLevel = slotsXp[i];

            List<Enchantment> eligible = new ArrayList<>(eligiblePool);

            if (eligible.isEmpty()) continue;

            Enchantment choice = getRandomWeighted(eligible, weighted, random);
            if (choice == null) continue;

            int enchantLevel = Math.max(1, Math.min(choice.getMaxLevel(),
                    (int) Math.ceil((xpLevel / 30.0) * choice.getMaxLevel())
            ));

//            offers[i].setEnchantment(choice);
//            offers[i].setEnchantmentLevel(enchantLevel);
//            offers[i].setCost(xpLevel);

            offers[i] = new EnchantmentOffer(choice, enchantLevel, xpLevel);
        }




    }

    private int[] computeSlotLevels(int b) {
        Random random = new Random();
        int base = (1 + random.nextInt(8)) + (b / 2) + random.nextInt(b + 1);

        int top    = Math.max(base / 3, 1);
        int middle = (base * 2) / 3 + 1;
        int bottom = Math.max(base, b * 2);

        return new int[]{ top, middle, bottom };
    }

    private Enchantment getRandomWeighted(List<Enchantment> eligible, Map<Enchantment, Float> weighted, Random random) {
        float totalWeight = 0f;
        for (Enchantment e : eligible) {
            totalWeight += weighted.getOrDefault(e, 1.0f);
        }

        float roll = random.nextFloat() * totalWeight;
        float cumulative = 0f;

        for (Enchantment e : eligible) {
            cumulative += weighted.getOrDefault(e, 1.0f);
            if (roll < cumulative) {
                return e;
            }
        }
        return eligible.get(eligible.size() - 1);

    }

    public static ArrayList<Enchantment> getApplicableEnchantments(ItemStack item) {
        Map<Enchantment, Integer> existing = item.getEnchantments();

        var applicable = new ArrayList<Enchantment>();

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
