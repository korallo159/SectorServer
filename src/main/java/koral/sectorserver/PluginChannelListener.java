package koral.sectorserver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.HashMap;


public class PluginChannelListener implements PluginMessageListener {
    public static HashMap<String, Location> lokacjaGracza = new HashMap<>();
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        try{
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();
            if (subchannel.equals("customchannel")) {
                short length = in.readShort();
                byte[] data = new byte[length];
                in.readFully(data);

                String s = new String(data);
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(s);

                String playername = (String) jsonObject.get("player");
                Location location = new Location(Bukkit.getWorld((String) jsonObject.get("world")),
                        (double)  jsonObject.get("x"),(double) jsonObject.get("y"),(double) jsonObject.get("z"));
                lokacjaGracza.put(playername, location);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


/*    @Override
    public synchronized void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(message);
        String input = byteArrayDataInput.readUTF();
        SectorServer.getPlugin(SectorServer.class).getLogger().info(input);
    }
 */

}
