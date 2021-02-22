package koral.sectorserver;

import com.google.common.collect.Iterables;
import koral.sectorserver.commands.*;
import koral.sectorserver.listeners.*;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
//TODO zeby komenda z teleportowania do gildii, teleportowala do gildii

public final class SectorServer extends JavaPlugin implements Listener, CommandExecutor {
    public static class OtherServer {
        public static OtherServer s_n; // -z
        public static OtherServer s_e; // +x
        public static OtherServer s_s; // +z
        public static OtherServer s_w; // -x

        public final Integer x;
        public final Integer z;

        public final String server;

        public final boolean positive;


        public OtherServer(String server, Integer x, Integer z, boolean positive) {
            this.positive = positive;
            this.server = server;
            this.x = x;
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("OtherSErver(%s, %s, %s)", server, x, z);
        }

        public static List<OtherServer> getNonNull() {
            ArrayList<OtherServer> list = new ArrayList<>();

            for (OtherServer server : new OtherServer[] {s_n, s_e, s_s, s_w})
                doForNonNull(server, list::add);

            return list;
        }
    }

    public static SectorServer plugin;
    public static PluginChannelListener pcl;

    public static double shiftX;
    public static double shiftZ;
    public static Location shiftLocation(Location loc) {
        return loc.add(shiftX, 0, shiftZ);
    }

    public static String serverName;
    public static List<String> servers;
    public static List<String> spawns;
    public static List<String> blockedCmds;
    public static int width; // szerokość pojedyńczego serwera
    public static int protectedBlocks;
    public static int bossbarDistance;
    public static int blockedCmdsDistance = 100;

    public static SectorServer getPlugin() {
            return plugin;
        }
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        saveDefaultConfig();
        if(!getConfig().getBoolean("isOnlyForApi")) {
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getPluginManager().registerEvents(new PlayerMove(), this);
            getServer().getPluginManager().registerEvents(new PlayerRespawn(), this);
            getServer().getPluginManager().registerEvents(new BlockPlace(), this);
            getServer().getPluginManager().registerEvents(new BlockBreak(), this);
            getServer().getPluginManager().registerEvents(new PlayerCommandPreprocess(), this);
            getCommand("spawn").setExecutor(new Spawn());
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pcl = new PluginChannelListener());
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChat(), this);
        getServer().getPluginManager().registerEvents(new koral.sectorserver.listeners.PlayerDropItem(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerViolation(), this);
        getCommand("tp").setExecutor(new TeleportCommand());
        Tpa tpa = new Tpa();
        Msg msg = new Msg();
        getCommand("tpa").setExecutor(tpa);
        getCommand("tpaccept").setExecutor(tpa);
        getCommand("tpdeny").setExecutor(tpa);
        getCommand("msg").setExecutor(msg);
        getCommand("msgtoggle").setExecutor(msg);
        getCommand("remote").setExecutor(new PerformRemote());
        getCommand("remotefile").setExecutor(new PerformRemoteFile());
        getCommand("helpop").setExecutor(new helpop());
        getCommand("sockettest").setExecutor(new SocketTest());
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            PluginChannelListener.playerCompleterList.clear();
            servers.forEach(serwer -> getPlayersFromServer(serwer));
            spawns.forEach(spawn-> getPlayersFromServer(spawn));
        }, 0, 100);
        reloadPlugin();

        registerForwardChannelListener(SocketTestListener.class);
        registerForwardChannelListener(SocketChannelListener.class);

    }
    @Override
    public void onDisable() {
    }

    public static void reloadPlugin() {
        SocketClient.connectToSocketServer();
    }

    static void reloadPlugin(List<String> servers, List<String> spawns, int width, double shiftX, double shiftZ, int protectedBlocks, List<String> blockedCmds, int bossbarDistance) {
        getPlugin().reloadConfig();

        SectorServer.width = width;
        SectorServer.shiftX = shiftX;
        SectorServer.shiftZ = shiftZ;
        SectorServer.servers = servers;
        SectorServer.spawns = spawns;
        SectorServer.spawns.forEach(spawn -> PlayerRespawn.spawnMap.put(spawn, 0));
        SectorServer.protectedBlocks = protectedBlocks;
        SectorServer.blockedCmds = blockedCmds;
        SectorServer.bossbarDistance = bossbarDistance;
        serverName = getPlugin().getConfig().getString("name"); //musi zostać, musi być w configu
        boolean isWeatherForwader = getPlugin().getConfig().getBoolean("weatherForwarder");
        if(isWeatherForwader)
            WeatherSync.runWeatherSync();

        int i = -1;
        try {
            while (true)
                if (servers.get(++i).equals(serverName))
                    break;
        } catch (IndexOutOfBoundsException error) {
            SectorServer.getPlugin().getLogger().severe("Nieporawny config.yml SectorServer, \"name\" nie znajduje sie w \"servers\"");
        }

        int n = i;
        int count = serversPerSide();

        System.out.println(serverName + " " + n);
        System.out.println(n % (double) count + " " + n / count);

        Function<Integer, Double> coord = x -> x * width + width / 2.0;
        Function<World, Location> getCenter = world -> new Location(world, coord.apply(n % count) + shiftX, 0, coord.apply(n / count) + shiftZ);

        if(!SectorServer.getPlugin().getConfig().getBoolean("isOnlyForApi")) Bukkit.getWorlds().forEach(world -> {
            WorldBorder border = world.getWorldBorder();

            border.setCenter(getCenter.apply(world));
            border.setSize(width + 2);
            border.setWarningDistance(0);
            border.setDamageBuffer(5);
            border.setDamageAmount(.2);
        });

        Location center = getCenter.apply(Bukkit.getWorlds().get(0));


        OtherServer.s_n = null;
        OtherServer.s_e = null;
        OtherServer.s_s = null;
        OtherServer.s_w = null;

        if (n % serversPerSide() != serversPerSide() - 1)
            doForNonNull(getServer(n + 1), server ->
                    OtherServer.s_e = new OtherServer(server, center.getBlockX() + width / 2, null, true)); // +x

        if (n % serversPerSide() != 0)
            doForNonNull(getServer(n - 1), server ->
                    OtherServer.s_w = new OtherServer(server, center.getBlockX() - width / 2 - 1, null, false)); // -x

        if (n / serversPerSide() != serversPerSide() - 1)
            doForNonNull(getServer(n + serversPerSide()), server ->
                    OtherServer.s_s = new OtherServer(server, null, center.getBlockZ() + width / 2, true)); // +z

        if (n / serversPerSide() != 0)
            doForNonNull(getServer(n - serversPerSide()), server ->
                    OtherServer.s_n = new OtherServer(server, null, center.getBlockZ() - width / 2 - 1, false)); // -z
    }


    public static <T> void doForNonNull(T obj, Consumer<T> cons) {
            if (obj != null)
                cons.accept(obj);
    }


    public static String getServer(int s) {
        if (s < 0 || s >= servers.size())
            return null;
        return servers.get(s);
    }
    public static int serversCount() {
        return servers.size();
    }
    public static int serversPerSide() {
        return (int) Math.sqrt(serversCount());
    }


    public static void sendPluginMessage(Player player, byte[] data) {
        player.sendPluginMessage(SectorServer.getPlugin(SectorServer.class), "BungeeCord", data);
    }

    public static JSONObject toJson(Location loc) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("world", loc.getWorld().getName());
        jsonObject.put("pitch", loc.getPitch());
        jsonObject.put("yaw", loc.getYaw());
        jsonObject.put("x", loc.getX());
        jsonObject.put("y", loc.getY());
        jsonObject.put("z", loc.getZ());

        return jsonObject;
    }
    public static Location toLocation(JSONObject json) {
        Function<String, Double> coord = str -> (double) json.get(str);
        return new Location(Bukkit.getWorld((String) json.get("world")),
                coord.apply("x"), coord.apply("y"), coord.apply("z"),
                (float) (double) json.get("yaw"), (float) (double) json.get("pitch"));
    }


    public static void connectAnotherServer(String server, Player player) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTask(SectorServer.getPlugin(), () -> {
            Bukkit.getPluginManager().callEvent(new PlayerChangeSectorEvent(player, serverName, server));
            player.addScoreboardTag("isConnectingAnotherServer");

            sendPluginMessage(player, byteArrayOutputStream.toByteArray());
            // join na s2
            // quit from there
            // umiera z mimi
            // player death
            Bukkit.getScheduler().runTaskLater(SectorServer.getPlugin(), () -> player.removeScoreboardTag("isConnectingAnotherServer"), 30);
        });

    }

    public static void sendToBungeeCord(Player p, String subchannel, String message) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(subchannel);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPluginMessage(p, b.toByteArray());
    }


    public static void getPlayersFromServer(String serverName){
        if(Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        try {
            out.writeUTF("PlayerList");
            out.writeUTF(serverName);
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

            SectorServer.sendPluginMessage(player, byteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }



    // nick: server
    private static Map<String, PlayerData> playerDataMap = new HashMap<>();
    public static String getPlayerServer(String playerName) {
        PlayerData data = getPlayerData(playerName);
        return data == null ? null : data.server;
    }
    public static String getExactPlayerName(String playerName) {
        PlayerData data = getPlayerData(playerName);
        return data == null ? null : data.nick;
    }
    public static PlayerData getPlayerData(String playerName) {
        return playerDataMap.get(playerName.toLowerCase());
    }
    public static PlayerData setPlayerData(String playerName, PlayerData data) {
        return playerDataMap.put(playerName.toLowerCase(), data);
    }
    public static boolean removePlayerData(String playerName, PlayerData data) {
        return playerDataMap.remove(playerName.toLowerCase(), data);
    }

    public static boolean msg(CommandSender p, String format, String... args) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(format, args)));
        return true;
    }
    public static boolean msg(String playerName, String format, String... args) {
        String server = getPlayerServer(playerName);
        if (server != null) {
            SectorServer.sendToServer("msg", server, out -> {
                out.writeUTF(playerName);
                out.writeUTF(ChatColor.translateAlternateColorCodes('&', String.format(format, args)));
            });
            return true;
        }
        return false;
    }


    public interface DataOutputStreamConsumer {
        void accept(DataOutputStream out) throws IOException;
    }
    public static void registerForwardChannelListener(Class<? extends ForwardChannelListener> clazz) {
        SocketClient.listeners.add(clazz);
    }
    public static void sendToServer(String subchannel, String server, DataOutputStreamConsumer outConsumer) {
        try {
            DataOutputStream out = new DataOutputStream(SocketClient.socket.getOutputStream());
            out.writeUTF("forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream dataout = new DataOutputStream(b);

            outConsumer.accept(dataout);

            byte[] data = b.toByteArray();
            out.writeShort(data.length);
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



























 /*   @EventHandler
    public void onPlayerCloseBorder(PlayerCloseBorderEvent event) {
        Player player = event.getPlayer();
        if (cooldown.containsKey(player.getUniqueId().toString())) {
            if (cooldown.get(player.getUniqueId().toString()) < System.currentTimeMillis() / 1000) {

                player.sendMessage("Przenoszenie do innego sektora");
                forwardCoordinates("customchannel", "ALL", event.getPlayer());
                connectAnotherServer("testowy", event.getPlayer());

                cooldown.put(player.getUniqueId().toString(), (System.currentTimeMillis() / 1000) + 10);
            } else {
                event.getPlayer().setVelocity(new Vector(player.getLocation().getX() * -0.01, 0.66, player.getLocation().getZ() * -0.01));
                player.sendMessage("Musisz odczekac jeszcze chwile aby przeniesc sie");

            }
        } else {
            player.sendMessage("Przenoszenie do innego sektora");
            forwardCoordinates("customchannel", "ALL", event.getPlayer());
            connectAnotherServer("testowy", event.getPlayer());
            cooldown.put(player.getUniqueId().toString(), (System.currentTimeMillis() / 1000) + 10);
        }
    }
*/




/*
   private void sendBungeeMessage(Player player) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF("testsetst");
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.sendPluginMessage(SectorServer.getPlugin(), "BungeeCord", byteArrayOutputStream.toByteArray());
        player.sendMessage(ChatColor.GREEN + "Wyslano wiadomosc do proxy");
    }
 */

/*
    public void forwardString(String subchannel, String target, Player player){
        try{
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(target);
            out.writeUTF(subchannel); // "customchannel" for example
            byte[] data = "testest".getBytes();
            byte[] data2 = "datadruga".getBytes();
            out.writeShort(data.length + data2.length);
            out.write(data);
            out.write(data2);

            player.sendPluginMessage(SectorServer.getPlugin(SectorServer.class), "BungeeCord", b.toByteArray());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
*/
