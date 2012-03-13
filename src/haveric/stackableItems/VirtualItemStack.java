package haveric.stackableItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class VirtualItemStack {

	ArrayList<ItemStack> virtualStack;
	
	public VirtualItemStack(){
		virtualStack = new ArrayList<ItemStack>();
	}
	
	public VirtualItemStack(ArrayList<ItemStack> items){
		if (items == null){
			virtualStack = new ArrayList<ItemStack>();
		} else {
			virtualStack = items;
		}
	}
	/*
	public VirtualItemStack(List<String> items) {
		int size = items.size();
		String parts[];
		for (int i = 0; i < size; i++){
			parts = items.get(i).split(",");
			Material mat = Material.getMaterial(parts[0]);
			int data = Integer.valueOf(parts[1]);
			int dur = Integer.valueOf(parts[2]);
			
			HashMap<Enchantment, Integer> enchants = convertStringToMap(parts[3]);
			ItemStack stack = new ItemStack(mat, data, (short)dur);
			stack.addUnsafeEnchantments(enchants);
			virtualStack.add(stack);
		}
	}

	private HashMap<Enchantment, Integer> convertStringToMap(String string) {
		HashMap<Enchantment, Integer> actualEnchants = new HashMap<Enchantment, Integer>();
		String[] enchants = string.split("|");
		int enchantAmount = enchants.length;
		for (int i = 0; i < enchantAmount; i++){
			String[] singleEnchant = enchants[i].split("-");
			Enchantment e = Enchantment.getById(Integer.parseInt(singleEnchant[0]));
			actualEnchants.put(e, Integer.parseInt(singleEnchant[1]));	
		}
		return actualEnchants;
	}

*/
	public void addItemStack(ItemStack stack){
		virtualStack.add(stack);
	}
	
	public void addToFront(ItemStack stack){
		virtualStack.add(0, stack);
	}
	
	public ItemStack removeLast(){
		return virtualStack.remove(virtualStack.size()-1);
	}
	
	public void removeFirst(){
		virtualStack.remove(0);
	}
	
	public ItemStack getFirst(){
		return virtualStack.get(0);
	}

	public ArrayList<ItemStack> getList() {
		return virtualStack;
	}
	
	public boolean isEmpty(){
		return virtualStack == null;
	}
}
