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
        getLogger().info("CustomEnchantments activo!");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Item item = player.getInventory().getItemInHand();

        // --- Sharpness (Filo) extra después del límite Vanilla 5 ---
        Enchantment sharpness = item.getEnchantment(Enchantment.ID_SHARPNESS);
        if (sharpness != null && sharpness.getLevel() > 5) {
            int extraLevel = sharpness.getLevel() - 5;
            event.setBaseDamage(event.getBaseDamage() + extraLevel * sharpnessMultiplier);
        }

        // --- Knockback (Retroceso) extra después del límite Vanilla 2 ---
        Enchantment knockback = item.getEnchantment(Enchantment.ID_KNOCKBACK);
        if (knockback != null && knockback.getLevel() > 2) {
            int extraLevel = knockback.getLevel() - 2;
            event.setKnockBack(event.getKnockBack() + extraLevel * knockbackMultiplier);
        }

        // --- Thorns (Espinas) extra después del límite Vanilla 3 ---
        Enchantment thorns = item.getEnchantment(Enchantment.ID_THORNS);
        if (thorns != null && thorns.getLevel() > 3) {
            int extraLevel = thorns.getLevel() - 3;
            if (event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                target.attack(new EntityDamageEvent(target, EntityDamageEvent.DamageCause.CUSTOM, extraLevel * thornsDamage));
            }
        }

        // --- Unbreaking (Irrompible) extra después del límite Vanilla 3 ---
        Enchantment unbreaking = item.getEnchantment(Enchantment.ID_UNBREAKING);
        if (unbreaking != null && unbreaking.getLevel() > 3) {
            int extraLevel = unbreaking.getLevel() - 3;
            double chance = 1.0 / (extraLevel + 1) * unbreakingMultiplier;
            if (Math.random() > chance) {
                item.setDamage(item.getDamage()); // Cancelamos daño extra al item
            }
        }

        // --- Looting (Botín) extra después del límite Vanilla 3 ---
        Enchantment looting = item.getEnchantment(Enchantment.ID_LOOTING);
        if (looting != null && looting.getLevel() > 3) {
            int extraLevel = looting.getLevel() - 3;
            event.getEntity().setData("custom_looting_extra", extraLevel);
        }
    }

    @EventHandler
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // --- Protection (Protección) extra después del límite Vanilla 4 ---
        double totalReduction = 0;
        for (Item armor : player.getArmorInventory().getContents().values()) {
            Enchantment prot = armor.getEnchantment(Enchantment.ID_PROTECTION);
            if (prot != null && prot.getLevel() > 4) {
                int extraLevel = prot.getLevel() - 4;
                totalReduction += extraLevel * protectionMultiplier;
            }
        }

        double newDamage = event.getDamage() - totalReduction;
        if (newDamage < 0) newDamage = 0;
        event.setDamage(newDamage);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        int extraLooting = entity.hasData("custom_looting_extra") ? entity.getData("custom_looting_extra") : 0;
        if (extraLooting > 0) {
            List<Item> newDrops = new ArrayList<>();
            for (Item drop : event.getDrops()) {
                for (int i = 0; i < extraLooting; i++) {
                    newDrops.add(drop.clone());
                }
            }
            event.getDrops().addAll(newDrops);
        }
    }
}
