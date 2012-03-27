package haveric.stackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Material;

public class ToolConfig {
	static StackableItems plugin;
	
    private static File defaultTools;
    private static File customTools;
    
    private static ArrayList<Material> tools;
    private static final int TOOLS_VERSION = 1;

    /**
     * Initializes the config file
     */
    public static void init(StackableItems si){
    	plugin = si;
    	defaultTools = new File(plugin.getDataFolder() + "/defaultTools.txt");
    	customTools = new File(plugin.getDataFolder() + "/customTools.txt");
    	
    	tools = new ArrayList<Material>();
    	
    	loadToolList();
    }
    
    private static void loadToolList(){
    	if (!defaultTools.exists()){
			try {
				defaultTools.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!customTools.exists()){
			try {
				customTools.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			Scanner sc = new Scanner(defaultTools);
			
			if (defaultTools.length() > 0){
				sc.next();
				int fileVersion = sc.nextInt();
				if (fileVersion < TOOLS_VERSION){
					defaultTools.delete();
					defaultTools = new File(plugin.getDataFolder() + "/defaultTools.txt");
					writeTools(defaultTools, true);
				}
			} else {
				writeTools(defaultTools, true);
			}
			sc.close();
			
			Scanner sc2 = new Scanner(defaultTools);
			tools = new ArrayList<Material>();
			sc2.next();
			sc2.nextInt();
			while(sc2.hasNextLine()){
				tools.add(Material.getMaterial(sc2.nextLine()));
			}
			
			sc2.close();
		} catch (FileNotFoundException e) {
			plugin.log.warning(String.format("[%s] defaultTools.txt not found." , plugin.getDescription().getName()));
			e.printStackTrace();
		}
		

		try {
			Scanner sc3 = new Scanner(customTools);
			
			if (customTools.length() > 0){
				tools = new ArrayList<Material>();
				while(sc3.hasNextLine()){
					tools.add(Material.getMaterial(sc3.nextLine()));
				}
			}
			
			sc3.close();
		} catch (FileNotFoundException e) {
			plugin.log.warning(String.format("[%s] customTools.txt not found." , plugin.getDescription().getName()));
			e.printStackTrace();
		}
		
    }

	private static void writeTools(File f, boolean vers) {
		try {
			FileWriter fstream = new FileWriter(f);
			PrintWriter out = new PrintWriter(fstream);
			if (vers){
				out.println("Version: " + TOOLS_VERSION);
			}
			
			out.println("WOOD_AXE");
			out.println("WOOD_HOE");
			out.println("WOOD_PICKAXE");
			out.println("WOOD_SPADE");
			out.println("WOOD_SWORD");
			
			out.println("STONE_AXE");
			out.println("STONE_HOE");
			out.println("STONE_PICKAXE");
			out.println("STONE_SPADE");
			out.println("STONE_SWORD");
			
			out.println("IRON_AXE");
			out.println("IRON_HOE");
			out.println("IRON_PICKAXE");
			out.println("IRON_SPADE");
			out.println("IRON_SWORD");
			
			out.println("GOLD_AXE");
			out.println("GOLD_HOE");
			out.println("GOLD_PICKAXE");
			out.println("GOLD_SPADE");
			out.println("GOLD_SWORD");
			
			out.println("DIAMOND_AXE");
			out.println("DIAMOND_HOE");
			out.println("DIAMOND_PICKAXE");
			out.println("DIAMOND_SPADE");
			out.println("DIAMOND_SWORD");
			
			out.close();
			fstream.close();
		} catch (IOException e) {
			plugin.log.warning(String.format("[%s] File %s not found." , plugin.getDescription().getName(),f.getName()));
		}
	}
	
	public static boolean isTool(Material mat){
		boolean isTool = false;
		
		if (tools.contains(mat)){
			isTool = true;
		}
		
		return isTool;
	}
}
