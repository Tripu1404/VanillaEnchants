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

    private double sharpnessMultiplier = 1.25;
    private double protectionMultiplier = 1.0;
    private double thornsDamage = 1.0;
    private double knockbackMultiplier = 0.5;
    private double lootingMultiplier = 1.0;
    private double unbreakingMultiplier = 0.5;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CustomEnchantments funcional más allá de Vanilla activado!");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Item weapon = player.getInventory().getItemInHand();

        float damage = event.getDamage(); // daño base antes de encantamientos
        float knockback = event.getKnockBack();

        // --- Sharpness (ID 9) ---
        Enchantment sharp = weapon.getEnchantment(9);
        if (sharp != null) {
            int extra = Math.max(0, sharp.getLevel() - 5);
            damage += extra * sharpnessMultiplier;
        }

        // --- Knockback (ID 12) ---
        Enchantment kb = weapon.getEnchantment(12);
        if (kb != null) {
            int extra = Math.max(0, kb.getLevel() - 2);
            knockback += extra * knockbackMultiplier;
        }

        // --- Thorns (ID 5) ---
        Enchantment thorns = weapon.getEnchantment(5);
        if (thorns != null) {
            int extra = Math.max(0, thorns.getLevel() - 3);
            Entity target = event.getEntity();
            target.attack(extra * thornsDamage); // daño directo al objetivo
        }

        // --- Unbreaking (ID 17) ---
        Enchantment unbreaking = weapon.getEnchantment(17);
        if (unbreaking != null) {
            int extra = Math.max(0, unbreaking.getLevel() - 3);
            double chance = 1.0 / (extra + 1) * unbreakingMultiplier;
            if (Math.random() > chance) {
                weapon.setDamage(weapon.getDamage()); // previene desgaste extra
            }
        }

        event.setDamage(damage);
        event.setKnockBack(knockback);
    }

    @EventHandler
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        double totalReduction = 0;

        // Armor slots: 0=boots,1=leggings,2=chestplate,3=helmet
        Item[] armor = new Item[4];
        armor[0] = player.getInventory().getBoots();
        armor[1] = player.getInventory().getLeggings();
        armor[2] = player.getInventory().getChestplate();
        armor[3] = player.getInventory().getHelmet();

        for (Item a : armor) {
            if (a == null) continue;
            Enchantment prot = a.getEnchantment(0); // Protection ID = 0
            if (prot != null) {
                int extra = Math.max(0, prot.getLevel() - 4);
                totalReduction += extra * protectionMultiplier;
            }
        }

        float newDamage = event.getDamage() - (float) totalReduction;
        if (newDamage < 0) newDamage = 0;
        event.setDamage(newDamage);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Item weapon = player.getInventory().getItemInHand();
        Enchantment looting = weapon.getEnchantment(14);
        int extraLoot = 0;
        if (looting != null) {
            extraLoot = Math.max(0, looting.getLevel() - 3);
        }

        if (extraLoot > 0) {
            Item[] drops = event.getDrops();
            List<Item> newDrops = new ArrayList<>();
            for (Item drop : drops) newDrops.add(drop.clone());

            for (int i = 0; i < extraLoot; i++) {
                for (Item drop : drops) newDrops.add(drop.clone());
            }

            event.setDrops(newDrops.toArray(new Item[0]));
        }
    }
}
