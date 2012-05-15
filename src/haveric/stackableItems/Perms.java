package haveric.stackableItems;

import net.milkbowl.vault.permission.Permission;

public class Perms {
	
    private static Permission perm = null;
    
	// TODO: make dynamic?
	private static String stack = "stackableitems.stack";
	
	private static String item = "stackableitems.item";

	public static void setPerm(Permission p){
		perm = p;
	}
	
    public static Permission getPerm(){
    	return perm;
    }
    
    public static boolean permEnabled(){
    	return (perm != null);
    }
    
	public static void setStack(String newPerm){
		stack = newPerm;
	}
	
	public static String getStack(){
		return stack;
	}
	
	public static String getItem(){
		return item;
	}
}
