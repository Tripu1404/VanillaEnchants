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
        getLogger().info("CustomEnchantments activo (compatible NukkitX 2.x, sin metadata)!");
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
            float newDamage = event.getDamage() + (float)(extraLevel * sharpnessMultiplier);
            event.setDamage(newDamage);
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
    }

    @EventHandler
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // --- Protection (ID 0) extra después del límite Vanilla 4 ---
        double totalReduction = 0;
        Item[] armorContents = player.getArmorInventory().getContents();
        for (Item armor : armorContents) {
            if (armor == null) continue;
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
        if (!(event.getEntity() instanceof Player)) return;
        Player player = event.getEntity();

        // --- Looting (ID 14) extra después del límite Vanilla 3 ---
        Item weapon = player.getInventory().getItemInHand();
        Enchantment looting = weapon.getEnchantment(14);
        int extraLooting = 0;
        if (looting != null && looting.getLevel() > 3) {
            extraLooting = looting.getLevel() - 3;
        }

        if (extraLooting > 0) {
            // Convertimos el array de drops a lista para agregar más
            Item[] drops = event.getDrops();
            List<Item> newDrops = new ArrayList<>();
            for (Item drop : drops) {
                newDrops.add(drop.clone());
            }

            // Duplicamos según extraLooting
            for (int i = 0; i < extraLooting; i++) {
                for (Item drop : drops) {
                    newDrops.add(drop.clone());
                }
            }

            // Convertimos la lista de vuelta a array
            Item[] combined = new Item[newDrops.size()];
            combined = newDrops.toArray(combined);
            event.setDrops(combined);
        }
    }
}
