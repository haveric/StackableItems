package haveric.stackableItems;


import org.bukkit.Material;

public class PlayerClickData {

	private Material type;
	private int slot;
	private int amount;
	private int dur;

	public PlayerClickData(){
		slot = -1;
		amount = -1;
		type = null;
	}
	
	public PlayerClickData(int slot, Material type, int amt, int dur){
		this.slot = slot;
		this.type = type;
		amount = amt;
	}

	public Material getType() {
		return type;
	}

	public void setType(Material type) {
		this.type = type;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public int getDur(){
		return dur;
	}
}
