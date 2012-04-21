package haveric.stackableItems;

import org.bukkit.Material;

public class ToolUtil {
	
	public static boolean isTool(Material mat){
		boolean isTool = false;
		
		switch(mat){
			case WOOD_AXE:
			case WOOD_HOE:
			case WOOD_PICKAXE:
			case WOOD_SPADE:
			case WOOD_SWORD:
			case STONE_AXE:
			case STONE_HOE:
			case STONE_PICKAXE:
			case STONE_SPADE:
			case STONE_SWORD:
			case IRON_AXE:
			case IRON_HOE:
			case IRON_PICKAXE:
			case IRON_SPADE:
			case IRON_SWORD:
			case GOLD_AXE:
			case GOLD_HOE:
			case GOLD_PICKAXE:
			case GOLD_SPADE:
			case GOLD_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_HOE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SPADE:
			case DIAMOND_SWORD:
				isTool = true;
				break;
				
			default:
				break;
		}
		
		return isTool;
	}
}
