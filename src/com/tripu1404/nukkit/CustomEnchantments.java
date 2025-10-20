package com.tripu1404.nukkit;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.metadata.FixedMetadataValue;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.List;

public class CustomEnchantments extends PluginBase implements Listener {

    private double sharpnessMultiplier;
    private double protectionMultiplier;
    private double thornsDamage;
    private double knockbackMultiplier;
    private double lootingMultiplier;
    private double unbreakingMultiplier;

    private final String METADATA_LOOTING = "custom_looting_extra";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        sharpnessMultiplier = getConfig().getDouble("enchants.sharpness_multiplier", 1.25);
        protectionMultiplier = getConfig().getDouble("enchants.protection_multiplier", 1.0);
        thornsDamage = getConfig().getDouble("enchants.thorns_damage", 1.0);
        knockbackMultiplier = getConfig().getDouble("enchants.knockback_multiplier", 0.5);
        lootingMultiplier = getConfig().getDouble("enchants.looting_multiplier", 1.0);
        unbreakingMultiplier = getConfig().getDouble("enchants.unbreaking_multiplier", 0.5);

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CustomEnchantments activo con metadata compatible NukkitX!");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Item item = player.getInventory().getItemInHand();

        // --- Sharpness (ID 9) extra después del límite Vanilla 5 ---
        Enchantment sharpness = item.getEnchantment(9);
        if (sharpness != null && sharpness.getLevel() > 5) {
            int extraLevel = sharpness.getLevel() - 5;
            float newDamage = event.getBaseDamage() + (float)(extraLevel * sharpnessMultiplier);
            event.setBaseDamage(newDamage);
        }

        // --- Knockback (ID 12) extra después del límite Vanilla 2 ---
        Enchantment knockback = item.getEnchantment(12);
        if (knockback != null && knockback.getLevel() > 2) {
            int extraLevel = knockback.getLevel() - 2;
            float newKnockback = event.getKnockBack() + (float)(extraLevel * knockbackMultiplier);
            event.setKnockBack(newKnockback);
        }

        // --- Thorns (ID 5) extra después del límite Vanilla 3 ---
        Enchantment thorns = item.getEnchantment(5);
        if (thorns != null && thorns.getLevel() > 3) {
            int extraLevel = thorns.getLevel() - 3;
            Entity target = event.getEntity();
            target.attack((float)(extraLevel * thornsDamage));
        }

        // --- Unbreaking (ID 17) extra después del límite Vanilla 3 ---
        Enchantment unbreaking = item.getEnchantment(17);
        if (unbreaking != null && unbreaking.getLevel() > 3) {
            int extraLevel = unbreaking.getLevel() - 3;
            double chance = 1.0 / (extraLevel + 1) * unbreakingMultiplier;
            if (Math.random() > chance) {
                item.setDamage(item.getDamage()); // previene daño adicional al item
            }
        }

        // --- Looting (ID 14) extra después del límite Vanilla 3 ---
        Enchantment looting = item.getEnchantment(14);
        if (looting != null && looting.getLevel() > 3) {
            int extraLevel = looting.getLevel() - 3;
            event.getEntity().setMetadata(METADATA_LOOTING, new FixedMetadataValue(this, extraLevel));
        }
    }

    @EventHandler
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // --- Protection (ID 0) extra después del límite Vanilla 4 ---
        double totalReduction = 0;
        for (Item armor : player.getArmorInventory().getContents().values()) {
            Enchantment prot = armor.getEnchantment(0); // Protection ID = 0
            if (prot != null && prot.getLevel() > 4) {
                int extraLevel = prot.getLevel() - 4;
                totalReduction += extraLevel * protectionMultiplier;
            }
        }

        float newDamage = event.getDamage() - (float) totalReduction;
        if (newDamage < 0) newDamage = 0;
        event.setDamage(newDamage);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        int extraLooting = 0;

        if (entity.hasMetadata(METADATA_LOOTING)) {
            List<MetadataValue> values = entity.getMetadata(METADATA_LOOTING);
            for (MetadataValue value : values) {
                extraLooting = value.asInt();
            }
        }

        if (extraLooting > 0) {
            List<Item> originalDrops = new ArrayList<>();
            for (Item drop : event.getDrops()) {
                originalDrops.add(drop.clone());
            }
            for (int i = 0; i < extraLooting; i++) {
                for (Item drop : originalDrops) {
                    event.getDrops().add(drop.clone());
                }
            }
        }
    }
}
