package haveric.stackableItems;


import org.bukkit.Location;
import org.bukkit.Material;

public class PlayerClickData {

    private Material type;
    private int slot;
    private int amount;
    private Material lastBlock;
    private Location lastBlockLoc;
    private double lastFoodLevel;

    public PlayerClickData() {
        slot = -1;
        amount = -1;
        type = null;
        lastBlock = null;
        lastBlockLoc = null;
        lastFoodLevel = -1;
    }

    public PlayerClickData(int newSlot, Material newType, int newAmt/*, int newDur*/) {
        slot = newSlot;
        type = newType;
        amount = newAmt;
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

    public Material getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(Material mat) {
        lastBlock = mat;
    }

    public Location getLastBlockLocation() {
        return lastBlockLoc;
    }

    public void setLastBlockLocation(Location loc) {
        lastBlockLoc = loc;
    }

    public double getLastFoodLevel() {
        return lastFoodLevel;
    }

    public void setLastFoodLevel(double foodLevel) {
        lastFoodLevel = foodLevel;
    }
}
