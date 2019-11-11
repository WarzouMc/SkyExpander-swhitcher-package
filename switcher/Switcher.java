package fr.WarzouMc.SkyExpanderInternalPlugin.utils.plugin.switcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.WarzouMc.SkyExpanderInternalPlugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.TreeMap;

public class Switcher {

    /**
     * 0 -> staff
     * 1 -> player
     */

    private Main main;
    private String playerName;
    private Player player;
    private Location location;

    private String filePath;

    private File file;
    private JSONObject jsonObject;
    private JSONParser jsonParser = new JSONParser();

    private HashMap<String, Object> defaults = new HashMap<>();

    public Switcher(Main main, Player player, int value){
        this.main = main;
        this.playerName = player.getName();

        this.filePath = main.getDataFolder() + File.separator + "switch/";
        this.file = new File(filePath + playerName + ".json");

        this.player = player;
        this.location = player.getLocation();

        if (player.getEffectivePermissions().contains("skyexpander.staff")){
            reload();
            if (value == 0){
                switching();
                save();
            }
        }
    }

    private void reload() {
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        try {
            if (!file.exists()){
                PrintWriter printWriter = new PrintWriter(file, "UTF-8");
                printWriter.print("{");
                printWriter.print("}");
                printWriter.flush();
                printWriter.close();
                defaults.put("mod", 0);
                defaults.put("invertPlayer", invertPlayer());
            }
            this.jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void switching(){
        defaults.put("invertPlayer", invertPlayer());
        changeMod();
        PlayerConstructor playerConstructor = new PlayerConstructor(this.player);
        playerConstructor.construct(jsonObject);
    }

    @SuppressWarnings("unchecked")
    public JSONObject invertPlayer(){
        JSONObject object = new JSONObject();
        PlayerConstructor playerConstructor = new PlayerConstructor(this.player);
        object.put("content", playerConstructor.inventoryContents());
        object.put("location", playerConstructor.locationObject());
        object.put("gm", playerConstructor.gm());
        object.put("stat", playerConstructor.stat());
        return object;
    }

    public void changeMod(){
        defaults.put("mod", getMod() == 0 ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public void save(){
        try {
            JSONObject toSave = jsonObject;

            for (String string : defaults.keySet()){
                Object o = defaults.get(string);
                if (o instanceof JSONObject){
                    toSave.put(string, getObject(string));
                } else if (o instanceof Integer){
                    toSave.put(string, getInteger(string));
                } else if (o instanceof String) {
                    toSave.put(string, StandardCharsets.UTF_8.encode(getString(string)));
                }
            }

            TreeMap<String, Object> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            treeMap.putAll(toSave);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String pettryJsonString = gson.toJson(treeMap);

            Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), "UTF-8"));
            fileWriter.write(pettryJsonString);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRawData(String key) {
        return defaults.containsKey(key) ? defaults.get(key).toString()
                : (jsonObject.containsKey(key) ? jsonObject.get(key).toString() : key);
    }

    public String getString(String key) {
        return ChatColor.translateAlternateColorCodes('&', getRawData(key));
    }

    public double getInteger(String key) {
        return Double.parseDouble(getRawData(key));
    }

    private JSONObject getObject(String key) {
        return defaults.containsKey(key) ? (JSONObject) defaults.get(key)
                : (jsonObject.containsKey(key) ? (JSONObject) jsonObject.get(key) : new JSONObject());
    }

    public int getMod(){
        if (player.getEffectivePermissions().contains("skyexpander.staff")){
            return (int) getInteger("mod");
        }
        return 1;
    }
}
