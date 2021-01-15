package koral.sectorserver;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.function.Function;


public class PluginChannelListener implements PluginMessageListener {
    public static HashMap<String, Location> lokacjaGracza = new HashMap<>();
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        System.out.println("Jest i on");
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
                System.out.println("Pan pakiet");
                String s = new String(data);
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(s);

                Function<String, Double> coord = str -> (double) jsonObject.get(str);

                String playername = (String) jsonObject.get("player");
                Location location = new Location(Bukkit.getWorld((String) jsonObject.get("world")),
                        coord.apply("x"), coord.apply("y"), coord.apply("z"),
                        (float) (double) jsonObject.get("yaw"), (float) (double) jsonObject.get("pitch"));

                Player p = Bukkit.getPlayer(playername);
                if (p != null)
                    p.teleport(location);
                else
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
