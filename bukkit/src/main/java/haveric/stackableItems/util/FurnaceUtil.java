package haveric.stackableItems.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.Material;
import org.bukkit.inventory.*;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.fileWriter.CustomFileWriter;

public final class FurnaceUtil {

    private static StackableItems plugin;

    private static final int FUEL_LIST_VERSION = 8;

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
        listOfFuels = new ArrayList<>();

        listOfFuels.add(Material.ACACIA_BOAT);
        listOfFuels.add(Material.ACACIA_BUTTON);
        listOfFuels.add(Material.ACACIA_CHEST_BOAT);
        listOfFuels.add(Material.ACACIA_DOOR);
        listOfFuels.add(Material.ACACIA_FENCE);
        listOfFuels.add(Material.ACACIA_FENCE_GATE);
        listOfFuels.add(Material.ACACIA_HANGING_SIGN);
        listOfFuels.add(Material.ACACIA_LOG);
        listOfFuels.add(Material.ACACIA_PLANKS);
        listOfFuels.add(Material.ACACIA_PRESSURE_PLATE);
        listOfFuels.add(Material.ACACIA_SAPLING);
        listOfFuels.add(Material.ACACIA_SIGN);
        listOfFuels.add(Material.ACACIA_SLAB);
        listOfFuels.add(Material.ACACIA_STAIRS);
        listOfFuels.add(Material.ACACIA_TRAPDOOR);
        listOfFuels.add(Material.ACACIA_WOOD);

        listOfFuels.add(Material.BARREL);
        listOfFuels.add(Material.BAMBOO);

        listOfFuels.add(Material.BAMBOO_BLOCK);
        listOfFuels.add(Material.BAMBOO_BUTTON);
        listOfFuels.add(Material.BAMBOO_CHEST_RAFT);
        listOfFuels.add(Material.BAMBOO_DOOR);
        listOfFuels.add(Material.BAMBOO_FENCE);
        listOfFuels.add(Material.BAMBOO_FENCE_GATE);
        listOfFuels.add(Material.BAMBOO_HANGING_SIGN);
        listOfFuels.add(Material.BAMBOO_MOSAIC);
        listOfFuels.add(Material.BAMBOO_MOSAIC_SLAB);
        listOfFuels.add(Material.BAMBOO_MOSAIC_STAIRS);
        listOfFuels.add(Material.BAMBOO_PLANKS);
        listOfFuels.add(Material.BAMBOO_PRESSURE_PLATE);
        listOfFuels.add(Material.BAMBOO_RAFT);
        listOfFuels.add(Material.BAMBOO_SIGN);
        listOfFuels.add(Material.BAMBOO_SLAB);
        listOfFuels.add(Material.BAMBOO_STAIRS);
        listOfFuels.add(Material.BAMBOO_TRAPDOOR);

        listOfFuels.add(Material.BIRCH_BOAT);
        listOfFuels.add(Material.BIRCH_BUTTON);
        listOfFuels.add(Material.BIRCH_CHEST_BOAT);
        listOfFuels.add(Material.BIRCH_DOOR);
        listOfFuels.add(Material.BIRCH_FENCE);
        listOfFuels.add(Material.BIRCH_FENCE_GATE);
        listOfFuels.add(Material.BIRCH_HANGING_SIGN);
        listOfFuels.add(Material.BIRCH_LOG);
        listOfFuels.add(Material.BIRCH_PLANKS);
        listOfFuels.add(Material.BIRCH_PRESSURE_PLATE);
        listOfFuels.add(Material.BIRCH_SAPLING);
        listOfFuels.add(Material.BIRCH_SIGN);
        listOfFuels.add(Material.BIRCH_SLAB);
        listOfFuels.add(Material.BIRCH_STAIRS);
        listOfFuels.add(Material.BIRCH_TRAPDOOR);
        listOfFuels.add(Material.BIRCH_WOOD);

        listOfFuels.add(Material.BLACK_BANNER);
        listOfFuels.add(Material.BLACK_CARPET);
        listOfFuels.add(Material.BLACK_WOOL);
        listOfFuels.add(Material.BLAZE_ROD);
        listOfFuels.add(Material.BLUE_BANNER);
        listOfFuels.add(Material.BLUE_CARPET);
        listOfFuels.add(Material.BLUE_WOOL);
        listOfFuels.add(Material.BROWN_BANNER);
        listOfFuels.add(Material.BROWN_CARPET);
        listOfFuels.add(Material.BROWN_WOOL);
        listOfFuels.add(Material.BOW);
        listOfFuels.add(Material.BOWL);
        listOfFuels.add(Material.BOOKSHELF);
        listOfFuels.add(Material.BROWN_MUSHROOM_BLOCK);
        listOfFuels.add(Material.CARTOGRAPHY_TABLE);
        listOfFuels.add(Material.CHARCOAL);

        listOfFuels.add(Material.CHERRY_BOAT);
        listOfFuels.add(Material.CHERRY_BUTTON);
        listOfFuels.add(Material.CHERRY_DOOR);
        listOfFuels.add(Material.CHERRY_FENCE);
        listOfFuels.add(Material.CHERRY_FENCE_GATE);
        listOfFuels.add(Material.CHERRY_HANGING_SIGN);
        listOfFuels.add(Material.CHERRY_LOG);
        listOfFuels.add(Material.CHERRY_PLANKS);
        listOfFuels.add(Material.CHERRY_PRESSURE_PLATE);
        listOfFuels.add(Material.CHERRY_SAPLING);
        listOfFuels.add(Material.CHERRY_SIGN);
        listOfFuels.add(Material.CHERRY_SLAB);
        listOfFuels.add(Material.CHERRY_STAIRS);
        listOfFuels.add(Material.CHERRY_TRAPDOOR);
        listOfFuels.add(Material.CHERRY_WOOD);

        listOfFuels.add(Material.CHEST);
        listOfFuels.add(Material.CHISELED_BOOKSHELF);
        listOfFuels.add(Material.COAL);
        listOfFuels.add(Material.COAL_BLOCK);
        listOfFuels.add(Material.COMPOSTER);
        listOfFuels.add(Material.CRAFTING_TABLE);
        listOfFuels.add(Material.CYAN_BANNER);
        listOfFuels.add(Material.CYAN_CARPET);
        listOfFuels.add(Material.CYAN_WOOL);

        listOfFuels.add(Material.DARK_OAK_BOAT);
        listOfFuels.add(Material.DARK_OAK_BUTTON);
        listOfFuels.add(Material.DARK_OAK_CHEST_BOAT);
        listOfFuels.add(Material.DARK_OAK_DOOR);
        listOfFuels.add(Material.DARK_OAK_FENCE);
        listOfFuels.add(Material.DARK_OAK_FENCE_GATE);
        listOfFuels.add(Material.DARK_OAK_HANGING_SIGN);
        listOfFuels.add(Material.DARK_OAK_LOG);
        listOfFuels.add(Material.DARK_OAK_PLANKS);
        listOfFuels.add(Material.DARK_OAK_PRESSURE_PLATE);
        listOfFuels.add(Material.DARK_OAK_SAPLING);
        listOfFuels.add(Material.DARK_OAK_SIGN);
        listOfFuels.add(Material.DARK_OAK_SLAB);
        listOfFuels.add(Material.DARK_OAK_STAIRS);
        listOfFuels.add(Material.DARK_OAK_TRAPDOOR);
        listOfFuels.add(Material.DARK_OAK_WOOD);

        listOfFuels.add(Material.DAYLIGHT_DETECTOR);
        listOfFuels.add(Material.DEAD_BUSH);
        listOfFuels.add(Material.DRIED_KELP_BLOCK);
        listOfFuels.add(Material.FISHING_ROD);
        listOfFuels.add(Material.FLETCHING_TABLE);
        listOfFuels.add(Material.GRAY_BANNER);
        listOfFuels.add(Material.GRAY_CARPET);
        listOfFuels.add(Material.GRAY_WOOL);
        listOfFuels.add(Material.GREEN_BANNER);
        listOfFuels.add(Material.GREEN_CARPET);
        listOfFuels.add(Material.GREEN_WOOL);
        listOfFuels.add(Material.JUKEBOX);

        listOfFuels.add(Material.JUNGLE_BOAT);
        listOfFuels.add(Material.JUNGLE_BUTTON);
        listOfFuels.add(Material.JUNGLE_CHEST_BOAT);
        listOfFuels.add(Material.JUNGLE_DOOR);
        listOfFuels.add(Material.JUNGLE_FENCE);
        listOfFuels.add(Material.JUNGLE_FENCE_GATE);
        listOfFuels.add(Material.JUNGLE_HANGING_SIGN);
        listOfFuels.add(Material.JUNGLE_LOG);
        listOfFuels.add(Material.JUNGLE_PLANKS);
        listOfFuels.add(Material.JUNGLE_PRESSURE_PLATE);
        listOfFuels.add(Material.JUNGLE_SAPLING);
        listOfFuels.add(Material.JUNGLE_SIGN);
        listOfFuels.add(Material.JUNGLE_SLAB);
        listOfFuels.add(Material.JUNGLE_STAIRS);
        listOfFuels.add(Material.JUNGLE_TRAPDOOR);
        listOfFuels.add(Material.JUNGLE_WOOD);

        listOfFuels.add(Material.LADDER);
        listOfFuels.add(Material.LAVA_BUCKET);
        listOfFuels.add(Material.LIGHT_BLUE_BANNER);
        listOfFuels.add(Material.LECTERN);
        listOfFuels.add(Material.LIGHT_BLUE_CARPET);
        listOfFuels.add(Material.LIGHT_BLUE_WOOL);
        listOfFuels.add(Material.LIGHT_GRAY_BANNER);
        listOfFuels.add(Material.LIGHT_GRAY_CARPET);
        listOfFuels.add(Material.LIGHT_GRAY_WOOL);
        listOfFuels.add(Material.LIME_BANNER);
        listOfFuels.add(Material.LIME_CARPET);
        listOfFuels.add(Material.LIME_WOOL);
        listOfFuels.add(Material.LOOM);
        listOfFuels.add(Material.MAGENTA_BANNER);
        listOfFuels.add(Material.MAGENTA_CARPET);
        listOfFuels.add(Material.MAGENTA_WOOL);

        listOfFuels.add(Material.MANGROVE_BOAT);
        listOfFuels.add(Material.MANGROVE_BUTTON);
        listOfFuels.add(Material.MANGROVE_CHEST_BOAT);
        listOfFuels.add(Material.MANGROVE_DOOR);
        listOfFuels.add(Material.MANGROVE_FENCE);
        listOfFuels.add(Material.MANGROVE_FENCE_GATE);
        listOfFuels.add(Material.MANGROVE_HANGING_SIGN);
        listOfFuels.add(Material.MANGROVE_LOG);
        listOfFuels.add(Material.MANGROVE_PLANKS);
        listOfFuels.add(Material.MANGROVE_PRESSURE_PLATE);
        listOfFuels.add(Material.MANGROVE_PROPAGULE);
        listOfFuels.add(Material.MANGROVE_ROOTS);
        listOfFuels.add(Material.MANGROVE_SIGN);
        listOfFuels.add(Material.MANGROVE_SLAB);
        listOfFuels.add(Material.MANGROVE_STAIRS);
        listOfFuels.add(Material.MANGROVE_TRAPDOOR);
        listOfFuels.add(Material.MANGROVE_WOOD);

        listOfFuels.add(Material.NOTE_BLOCK);

        listOfFuels.add(Material.OAK_BOAT);
        listOfFuels.add(Material.OAK_BUTTON);
        listOfFuels.add(Material.OAK_CHEST_BOAT);
        listOfFuels.add(Material.OAK_DOOR);
        listOfFuels.add(Material.OAK_FENCE);
        listOfFuels.add(Material.OAK_FENCE_GATE);
        listOfFuels.add(Material.OAK_HANGING_SIGN);
        listOfFuels.add(Material.OAK_LOG);
        listOfFuels.add(Material.OAK_PLANKS);
        listOfFuels.add(Material.OAK_PRESSURE_PLATE);
        listOfFuels.add(Material.OAK_SAPLING);
        listOfFuels.add(Material.OAK_SIGN);
        listOfFuels.add(Material.OAK_SLAB);
        listOfFuels.add(Material.OAK_STAIRS);
        listOfFuels.add(Material.OAK_TRAPDOOR);
        listOfFuels.add(Material.OAK_WOOD);

        listOfFuels.add(Material.ORANGE_BANNER);
        listOfFuels.add(Material.ORANGE_CARPET);
        listOfFuels.add(Material.ORANGE_WOOL);
        listOfFuels.add(Material.PINK_BANNER);
        listOfFuels.add(Material.PINK_CARPET);
        listOfFuels.add(Material.PINK_WOOL);
        listOfFuels.add(Material.PURPLE_BANNER);
        listOfFuels.add(Material.PURPLE_CARPET);
        listOfFuels.add(Material.PURPLE_WOOL);
        listOfFuels.add(Material.RED_BANNER);
        listOfFuels.add(Material.RED_CARPET);
        listOfFuels.add(Material.RED_MUSHROOM_BLOCK);
        listOfFuels.add(Material.RED_WOOL);
        listOfFuels.add(Material.SCAFFOLDING);
        listOfFuels.add(Material.SMITHING_TABLE);

        listOfFuels.add(Material.SPRUCE_BOAT);
        listOfFuels.add(Material.SPRUCE_BUTTON);
        listOfFuels.add(Material.SPRUCE_CHEST_BOAT);
        listOfFuels.add(Material.SPRUCE_DOOR);
        listOfFuels.add(Material.SPRUCE_FENCE);
        listOfFuels.add(Material.SPRUCE_FENCE_GATE);
        listOfFuels.add(Material.SPRUCE_HANGING_SIGN);
        listOfFuels.add(Material.SPRUCE_LOG);
        listOfFuels.add(Material.SPRUCE_PLANKS);
        listOfFuels.add(Material.SPRUCE_PRESSURE_PLATE);
        listOfFuels.add(Material.SPRUCE_SAPLING);
        listOfFuels.add(Material.SPRUCE_SIGN);
        listOfFuels.add(Material.SPRUCE_SLAB);
        listOfFuels.add(Material.SPRUCE_STAIRS);
        listOfFuels.add(Material.SPRUCE_TRAPDOOR);

        listOfFuels.add(Material.STICK);
        listOfFuels.add(Material.STRIPPED_ACACIA_LOG);
        listOfFuels.add(Material.STRIPPED_ACACIA_WOOD);
        listOfFuels.add(Material.STRIPPED_BAMBOO_BLOCK);
        listOfFuels.add(Material.STRIPPED_BIRCH_LOG);
        listOfFuels.add(Material.STRIPPED_BIRCH_WOOD);
        listOfFuels.add(Material.STRIPPED_CHERRY_LOG);
        listOfFuels.add(Material.STRIPPED_CHERRY_WOOD);
        listOfFuels.add(Material.STRIPPED_DARK_OAK_LOG);
        listOfFuels.add(Material.STRIPPED_DARK_OAK_WOOD);
        listOfFuels.add(Material.STRIPPED_JUNGLE_LOG);
        listOfFuels.add(Material.STRIPPED_JUNGLE_WOOD);
        listOfFuels.add(Material.STRIPPED_MANGROVE_LOG);
        listOfFuels.add(Material.STRIPPED_MANGROVE_WOOD);
        listOfFuels.add(Material.STRIPPED_OAK_LOG);
        listOfFuels.add(Material.STRIPPED_OAK_WOOD);
        listOfFuels.add(Material.STRIPPED_SPRUCE_LOG);
        listOfFuels.add(Material.STRIPPED_SPRUCE_WOOD);
        listOfFuels.add(Material.TRAPPED_CHEST);
        listOfFuels.add(Material.WHITE_BANNER);
        listOfFuels.add(Material.WHITE_CARPET);
        listOfFuels.add(Material.WHITE_WOOL);
        listOfFuels.add(Material.WOODEN_AXE);
        listOfFuels.add(Material.WOODEN_HOE);
        listOfFuels.add(Material.WOODEN_PICKAXE);
        listOfFuels.add(Material.WOODEN_SHOVEL);
        listOfFuels.add(Material.WOODEN_SWORD);
        listOfFuels.add(Material.YELLOW_BANNER);
        listOfFuels.add(Material.YELLOW_CARPET);
        listOfFuels.add(Material.YELLOW_WOOL);
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
