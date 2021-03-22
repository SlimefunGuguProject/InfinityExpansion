package io.github.mooy1.infinityexpansion.implementation.storage;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.mooy1.infinityexpansion.implementation.Categories;
import io.github.mooy1.infinityexpansion.implementation.materials.Items;
import io.github.mooy1.infinitylib.core.PluginUtils;
import io.github.mooy1.infinitylib.items.PersistentItemStack;
import io.github.mooy1.infinitylib.items.StackUtils;
import io.github.mooy1.infinitylib.slimefun.abstracts.AbstractContainer;
import io.github.mooy1.infinitylib.slimefun.presets.LorePreset;
import io.github.mooy1.infinitylib.slimefun.presets.MenuPreset;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.mrCookieSlime.Slimefun.cscorelib2.collections.Pair;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A block that stored large amounts of 1 item
 *
 * @author Mooy1
 *
 * Thanks to FluffyBear for stuff to learn from
 */
public final class StorageUnit extends AbstractContainer {

    public static void setup(InfinityExpansion plugin) {
        new StorageUnit(BASIC, BASIC_STORAGE, new ItemStack[] {
                new ItemStack(Material.OAK_LOG), Items.MAGSTEEL, new ItemStack(Material.OAK_LOG),
                new ItemStack(Material.OAK_LOG), new ItemStack(Material.BARREL), new ItemStack(Material.OAK_LOG),
                new ItemStack(Material.OAK_LOG), Items.MAGSTEEL, new ItemStack(Material.OAK_LOG)
        }).register(plugin);
        new StorageUnit(ADVANCED, ADVANCED_STORAGE, new ItemStack[] {
                Items.MAGSTEEL, Items.MACHINE_CIRCUIT, Items.MAGSTEEL,
                Items.MAGSTEEL, StorageUnit.BASIC, Items.MAGSTEEL,
                Items.MAGSTEEL, Items.MACHINE_CIRCUIT, Items.MAGSTEEL
        }).register(plugin);
        new StorageUnit(REINFORCED, REINFORCED_STORAGE, new ItemStack[] {
                Items.MAGSTEEL_PLATE, Items.MACHINE_CIRCUIT, Items.MAGSTEEL_PLATE,
                Items.MAGSTEEL_PLATE, StorageUnit.ADVANCED, Items.MAGSTEEL_PLATE,
                Items.MAGSTEEL_PLATE, Items.MACHINE_PLATE, Items.MAGSTEEL_PLATE
        }).register(plugin);
        new StorageUnit(VOID, VOID_STORAGE, new ItemStack[] {
                Items.VOID_INGOT, Items.MACHINE_PLATE, Items.VOID_INGOT,
                Items.MAGNONIUM, StorageUnit.REINFORCED, Items.MAGNONIUM,
                Items.VOID_INGOT, Items.MACHINE_CORE, Items.VOID_INGOT
        }).register(plugin);
        new StorageUnit(INFINITY, INFINITY_STORAGE, new ItemStack[] {
                Items.INFINITY, Items.VOID_INGOT, Items.INFINITY,
                Items.INFINITY, StorageUnit.VOID, Items.INFINITY,
                Items.INFINITY, Items.VOID_INGOT, Items.INFINITY
        }).register(plugin);
    }

    /* Storage amounts for each tier */
    private static final int BASIC_STORAGE = 6400;
    private static final int ADVANCED_STORAGE = 25600;
    private static final int REINFORCED_STORAGE = 102400;
    private static final int VOID_STORAGE = 409600;
    static final int INFINITY_STORAGE = 1_600_000_000;

    /* Items */
    public static final SlimefunItemStack BASIC = new SlimefunItemStack(
            "BASIC_STORAGE",
            Material.OAK_WOOD,
            "&9Basic &8Storage Unit",
            LorePreset.storesItem(StorageUnit.BASIC_STORAGE)
    );
    public static final SlimefunItemStack ADVANCED = new SlimefunItemStack(
            "ADVANCED_STORAGE",
            Material.DARK_OAK_WOOD,
            "&cAdvanced &8Storage Unit",
            LorePreset.storesItem(StorageUnit.ADVANCED_STORAGE)
    );
    public static final SlimefunItemStack REINFORCED = new SlimefunItemStack(
            "REINFORCED_STORAGE",
            Material.ACACIA_WOOD,
            "&fReinforced &8Storage Unit",
            LorePreset.storesItem(StorageUnit.REINFORCED_STORAGE)
    );
    public static final SlimefunItemStack VOID = new SlimefunItemStack(
            "VOID_STORAGE",
            Material.CRIMSON_HYPHAE,
            "&8Void &8Storage Unit",
            LorePreset.storesItem(StorageUnit.VOID_STORAGE)
    );
    public static final SlimefunItemStack INFINITY = new SlimefunItemStack(
            "INFINITY_STORAGE",
            Material.WARPED_HYPHAE,
            "&bInfinity &8Storage Unit",
            "&6Capacity: &eInfinite items"
    );

    /* Namespaced keys */
    static final NamespacedKey EMPTY_KEY = PluginUtils.getKey("empty"); // key for empty item
    static final NamespacedKey DISPLAY_KEY = PluginUtils.getKey("display"); // key for display item
    private static final NamespacedKey OLD_ITEM_KEY = PluginUtils.getKey("stored_item"); // old item key in pdc
    private static final NamespacedKey ITEM_KEY = PluginUtils.getKey("item"); // item key for item pdc
    private static final NamespacedKey AMOUNT_KEY = PluginUtils.getKey("stored"); // amount key for item pdc

    /* Menu slots */
    static final int INPUT_SLOT = MenuPreset.slot1;
    static final int DISPLAY_SLOT = MenuPreset.slot2;
    static final int STATUS_SLOT = DISPLAY_SLOT - 9;
    static final int OUTPUT_SLOT = MenuPreset.slot3;
    private static final int INTERACT_SLOT = DISPLAY_SLOT + 9;

    /* Menu items */
    private static final ItemStack INTERACTION_ITEM = new CustomItem(Material.LIME_STAINED_GLASS_PANE,
            "&aQuick Actions",
            "&bLeft Click: &7Withdraw 1 item",
            "&bRight Click: &7Withdraw 1 stack",
            "&bShift Left Click: &7Deposit inventory",
            "&bShift Right Click: &7Withdraw inventory"
    );
    private static final ItemStack LOADING_ITEM = new CustomItem(Material.CYAN_STAINED_GLASS_PANE,
            "&bStatus",
            "&7Loading..."
    );

    /* Instance constants */
    private final Map<Block, StorageCache> caches = new HashMap<>();
    final int max;

    private StorageUnit(SlimefunItemStack item, int max, ItemStack[] recipe) {
        super(Categories.STORAGE, item, StorageForge.TYPE, recipe);
        this.max = max;

        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return true;
            }

            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                StorageUnit.this.caches.get(b).updateStatus(b);
            }
        });
    }

    @Override
    protected void onNewInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
        StorageCache cache = new StorageCache(StorageUnit.this, b, menu);
        StorageUnit.this.caches.put(b, cache);
        menu.addMenuClickHandler(STATUS_SLOT, cache::voidExcessHandler);
        menu.addMenuClickHandler(INTERACT_SLOT, cache::interactHandler);
    }

    @Override
    protected void onBreak(@Nonnull BlockBreakEvent e, @Nonnull BlockMenu menu, @Nonnull Location l) {
        this.caches.remove(e.getBlock()).destroy(e);
    }

    @Override
    protected void onPlace(@Nonnull BlockPlaceEvent e, @Nonnull Block b) {
        Pair<ItemStack, Integer> data = loadFromStack(e.getItemInHand().getItemMeta());
        if (data != null) {
            PluginUtils.runSync(() -> this.caches.get(b)
                    .load(data.getFirstValue(), data.getFirstValue().getItemMeta())
                    .setAmount(data.getSecondValue())
            );
        }
    }

    @Override
    protected void setupMenu(@Nonnull BlockMenuPreset blockMenuPreset) {
        MenuPreset.setupBasicMenu(blockMenuPreset);
        blockMenuPreset.addMenuClickHandler(DISPLAY_SLOT, ChestMenuUtils.getEmptyClickHandler());
        blockMenuPreset.addItem(INTERACT_SLOT, INTERACTION_ITEM);
        blockMenuPreset.addItem(STATUS_SLOT, LOADING_ITEM);
    }

    @Nonnull
    @Override
    protected int[] getTransportSlots(@Nonnull DirtyChestMenu dirtyChestMenu, @Nonnull ItemTransportFlow flow, ItemStack itemStack) {
        if (flow == ItemTransportFlow.INSERT) {
            StorageCache cache = this.caches.get(((BlockMenu) dirtyChestMenu).getBlock());
            if (cache.isEmpty() || cache.matches(itemStack)) {
                cache.input();
                return new int[] {INPUT_SLOT};
            }
        } else if (flow == ItemTransportFlow.WITHDRAW) {
            this.caches.get(((BlockMenu) dirtyChestMenu).getBlock()).output();
            return new int[] {OUTPUT_SLOT};
        }
        return new int[0];
    }

    static void transferToStack(@Nonnull ItemStack source, @Nonnull ItemStack target) {
        Pair<ItemStack, Integer> data = loadFromStack(source.getItemMeta());
        if (data != null) {
            target.setItemMeta(saveToStack(target.getItemMeta(), data.getFirstValue(),
                    StackUtils.getDisplayName(data.getFirstValue()), data.getSecondValue()));
        }
    }

    static ItemMeta saveToStack(ItemMeta meta, ItemStack displayItem, String displayName, int amount) {
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            lore.add(ChatColor.GOLD + "Stored: " + displayName + ChatColor.YELLOW + " x " + amount);
            meta.setLore(lore);
        }
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentItemStack.instance(), displayItem);
        meta.getPersistentDataContainer().set(AMOUNT_KEY, PersistentDataType.INTEGER, amount);
        return meta;
    }

    private static Pair<ItemStack, Integer> loadFromStack(ItemMeta meta) {
        // get amount
        Integer amount = meta.getPersistentDataContainer().get(AMOUNT_KEY, PersistentDataType.INTEGER);
        if (amount != null) {

            // check for old id
            String oldID = meta.getPersistentDataContainer().get(OLD_ITEM_KEY, PersistentDataType.STRING);
            if (oldID != null) {
                ItemStack item = StackUtils.getItemByIDorType(oldID);
                if (item != null) {
                    // add the display key to it
                    ItemMeta update = item.getItemMeta();
                    update.getPersistentDataContainer().set(DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);
                    item.setItemMeta(update);
                    return new Pair<>(item, amount);
                }
            }

            // get item
            ItemStack item = meta.getPersistentDataContainer().get(ITEM_KEY, PersistentItemStack.instance());
            if (item != null) {
                return new Pair<>(item, amount);
            }
        }
        return null;
    }

}
