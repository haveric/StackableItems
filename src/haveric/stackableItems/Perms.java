package haveric.stackableItems;

public class Perms {
	
	// TODO: make dynamic?
	private static String stack = "stackableitems.stack";
	
	private static String item = "stackableitems.item";

	
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
