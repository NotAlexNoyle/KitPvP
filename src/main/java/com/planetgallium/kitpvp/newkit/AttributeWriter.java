package com.planetgallium.kitpvp.newkit;

import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Toolkit;
import com.planetgallium.kitpvp.util.XEnchantment;
import com.planetgallium.kitpvp.util.XMaterial;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

public class AttributeWriter {

    public static void potionEffectToResource(Resource resource, String path, PotionEffect effect) {

        if (effect == null) return;

        resource.set(path + "." + effect.getType() + ".Amplifier", effect.getAmplifier());
        resource.set(path + "." + effect.getType() + ".Duration", effect.getDuration());
        resource.save();

    }

    public static void itemStackToResource(Resource resource, String path, ItemStack item) {

        if (item == null) return;

        ItemMeta meta = item.getItemMeta();

        resource.set(path + ".Name", meta.hasDisplayName() ? meta.getDisplayName().replace("§", "&") : null);
        resource.set(path + ".Lore", meta.getLore());
        resource.set(path + ".Item", item.getType().toString());
        resource.set(path + ".Amount", item.getAmount() == 1 ? null : item.getAmount());
        resource.save();

        // might not need to pass the meta tbh
        serializeDyedArmor(resource, item, meta, path);
        serializeSkull(resource, item, meta, path);
        serializeTippedArrows(resource, item, meta, path);
        serializePotion(resource, item, meta, path);
        serializeEnchantments(resource, item, path);
        serializeDurability(resource, item, meta, path);

    }

    private static void serializeEffects(Resource resource, PotionMeta meta, String path) {

        PotionData data = meta.getBasePotionData();

        if (meta.getCustomEffects().size() > 0) {

            for (PotionEffect potion : meta.getCustomEffects()) {

                resource.set(path + ".Effects." + potion.getType().getName() + ".Amplifier", potion.getAmplifier() + 1);
                resource.set(path + ".Effects." + potion.getType().getName() + ".Duration", potion.getDuration() / 20);
                resource.save();

            }

        } else {

            String effectName = data.getType().getEffectType().getName();
            resource.set(path + ".Type", data.getType().toString());
            resource.set(path + ".Effects." + effectName + ".Upgraded", data.isUpgraded());
            resource.set(path + ".Effects." + effectName + ".Extended", data.isExtended());
            resource.save();

        }

        if (meta.hasColor()) {

            // check if this ever gets used?
            Color potionColor = meta.getColor();
            resource.set(path + ".Color.Red", potionColor.getRed());
            resource.set(path + ".Color.Green", potionColor.getGreen());
            resource.set(path + ".Color.Blue", potionColor.getBlue());
            resource.save();

        }

    }

    private static void serializeDyedArmor(Resource resource, ItemStack item, ItemMeta meta, String path) {

        if (item.getType() == XMaterial.LEATHER_HELMET.parseMaterial().get() ||
            item.getType() == XMaterial.LEATHER_CHESTPLATE.parseMaterial().get() ||
            item.getType() == XMaterial.LEATHER_LEGGINGS.parseMaterial().get() ||
            item.getType() == XMaterial.LEATHER_BOOTS.parseMaterial().get()) {

            LeatherArmorMeta dyedMeta = (LeatherArmorMeta) meta;

            resource.set(path + ".Dye.Red", dyedMeta.getColor().getRed());
            resource.set(path + ".Dye.Green", dyedMeta.getColor().getGreen());
            resource.set(path + ".Dye.Blue", dyedMeta.getColor().getBlue());
            resource.save();

        }

    }

    private static void serializeSkull(Resource resource, ItemStack item, ItemMeta meta, String path) {

        if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial().get()) {

            SkullMeta skullMeta = (SkullMeta) meta;

            resource.set(path + ".Skull", skullMeta.getOwner());
            resource.save();

        }

    }

    private static void serializeTippedArrows(Resource resource, ItemStack item, ItemMeta meta, String path) {

        if (Toolkit.versionToNumber() >= 19 && item.getType() == XMaterial.TIPPED_ARROW.parseMaterial().get()) {

            serializeEffects(resource, (PotionMeta) meta, path);

        }

    }

    private static void serializePotion(Resource resource, ItemStack item, ItemMeta meta, String path) {

        if (item.getType() == XMaterial.POTION.parseMaterial().get() ||
                (Toolkit.versionToNumber() >= 19 &&
                        (item.getType() == XMaterial.SPLASH_POTION.parseMaterial().get() ||
                        item.getType() == XMaterial.LINGERING_POTION.parseMaterial().get()))) {

            if (Toolkit.versionToNumber() == 18) {

                Potion potionStack = Potion.fromItemStack(item);
                PotionMeta potionMeta = (PotionMeta) meta;

                resource.set(path + ".Type", potionStack.isSplash() ? "SPLASH_POTION" : "POTION");
                resource.save();

                if (potionMeta.getCustomEffects().size() > 0) {

                    for (PotionEffect effect : potionMeta.getCustomEffects()) {

                        resource.set(path + ".Effects." + effect.getType().getName() + ".Amplifier", effect.getAmplifier() + 1);
                        resource.set(path + ".Effects." + effect.getType().getName() + ".Duration", effect.getDuration() / 20);
                        resource.save();

                    }

                } else {

                    for (PotionEffect effect : potionStack.getEffects()) {

                        resource.set(path + ".Effects." + effect.getType().getName() + ".Amplifier", effect.getAmplifier() + 1);
                        resource.set(path + ".Effects." + effect.getType().getName() + ".Duration", effect.getDuration() / 20);
                        resource.save();

                    }

                }

            } else if (Toolkit.versionToNumber() >= 19) {

                serializeEffects(resource, (PotionMeta) meta, path);

            }

        }

    }

    private static void serializeEnchantments(Resource resource, ItemStack item, String path) {

        if (item.getEnchantments().size() > 0) {

            for (Enchantment enchantment : item.getEnchantments().keySet()) {

                // possibly use XEnchantment here or uncomment code below if this doesn't work
                String enchantmentName = Toolkit.versionToNumber() < 113 ? enchantment.getName() : enchantment.getKey().getKey();
                resource.set(path + ".Enchantments." + enchantmentName + ".Level", item.getEnchantments().get(enchantment));
                resource.save();

            }

//            if (Toolkit.versionToNumber() < 113) {
//
//                for (Enchantment enchantment : item.getEnchantments().keySet()) {
//
//                    resource.set(path + ".Enchantments." + enchantment.getName() + ".Level", item.getEnchantments().get(enchantment));
//                    resource.save();
//
//                }
//
//            } else if (Toolkit.versionToNumber() >= 113) {
//
//                for (Enchantment enchantment : item.getEnchantments().keySet()) {
//
//                    resource.set(path + ".Enchantments." + enchantment.getKey().getKey() + ".Level", item.getEnchantments().get(enchantment));
//                    resource.save();
//
//                }
//
//            }

        }

    }

    private static void serializeDurability(Resource resource, ItemStack item, ItemMeta meta, String path) {

        if (Toolkit.versionToNumber() < 113) {

            if (item.getDurability() > 0 &&
                    item.getType() != XMaterial.PLAYER_HEAD.parseMaterial().get() &&
                    item.getType() != XMaterial.POTION.parseMaterial().get() &&
                    item.getType() != XMaterial.SPLASH_POTION.parseMaterial().get()) {

                resource.set(path + ".Durability", item.getDurability());
                resource.save();

            }

        } else if (Toolkit.versionToNumber() >= 113) {

            if (meta instanceof Damageable &&
                    item.getType() != XMaterial.PLAYER_HEAD.parseMaterial().get() &&
                    item.getType() != XMaterial.POTION.parseMaterial().get() &&
                    item.getType() != XMaterial.SPLASH_POTION.parseMaterial().get()) {

                Damageable damagedMeta = (Damageable) meta;

                if (damagedMeta.hasDamage()) {

                    resource.set(path + ".Durability", damagedMeta.getDamage());
                    resource.save();

                }

            }

        }

    }

}