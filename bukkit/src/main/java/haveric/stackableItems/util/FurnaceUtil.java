package haveric.stackableItems.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.Material;
import org.bukkit.inventory.*;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.fileWriter.CustomFileWriter;

public final class FurnaceUtil {

    private static StackableItems plugin;

    private static final int FUEL_LIST_VERSION = 7;

    private static List<Material> listOfFuels;
    private static List<Material> furnaceBurnables;
    private static List<Material> blastFurnaceBurnables;
    private static List<Material> smokerBurnables;

    private FurnaceUtil() { } // Private constructor for utility class

    private static CustomFileWriter fileWriter;

    public static void init(StackableItems si) {
        plugin = si;
        fileWriter = new CustomFileWriter(plugin, "fuel");
        loadDefaultList();
        reload();
    }

    private static void loadDefaultList() {
        Material[] defaultFuels = {
            Material.ACACIA_BOAT,
            Material.ACACIA_BUTTON,
            Material.ACACIA_DOOR,
            Material.ACACIA_FENCE,
            Material.ACACIA_FENCE_GATE,
            Material.ACACIA_LOG,
            Material.ACACIA_PLANKS,
            Material.ACACIA_PRESSURE_PLATE,
            Material.ACACIA_SAPLING,
            Material.ACACIA_SIGN,
            Material.ACACIA_SLAB,
            Material.ACACIA_STAIRS,
            Material.ACACIA_TRAPDOOR,
            Material.ACACIA_WOOD,
            Material.BARREL,
            Material.BAMBOO,
            Material.BIRCH_BOAT,
            Material.BIRCH_BUTTON,
            Material.BIRCH_DOOR,
            Material.BIRCH_FENCE,
            Material.BIRCH_FENCE_GATE,
            Material.BIRCH_LOG,
            Material.BIRCH_PLANKS,
            Material.BIRCH_PRESSURE_PLATE,
            Material.BIRCH_SAPLING,
            Material.BIRCH_SIGN,
            Material.BIRCH_SLAB,
            Material.BIRCH_STAIRS,
            Material.BIRCH_TRAPDOOR,
            Material.BIRCH_WOOD,
            Material.BLACK_BANNER,
            Material.BLACK_CARPET,
            Material.BLACK_WOOL,
            Material.BLAZE_ROD,
            Material.BLUE_BANNER,
            Material.BLUE_CARPET,
            Material.BLUE_WOOL,
            Material.BROWN_BANNER,
            Material.BROWN_CARPET,
            Material.BROWN_WOOL,
            Material.BOW,
            Material.BOWL,
            Material.BOOKSHELF,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.CARTOGRAPHY_TABLE,
            Material.CHARCOAL,
            Material.CHEST,
            Material.COAL,
            Material.COAL_BLOCK,
            Material.COMPOSTER,
            Material.CRAFTING_TABLE,
            Material.CYAN_BANNER,
            Material.CYAN_CARPET,
            Material.CYAN_WOOL,
            Material.DARK_OAK_BOAT,
            Material.DARK_OAK_BUTTON,
            Material.DARK_OAK_DOOR,
            Material.DARK_OAK_FENCE,
            Material.DARK_OAK_FENCE_GATE,
            Material.DARK_OAK_LOG,
            Material.DARK_OAK_PLANKS,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.DARK_OAK_SAPLING,
            Material.DARK_OAK_SIGN,
            Material.DARK_OAK_SLAB,
            Material.DARK_OAK_STAIRS,
            Material.DARK_OAK_TRAPDOOR,
            Material.DARK_OAK_WOOD,
            Material.DAYLIGHT_DETECTOR,
            Material.DEAD_BUSH,
            Material.DRIED_KELP_BLOCK,
            Material.FISHING_ROD,
            Material.FLETCHING_TABLE,
            Material.GRAY_BANNER,
            Material.GRAY_CARPET,
            Material.GRAY_WOOL,
            Material.GREEN_BANNER,
            Material.GREEN_CARPET,
            Material.GREEN_WOOL,
            Material.JUKEBOX,
            Material.JUNGLE_BOAT,
            Material.JUNGLE_BUTTON,
            Material.JUNGLE_DOOR,
            Material.JUNGLE_FENCE,
            Material.JUNGLE_FENCE_GATE,
            Material.JUNGLE_LOG,
            Material.JUNGLE_PLANKS,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.JUNGLE_SAPLING,
            Material.JUNGLE_SIGN,
            Material.JUNGLE_SLAB,
            Material.JUNGLE_STAIRS,
            Material.JUNGLE_TRAPDOOR,
            Material.JUNGLE_WOOD,
            Material.LADDER,
            Material.LAVA_BUCKET,
            Material.LIGHT_BLUE_BANNER,
            Material.LECTERN,
            Material.LIGHT_BLUE_CARPET,
            Material.LIGHT_BLUE_WOOL,
            Material.LIGHT_GRAY_BANNER,
            Material.LIGHT_GRAY_CARPET,
            Material.LIGHT_GRAY_WOOL,
            Material.LIME_BANNER,
            Material.LIME_CARPET,
            Material.LIME_WOOL,
            Material.LOOM,
            Material.MAGENTA_BANNER,
            Material.MAGENTA_CARPET,
            Material.MAGENTA_WOOL,
            Material.NOTE_BLOCK,
            Material.OAK_BOAT,
            Material.OAK_BUTTON,
            Material.OAK_DOOR,
            Material.OAK_FENCE,
            Material.OAK_FENCE_GATE,
            Material.OAK_LOG,
            Material.OAK_PLANKS,
            Material.OAK_PRESSURE_PLATE,
            Material.OAK_SAPLING,
            Material.OAK_SIGN,
            Material.OAK_SLAB,
            Material.OAK_STAIRS,
            Material.OAK_TRAPDOOR,
            Material.OAK_WOOD,
            Material.ORANGE_BANNER,
            Material.ORANGE_CARPET,
            Material.ORANGE_WOOL,
            Material.PINK_BANNER,
            Material.PINK_CARPET,
            Material.PINK_WOOL,
            Material.PURPLE_BANNER,
            Material.PURPLE_CARPET,
            Material.PURPLE_WOOL,
            Material.RED_BANNER,
            Material.RED_CARPET,
            Material.RED_MUSHROOM_BLOCK,
            Material.RED_WOOL,
            Material.SCAFFOLDING,
            Material.SMITHING_TABLE,
            Material.SPRUCE_BOAT,
            Material.SPRUCE_BUTTON,
            Material.SPRUCE_DOOR,
            Material.SPRUCE_FENCE,
            Material.SPRUCE_FENCE_GATE,
            Material.SPRUCE_LOG,
            Material.SPRUCE_PLANKS,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.SPRUCE_SAPLING,
            Material.SPRUCE_SIGN,
            Material.SPRUCE_SLAB,
            Material.SPRUCE_STAIRS,
            Material.SPRUCE_TRAPDOOR,
            Material.STICK,
            Material.STRIPPED_ACACIA_LOG,
            Material.STRIPPED_ACACIA_WOOD,
            Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_BIRCH_WOOD,
            Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_DARK_OAK_WOOD,
            Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_JUNGLE_WOOD,
            Material.STRIPPED_OAK_LOG,
            Material.STRIPPED_OAK_WOOD,
            Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_SPRUCE_WOOD,
            Material.TRAPPED_CHEST,
            Material.WHITE_BANNER,
            Material.WHITE_CARPET,
            Material.WHITE_WOOL,
            Material.WOODEN_AXE,
            Material.WOODEN_HOE,
            Material.WOODEN_PICKAXE,
            Material.WOODEN_SHOVEL,
            Material.WOODEN_SWORD,
            Material.YELLOW_BANNER,
            Material.YELLOW_CARPET,
            Material.YELLOW_WOOL,
        };
        listOfFuels = Arrays.asList(defaultFuels);
    }

    public static void reload() {
        loadRecipes();

        fileWriter.reloadFiles(FUEL_LIST_VERSION, listOfFuels);
        listOfFuels = fileWriter.getMatList();
    }

    public static boolean isFuel(Material mat) {
        return listOfFuels.contains(mat);
    }

    public static void loadRecipes() {
        furnaceBurnables = new ArrayList<>();
        blastFurnaceBurnables = new ArrayList<>();
        smokerBurnables = new ArrayList<>();

        Iterator<Recipe> iter = plugin.getServer().recipeIterator();

        while (iter.hasNext()) {
            try {
                Recipe recipe = iter.next();
                if (recipe instanceof CookingRecipe) {
                    CookingRecipe cookingRecipe = (CookingRecipe) recipe;

                    List<Material> materials = new ArrayList<>();
                    RecipeChoice choice = cookingRecipe.getInputChoice();
                    if (choice instanceof RecipeChoice.MaterialChoice) {
                        RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
                        materials.addAll(materialChoice.getChoices());

                    } else if (choice instanceof RecipeChoice.ExactChoice) {
                        RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
                        List<ItemStack> items = exactChoice.getChoices();

                        for (ItemStack item : items) {
                            materials.add(item.getType());
                        }
                    }

                    if (!materials.isEmpty()) {
                        for (Material mat : materials) {
                            if (recipe instanceof SmokingRecipe) {
                                if (!smokerBurnables.contains(mat)) {
                                    smokerBurnables.add(mat);
                                }
                            } else if (recipe instanceof BlastingRecipe) {
                                if (!blastFurnaceBurnables.contains(mat)) {
                                    blastFurnaceBurnables.add(mat);
                                }
                            } else if (recipe instanceof FurnaceRecipe) {
                                if (!furnaceBurnables.contains(mat)) {
                                    furnaceBurnables.add(mat);
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Catch any invalid Bukkit recipes
            } catch (NoSuchElementException e) {
                // Vanilla datapack is disabled
            }
        }
    }

    public static boolean isFurnaceBurnable(Material mat) {
        return furnaceBurnables.contains(mat);
    }

    public static boolean isBlastFurnaceBurnable(Material mat) {
        return blastFurnaceBurnables.contains(mat);
    }

    public static boolean isSmokerBurnable(Material mat) {
        return smokerBurnables.contains(mat);
    }
}
