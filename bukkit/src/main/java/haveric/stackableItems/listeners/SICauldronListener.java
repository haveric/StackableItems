package haveric.stackableItems.listeners;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

public class SICauldronListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void cauldronChangeLevel(CauldronLevelChangeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            CauldronLevelChangeEvent.ChangeReason reason = event.getReason();
            Player player = (Player) entity;
            ItemStack holdingMainHand = player.getInventory().getItemInMainHand();
            ItemStack holdingMainHandClone = holdingMainHand.clone();
            Material mainHandType = holdingMainHandClone.getType();

            ItemStack holdingOffHand = player.getInventory().getItemInOffHand();
            ItemStack holdingOffHandClone = holdingOffHand.clone();
            Material offHandType = holdingOffHandClone.getType();

            int holdingMainHandMax = SIItems.getItemMax(player, mainHandType, holdingMainHandClone.getDurability(), player.getInventory().getType());
            int holdingOffHandMax = SIItems.getItemMax(player, offHandType, holdingOffHandClone.getDurability(), player.getInventory().getType());
            boolean isMainHandHoldingCustomStackSize = holdingMainHandMax != SIItems.ITEM_DEFAULT;
            boolean isOffHandHoldingCustomStackSize = holdingOffHandMax != SIItems.ITEM_DEFAULT;

            if (reason == CauldronLevelChangeEvent.ChangeReason.BOTTLE_EMPTY || reason == CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL) {
                ItemStack waterBottle = new ItemStack(Material.POTION);
                PotionMeta waterBottleMeta = (PotionMeta) waterBottle.getItemMeta();
                if (waterBottleMeta != null) {
                    waterBottleMeta.setBasePotionData(new PotionData(PotionType.WATER));
                }
                waterBottle.setItemMeta(waterBottleMeta);

                if (reason == CauldronLevelChangeEvent.ChangeReason.BOTTLE_EMPTY) {
                    if (ItemUtil.isSameItem(holdingMainHandClone, waterBottle)) {
                        if (isMainHandHoldingCustomStackSize) {
                            handleCauldronManually(event, player, holdingMainHandClone, new ItemStack(Material.GLASS_BOTTLE), EquipmentSlot.HAND);
                        }
                    } else {
                        if (isOffHandHoldingCustomStackSize) {
                            handleCauldronManually(event, player, holdingOffHandClone, new ItemStack(Material.GLASS_BOTTLE), EquipmentSlot.OFF_HAND);
                        }
                    }
                } else {
                    int potionMax = SIItems.getItemMax(player, waterBottle.getType(), waterBottle.getDurability(), player.getInventory().getType());
                    if (potionMax != SIItems.ITEM_DEFAULT) {
                        if (mainHandType == Material.GLASS_BOTTLE) {
                            handleCauldronManually(event, player, holdingMainHandClone, waterBottle.clone(), EquipmentSlot.HAND);
                        } else {
                            handleCauldronManually(event, player, holdingOffHandClone, waterBottle.clone(), EquipmentSlot.OFF_HAND);
                        }
                    }
                }
            } else if (reason == CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY) {
                if (mainHandType == Material.WATER_BUCKET || mainHandType == Material.LAVA_BUCKET || mainHandType == Material.POWDER_SNOW_BUCKET) {
                    if (isMainHandHoldingCustomStackSize) {
                        handleCauldronManually(event, player, holdingMainHandClone, new ItemStack(Material.BUCKET), EquipmentSlot.HAND);
                    }
                } else {
                    if (isOffHandHoldingCustomStackSize) {
                        handleCauldronManually(event, player, holdingOffHandClone, new ItemStack(Material.BUCKET), EquipmentSlot.OFF_HAND);
                    }
                }
            } else if (reason == CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL) {
                Block block = event.getBlock();
                Material blockType = block.getType();

                ItemStack filledBucket = null;
                if (blockType == Material.WATER_CAULDRON) {
                    filledBucket = new ItemStack(Material.WATER_BUCKET);
                } else if (blockType == Material.LAVA_CAULDRON) {
                    filledBucket = new ItemStack(Material.LAVA_BUCKET);
                } else if (blockType == Material.POWDER_SNOW_CAULDRON) {
                    filledBucket = new ItemStack(Material.POWDER_SNOW_BUCKET);
                }

                if (filledBucket != null) {
                    int filledBucketMax = SIItems.getItemMax(player, filledBucket.getType(), filledBucket.getDurability(), player.getInventory().getType());

                    if (filledBucketMax != SIItems.ITEM_DEFAULT) {
                        if (mainHandType == Material.BUCKET) {
                            handleCauldronManually(event, player, holdingMainHandClone, filledBucket.clone(), EquipmentSlot.HAND);
                        } else {
                            handleCauldronManually(event, player, holdingOffHandClone, filledBucket.clone(), EquipmentSlot.OFF_HAND);

                        }
                    }
                }
            } else if (reason == CauldronLevelChangeEvent.ChangeReason.ARMOR_WASH) {
                ItemStack washedMainClone = holdingMainHandClone.clone();
                LeatherArmorMeta leatherArmorMainMeta = (LeatherArmorMeta) washedMainClone.getItemMeta();
                if (leatherArmorMainMeta != null) {
                    if (isMainHandHoldingCustomStackSize) {
                        leatherArmorMainMeta.setColor(Bukkit.getItemFactory().getDefaultLeatherColor());
                        washedMainClone.setItemMeta(leatherArmorMainMeta);
                        handleCauldronManually(event, player, holdingMainHandClone, washedMainClone, EquipmentSlot.HAND);
                    }
                } else {
                    ItemStack washedOffhandClone = holdingOffHandClone.clone();
                    LeatherArmorMeta leatherArmorOffhandMeta = (LeatherArmorMeta) washedOffhandClone.getItemMeta();
                    if (leatherArmorOffhandMeta != null) {
                        if (isOffHandHoldingCustomStackSize) {
                            leatherArmorOffhandMeta.setColor(Bukkit.getItemFactory().getDefaultLeatherColor());
                            washedOffhandClone.setItemMeta(leatherArmorOffhandMeta);
                            handleCauldronManually(event, player, holdingOffHandClone, washedOffhandClone, EquipmentSlot.OFF_HAND);
                        }
                    }
                }
            } else if (reason == CauldronLevelChangeEvent.ChangeReason.BANNER_WASH) {
                ItemStack washedMainClone = holdingMainHandClone.clone();
                BannerMeta bannerMainMeta = (BannerMeta) washedMainClone.getItemMeta();
                if (bannerMainMeta != null) {
                    if (isMainHandHoldingCustomStackSize) {
                        bannerMainMeta.setPatterns(new ArrayList<>());
                        washedMainClone.setItemMeta(bannerMainMeta);
                        handleCauldronManually(event, player, holdingMainHandClone, washedMainClone, EquipmentSlot.HAND);
                    }
                } else {
                    ItemStack washedOffhandClone = holdingOffHandClone.clone();
                    BannerMeta bannerOffhandMeta = (BannerMeta) washedOffhandClone.getItemMeta();
                    if (bannerOffhandMeta != null) {
                        if (isOffHandHoldingCustomStackSize) {
                            bannerOffhandMeta.setPatterns(new ArrayList<>());
                            washedOffhandClone.setItemMeta(bannerOffhandMeta);
                            handleCauldronManually(event, player, holdingOffHandClone, washedOffhandClone, EquipmentSlot.OFF_HAND);
                        }
                    }
                }
            } else if (reason == CauldronLevelChangeEvent.ChangeReason.SHULKER_WASH) {
                ItemStack washedMainClone = holdingMainHandClone.clone();
                BlockStateMeta blockStateMainMeta = (BlockStateMeta) washedMainClone.getItemMeta();
                if (blockStateMainMeta != null && ItemUtil.isShulkerBox(mainHandType)) {
                    if (isMainHandHoldingCustomStackSize) {
                        washedMainClone.setType(Material.SHULKER_BOX);
                        washedMainClone.setItemMeta(blockStateMainMeta);
                        handleCauldronManually(event, player, holdingMainHandClone, washedMainClone, EquipmentSlot.HAND);
                    }
                } else {
                    ItemStack washedOffhandClone = holdingOffHandClone.clone();
                    BlockStateMeta blockStateOffhandMeta = (BlockStateMeta) washedOffhandClone.getItemMeta();
                    if (blockStateOffhandMeta != null && ItemUtil.isShulkerBox(offHandType)) {
                        if (isOffHandHoldingCustomStackSize) {
                            washedOffhandClone.setType(Material.SHULKER_BOX);
                            washedOffhandClone.setItemMeta(blockStateOffhandMeta);
                            handleCauldronManually(event, player, holdingOffHandClone, washedOffhandClone, EquipmentSlot.OFF_HAND);
                        }
                    }
                }
            }
        }
    }

    private void handleCauldronManually(CauldronLevelChangeEvent event, Player player, ItemStack holdingClone, ItemStack returnClone, EquipmentSlot slot) {
        event.setCancelled(true);

        holdingClone.setAmount(holdingClone.getAmount() - 1);
        if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(holdingClone);
        } else {
            player.getInventory().setItemInOffHand(holdingClone);
        }

        returnClone.setAmount(1);
        InventoryUtil.addItemsToPlayer(player, returnClone, "");

        Block block = event.getBlock();
        BlockState state = event.getNewState();
        block.setBlockData(state.getBlockData());

        // May not need this, but let's update just in case
        InventoryUtil.updateInventoryLater(player, 2);
    }
}
