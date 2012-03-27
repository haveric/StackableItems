package haveric.stackableItems;

import java.util.ArrayList;
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
		return virtualStack == null || virtualStack.size() == 0;
	}
}
