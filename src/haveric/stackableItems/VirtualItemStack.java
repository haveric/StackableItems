package haveric.stackableItems;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public class VirtualItemStack {

	ArrayList<ItemStack> virtualStack;
	
	public VirtualItemStack(){
		virtualStack = new ArrayList<ItemStack>();
	}
	
	private void addItemStack(ItemStack stack){
		virtualStack.add(stack);
	}
	
	private ItemStack removeLast(){
		return virtualStack.remove(virtualStack.size()-1);
	}
	
	private void removeFirst(){
		virtualStack.remove(0);
	}
	
	private ItemStack getFirst(){
		return virtualStack.get(0);
	}
}
