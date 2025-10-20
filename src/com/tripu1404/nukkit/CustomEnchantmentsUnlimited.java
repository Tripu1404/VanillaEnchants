package com.tripu1404.nukkit;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.PluginBase;

public class CustomEnchantmentsSimple extends PluginBase implements Listener {

    private double sharpnessMultiplier = 1.25; // daño extra por nivel de Sharpness sobre 5
    private double protectionMultiplier = 1.0; // reducción extra por nivel de Protection sobre 4

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CustomEnchantmentsSimple activo: solo Sharpness y Protection.");
    }

    // ------------------ Sharpness (Filo) ------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Item weapon = player.getInventory().getItemInHand();

        Enchantment sharp = weapon.getEnchantment(9); // ID Sharpness
        if (sharp != null && sharp.getLevel() > 5) {
            int extra = sharp.getLevel() - 5;
            float newDamage = event.getDamage() + (float)(extra * sharpnessMultiplier);
            event.setDamage(newDamage);
        }
    }

    // ------------------ Protection (Protección) ------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageFinal(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        float totalReduction = 0f;

        // Slots de armor: boots, leggings, chestplate, helmet
        Item[] armor = new Item[4];
        armor[0] = player.getInventory().getBoots();
        armor[1] = player.getInventory().getLeggings();
        armor[2] = player.getInventory().getChestplate();
        armor[3] = player.getInventory().getHelmet();

        for (Item a : armor) {
            if (a == null) continue;
            Enchantment prot = a.getEnchantment(0); // ID Protection
            if (prot != null && prot.getLevel() > 4) {
                int extra = prot.getLevel() - 4;
                totalReduction += extra * protectionMultiplier;
            }
        }

        float newDamage = event.getDamage() - totalReduction;
        if (newDamage < 0) newDamage = 0;
        event.setDamage(newDamage);
    }
}
