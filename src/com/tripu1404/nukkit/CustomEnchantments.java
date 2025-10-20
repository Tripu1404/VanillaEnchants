package com.tripu1404.nukkit;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.List;

public class CustomEnchantmentsUnlimited extends PluginBase implements Listener {

    private double sharpnessMultiplier = 1.25;
    private double protectionMultiplier = 1.0;
    private double thornsDamage = 1.0;
    private double knockbackMultiplier = 0.5;
    private double unbreakingMultiplier = 0.5;
    private double lootingMultiplier = 1.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CustomEnchantmentsUnlimited activo!");
    }

    // ------------------ COMBATE ------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Item weapon = player.getInventory().getItemInHand();

        float baseDamage = 1f; // daño base genérico si quieres puedes personalizar por arma
        float knockback = 0f;

        // --- Sharpness (ID 9) ---
        Enchantment sharp = weapon.getEnchantment(9);
        if (sharp != null) {
            int extra = sharp.getLevel();
            baseDamage += extra * sharpnessMultiplier;
        }

        // --- Knockback (ID 12) ---
        Enchantment kb = weapon.getEnchantment(12);
        if (kb != null) {
            int extra = kb.getLevel();
            knockback += extra * knockbackMultiplier;
        }

        // --- Thorns (ID 5) ---
        Enchantment thorns = weapon.getEnchantment(5);
        if (thorns != null) {
            int extra = thorns.getLevel();
            Entity target = event.getEntity();
            target.attack((float)(extra * thornsDamage));
        }

        // --- Unbreaking (ID 17) ---
        Enchantment unbreaking = weapon.getEnchantment(17);
        if (unbreaking != null) {
            int extra = unbreaking.getLevel();
            double chance = 1.0 / (extra + 1) * unbreakingMultiplier;
            if (Math.random() > chance) {
                weapon.setDamage(weapon.getDamage()); // evita desgaste
            }
        }

        // Aplicar daño y knockback manualmente
        event.setDamage(baseDamage);
        event.setKnockBack(knockback);
    }

    // ------------------ PROTECCION ------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        float totalReduction = 0f;

        // Armor slots: 0=boots,1=leggings,2=chestplate,3=helmet
        Item[] armor = new Item[4];
        armor[0] = player.getInventory().getBoots();
        armor[1] = player.getInventory().getLeggings();
        armor[2] = player.getInventory().getChestplate();
        armor[3] = player.getInventory().getHelmet();

        for (Item a : armor) {
            if (a == null) continue;
            Enchantment prot = a.getEnchantment(0); // Protection
            if (prot != null) {
                int extra = prot.getLevel();
                totalReduction += extra * protectionMultiplier;
            }
        }

        float newDamage = event.getDamage() - totalReduction;
        if (newDamage < 0) newDamage = 0;
        event.setDamage(newDamage);
    }

    // ------------------ LOOTING ------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Item weapon = player.getInventory().getItemInHand();

        Enchantment looting = weapon.getEnchantment(14);
        int extraLoot = looting != null ? looting.getLevel() : 0;
        if (extraLoot <= 0) return;

        Item[] drops = event.getDrops();
        List<Item> newDrops = new ArrayList<>();
        for (Item drop : drops) newDrops.add(drop.clone());

        // Generar drops extra según nivel de Looting
        for (int i = 0; i < extraLoot; i++) {
            for (Item drop : drops) newDrops.add(drop.clone());
        }

        event.setDrops(newDrops.toArray(new Item[0]));
    }
}
