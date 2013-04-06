package haveric.stackableItems.fileWriter;

import haveric.stackableItems.StackableItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Material;

public class CustomFileWriter {
    private StackableItems plugin;

    private File defaultFile;
    private File customFile;

    private File dataFolder;

    private List<Material> matList;

    private String fileName;

    public CustomFileWriter(StackableItems si, String name) {
        plugin = si;
        fileName = name;
        reload();
    }

    public void reload() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File lists = new File(getDataFolder() + "/lists");
        if (!lists.exists()) {
            lists.mkdir();
        }
    }

    public void reloadFiles(int version, List<Material> list) {
        defaultFile = new File(getDataFolder() + "/lists/default" + fileName + ".txt");
        customFile = new File(getDataFolder() + "/lists/custom" + fileName + ".txt");

        createFiles(version, list);
    }

    private void createFiles(int version, List<Material> list) {
        if (!defaultFile.exists()) {
            try {
                defaultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!customFile.exists()) {
            try {
                customFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Scanner defaultScanner = new Scanner(defaultFile);

            if (defaultFile.length() > 0) {
                defaultScanner.next();
                int fileVersion = defaultScanner.nextInt();
                if (fileVersion < version) {
                    defaultFile.delete();
                    defaultFile = new File(getDataFolder() + "/lists/default" + fileName + ".txt");
                    writeList(list, defaultFile, version);
                }
            } else {
                writeList(list, defaultFile, version);
            }
            defaultScanner.close();

            Scanner listScanner = new Scanner(defaultFile);
            matList = new ArrayList<Material>();

            listScanner.next();
            listScanner.nextInt();
            while (listScanner.hasNextLine()) {
                matList.add(Material.getMaterial(listScanner.nextLine()));
            }

            listScanner.close();
        } catch (FileNotFoundException e) {
            plugin.log.warning("default" + fileName + ".txt not found.");
            e.printStackTrace();
        }

        try {
            Scanner customScanner = new Scanner(customFile);

            if (customFile.length() > 0) {
                matList = new ArrayList<Material>();
                while (customScanner.hasNextLine()) {
                    matList.add(Material.getMaterial(customScanner.nextLine()));
                }
            }

            customScanner.close();
        } catch (FileNotFoundException e) {
            plugin.log.warning("custom" + fileName + ".txt not found.");
            e.printStackTrace();
        }
    }

    private void writeList(List<Material> list, File f, int version) {
        try {
            FileWriter fstream = new FileWriter(f);
            PrintWriter out = new PrintWriter(fstream);
            out.println("Version: " + version);

            for (Material mat : list) {
                out.println(mat.name());
            }

            out.close();
            fstream.close();
        } catch (IOException e) {
            plugin.log.warning(String.format("File %s not found.", f.getName()));
        }
    }

    private File getDataFolder() {
        if (dataFolder == null) {
            dataFolder = plugin.getDataFolder();
        }

        return dataFolder;
    }

    public List<Material> getMatList() {
        return matList;
    }
}
