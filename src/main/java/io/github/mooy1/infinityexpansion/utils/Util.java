package io.github.mooy1.infinityexpansion.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import lombok.experimental.UtilityClass;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.mooy1.infinityexpansion.InfinityExpansion;

import org.bukkit.persistence.PersistentDataType;

@UtilityClass
public final class Util {
    static final NamespacedKey DISPLAY_KEY = InfinityExpansion.createKey("display-only");
    @Nonnull
    public static ItemStack getDisplayItem(@Nonnull ItemStack output) {
        ItemMeta meta = output.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        }
        else {
            lore = new ArrayList<>();
        }
        lore.add("");
        lore.add(ChatColor.GREEN + "-------------------");
        lore.add(ChatColor.GREEN + "\u21E8 点击合成");
        lore.add(ChatColor.GREEN + "-------------------");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(DISPLAY_KEY, PersistentDataType.BYTE,(byte)1);
        output.setItemMeta(meta);
        return output;
    }

    @Nonnull
    public static Map<Enchantment, Integer> getEnchants(@Nonnull ConfigurationSection section) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (String path : section.getKeys(false)) {
            Enchantment e = enchantmentByPath(path);
            if (e != null) {
                int level = section.getInt(path);
                if (level > 0 && level <= Short.MAX_VALUE) {
                    enchants.put(e, level);
                }
                else if (level != 0) {
                    section.set(path, 0);
                    InfinityExpansion.log(Level.WARNING,
                            "附魔等级 " + level
                                    + " 超出限制 " + e.getKey()
                                    + ", 正在恢复为默认值!"
                    );
                }
            }
        }
        return enchants;
    }

    @Nullable
    private static Enchantment enchantmentByPath(@Nonnull String path) {
        switch (path) {
            case "sharpness":
                return Enchantment.DAMAGE_ALL;
            case "smite":
                return Enchantment.DAMAGE_UNDEAD;
            case "bane-of-arthropods":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "efficiency":
                return Enchantment.DIG_SPEED;
            case "protection":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "fire-aspect":
                return Enchantment.FIRE_ASPECT;
            case "fortune":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "looting":
                return Enchantment.LOOT_BONUS_MOBS;
            case "silk-touch":
                return Enchantment.SILK_TOUCH;
            case "thorns":
                return Enchantment.THORNS;
            case "aqua-affinity":
                return Enchantment.WATER_WORKER;
            case "power":
                return Enchantment.ARROW_DAMAGE;
            case "flame":
                return Enchantment.ARROW_FIRE;
            case "infinity":
                return Enchantment.ARROW_INFINITE;
            case "punch":
                return Enchantment.ARROW_KNOCKBACK;
            case "feather-falling":
                return Enchantment.PROTECTION_FALL;
            case "unbreaking":
                return Enchantment.DURABILITY;
            default:
                return null;
        }
    }

    public static boolean isWaterLogged(@Nonnull Block b) {
        if (InfinityExpansion.slimefunTickCount() % 63 == 0) {
            BlockData blockData = b.getBlockData();

            if (blockData instanceof Waterlogged) {
                Waterlogged waterLogged = (Waterlogged) blockData;
                if (waterLogged.isWaterlogged()) {
                    StorageCacheUtils.setData(b.getLocation(), "water_logged", "true");
                    return true;
                }
                else {
                    StorageCacheUtils.setData(b.getLocation(), "water_logged", "false");
                    return false;
                }
            }
            else {
                return false;
            }

        }
        else {
            return "true".equals(StorageCacheUtils.getData(b.getLocation(), "water_logged"));
        }
    }

    public static int getIntData(String key, Location block) {
        String val = StorageCacheUtils.getData(block, key);
        if (val == null) {
            StorageCacheUtils.setData(block, key, "0");
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException x) {
            StorageCacheUtils.setData(block, key, "0");
            return 0;
        }
    }

}
