package fr.WarzouMc.SkyExpanderInternalPlugin.utils.plugin.switcher;

import fr.WarzouMc.SkyExpanderInternalPlugin.utils.fileConfiguration.LocationFrom;
import fr.WarzouMc.SkyExpanderInternalPlugin.utils.graphics.itemBuilder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PlayerConstructor {

    private Player player;
    private JSONObject jsonObject;
    public PlayerConstructor(Player player){
        this.player = player;
    }

    public JSONArray inventoryContents(){
        JSONArray jsonArray = new JSONArray();
        ItemStack[] itemStacks = player.getInventory().getContents();

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                ItemBuilder itemBuilder = new ItemBuilder(item);
                JSONObject jsonObject = itemBuilder.toJSONObject(i);
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
    }

    public String gm(){
        return String.valueOf(player.getGameMode());
    }

    public double[] location(){
        Location location = player.getLocation();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double yaw = location.getYaw();
        double pitch = location.getPitch();

        return new double[] {x,y,z,yaw,pitch};
    }

    public String world(){
        return player.getLocation().getWorld().getName();
    }

    public JSONObject locationObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("world", world());
        jsonObject.put("loc", location());
        return jsonObject;
    }

    public JSONObject stat(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("health", player.getHealth());
        jsonObject.put("food", player.getFoodLevel());
        jsonObject.put("saturation", player.getSaturation());
        jsonObject.put("maxhealth", player.getMaxHealth());
        jsonObject.put("exp", player.getExp());
        jsonObject.put("level", player.getLevel());
        return jsonObject;
    }

    public void construct(JSONObject jsonObject){
        this.jsonObject = jsonObject;
        player.getInventory().clear();
        constructInventory();
        constructStat();
        constructLocation();
        constructGm();
    }

    private void constructGm() {
        if (jsonObject.containsKey("invertPlayer")) {
            JSONObject invertPlayer = (JSONObject) jsonObject.get("invertPlayer");
            if (invertPlayer.containsKey("gm")){
                player.setGameMode(GameMode.valueOf((String) invertPlayer.get("gm")));
            }
        }
    }

    private void constructLocation() {
        if (jsonObject.containsKey("invertPlayer")){
            JSONObject invertPlayer = (JSONObject) jsonObject.get("invertPlayer");
            if (invertPlayer.containsKey("location")){
                JSONObject location = (JSONObject) invertPlayer.get("location");
                try {
                    String world = (String) location.get("world");
                    JSONArray locCord = (JSONArray) location.get("loc");
                    Location loc = new LocationFrom().locationFromJSONArray(world, locCord);
                    player.teleport(loc);
                } catch (NullPointerException ignored){}
            }
        }
    }

    private void constructStat() {
        if (jsonObject.containsKey("invertPlayer")){
            JSONObject invertPlayer = (JSONObject) jsonObject.get("invertPlayer");
            if (invertPlayer.containsKey("stat")){
                JSONObject stat = (JSONObject) invertPlayer.get("stat");
                if (stat.containsKey("health")) player.setHealth((Double) stat.get("health"));
                if (stat.containsKey("food")){
                    long l = (long) stat.get("food");
                    player.setFoodLevel((int)l);
                }
                if (stat.containsKey("saturation")){
                    player.setSaturation((float) ((Double) stat.get("saturation") + 0.0f));
                }
                if (stat.containsKey("maxhealth")){
                    player.setMaxHealth((Double) stat.get("maxhealth"));
                }
                if (stat.containsKey("level")){
                    long l = (long) stat.get("level");
                    player.setLevel((int) l);
                }
                if (stat.containsKey("exp")){
                    player.setExp((float) ((Double) stat.get("exp") + 0.0f));
                }
            }
        }
    }

    private void constructInventory() {
        if (jsonObject.containsKey("invertPlayer")){
            JSONObject invertPlayer = (JSONObject) jsonObject.get("invertPlayer");
            if (invertPlayer.containsKey("content")){
                JSONArray content = (JSONArray) invertPlayer.get("content");
                for (Object o : content) {
                    Bukkit.broadcastMessage(((JSONObject)o).toJSONString());
                    ItemBuilder itemBuilder = new ItemBuilder((JSONObject) o);
                    player.getInventory().setItem((int) itemBuilder.getPosition(), itemBuilder.toItemStack());
                }
            }
        }
    }

}
