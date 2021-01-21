package koral.sectorserver;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static koral.sectorserver.SectorServer.sendPluginMessage;

public class WeatherSync {

    public static void forwardWeather(String subchannel) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF(subchannel);
            ArrayList<Player> players = new ArrayList<>();
            for(Player player: Bukkit.getOnlinePlayers()){
                players.add(player);
            }
            for(int i =0; i<players.size(); i++){
                int randomPlayer = new Random().nextInt(players.size());
                Player player = players.get(randomPlayer);
                if(Bukkit.getServer().getWorlds().get(0).equals(player.getWorld())){ //world 0 to prawie zawsze swiat standardowy, a tylko tam sie liczy dla nas pogoda
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("time", player.getWorld().getTime());
                    jsonObject.put("hasStorm", player.getLocation().getWorld().hasStorm());
                    jsonObject.put("isThundering", player.getLocation().getWorld().isThundering());
                    String s = jsonObject.toJSONString();
                    byte[] data = s.getBytes();
                    out.writeShort(data.length);
                    out.write(data);
                    sendPluginMessage(player, b.toByteArray());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void runWeatherSync(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(SectorServer.getPlugin(), () ->{
           forwardWeather("weatherchannel");
        }, 0, 20 * 10);
    }

}
