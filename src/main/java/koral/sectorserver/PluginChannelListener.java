package koral.sectorserver;
import koral.sectorserver.listeners.PlayerRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static koral.sectorserver.listeners.PlayerJoin.randomSectorLoc;

@SuppressWarnings("unused")
public class PluginChannelListener implements PluginMessageListener {
    public static HashMap<String, Location> lokacjaGracza = new HashMap<>();
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        String subchannel = "Brak subchannelu";
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            subchannel = in.readUTF();

            Method method = PluginChannelListener.class.getDeclaredMethod(subchannel, DataInputStream.class);
            if (method == null)
                System.out.println("Brak metody obsługującej subchannel " + subchannel);
            else
                method.invoke(this, in);

        } catch(Throwable ex) {
            System.out.println("Problem z odbieraniem subchannelu " + subchannel);
            ex.printStackTrace();
        }
    }


    // Metody obsługujące informacje z subchanneli potrzebują mieć identyczną nazwę co subchannel i przymować parametr DataInputStream



    void configChannel(DataInputStream in) throws IOException {
        System.out.println(in.readUTF());
    }
    void customchannel(DataInputStream in) throws IOException, ParseException {
        short length = in.readShort();
        byte[] data = new byte[length];
        in.readFully(data);
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

    void weatherchannel(DataInputStream in) throws IOException, ParseException {
        short length = in.readShort();
        byte[] data = new byte[length];
        in.readFully(data);
        String s = new String(data);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(s);

        int time = (int) (long) jsonObject.get("time");
        boolean storm = (boolean) jsonObject.get("hasStorm");
        boolean thundering = (boolean) jsonObject.get("isThundering");

        if(Bukkit.getWorlds().get(0).getTime() != time)
            Bukkit.getWorlds().get(0).setTime(time);

        if(Bukkit.getWorlds().get(0).hasStorm() != storm)
            Bukkit.getWorlds().get(0).setStorm(storm);

        if(Bukkit.getWorlds().get(0).isThundering() != thundering)
            Bukkit.getWorlds().get(0).setThundering(thundering);

    }

    void PlayerCount(DataInputStream in) throws IOException{
        String server = in.readUTF();
        int playerCount = in.readInt();

        PlayerRespawn.spawnMap.put(server, playerCount);

    }

    void ChatChannel(DataInputStream in) throws IOException{
        if(Bukkit.getOnlinePlayers().isEmpty()) return;
        short length = in.readShort();
        byte[] data = new byte[length];
        in.readFully(data);
        String s = new String(data);
        Bukkit.broadcastMessage(s);

    }
public static Set<String> rtpPlayers = new HashSet<>();
    void RtpChannel(DataInputStream in) throws IOException{
        short length = in.readShort();
        byte[] data = new byte[length];
        in.readFully(data);
        String s = new String(data);
        Player p = Bukkit.getPlayer(s);
        if(p != null){
            p.teleport(randomSectorLoc());
            Bukkit.getScheduler().runTaskLater(SectorServer.getPlugin(), task ->{
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 500, 1));
            }, 20);
        }
        else
            rtpPlayers.add(s);
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
