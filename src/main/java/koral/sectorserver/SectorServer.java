package koral.sectorserver;

import koral.sectorserver.listeners.BlockBreak;
import koral.sectorserver.listeners.BlockPlace;
import koral.sectorserver.listeners.PlayerJoin;
import koral.sectorserver.listeners.PlayerMove;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
//TODO:żeby nie można było niszczyć kilkanaście kratek od sektora/ nie mozna bylo uzywac perel i bugowac generalnie
//TODO:Bossbar, powiadomienie, że zbliżasz się do sektora/ powiadomienie, ze zblizasz sie do granicy gdzie nie ma sektora.
//TODO:SYNCHRONIZACJA POGODY // OPCJONALNE
//TODO: ZAPISYWANIE PRZEDMIOTÓW MIĘDZY SERWERAMI - WRZUCENIE PRZEDMIOTÓW DO SQLA I ODBIERANIE JE.
//TODO: GILDIE - jest gotowy plugin na gildie open source funny guilds, dziala na sql wszystko.
//TODO: rzeczy zwiazane ze spawnem - po smierci trafia albo do bazy, albo na spawna z innego serwera.
//TODO: balancer przekierować ruch 50%/50% miedzy dwoma spawnami
//TODO: zabezpieczenie z sektorami okreslona ilosc graczy na sektor, info jezeli jest full
//TODO: Bypass na budowanie przy borderze

public final class SectorServer extends JavaPlugin implements Listener, CommandExecutor {
    public static SectorServer plugin;
    public static PluginChannelListener pcl;

    public static double shiftX;
    public static double shiftZ;
    public static Location shiftLocation(Location loc) {
        return loc.add(shiftX, 0, shiftZ);
    }

    public static String serverName;
    private static List<String> servers;
    public static int width; // szerokość pojedyńczego serwera
    public static int protectedBlocks;

    public static SectorServer getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        saveDefaultConfig();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pcl = new PluginChannelListener());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerMove(), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(), this);
        getCommand("get").setExecutor(this);

        reloadPlugin();
    }
    @Override
    public void onDisable() {
    }
//TODO przerobić tak, aby pobierało dane z jsona z SocketClient, a nie z configu.

    public static void reloadPlugin() {
        SocketClient.connectToSocketServer();
    }
    static void reloadPlugin(List<String> servers, int width, double shiftX, double shiftZ, int protectedBlocks) {
        getPlugin().reloadConfig();

        SectorServer.width = width;
        SectorServer.shiftX = shiftX;
        SectorServer.shiftZ = shiftZ;
        SectorServer.servers = servers;
        SectorServer.protectedBlocks = protectedBlocks;
        serverName = getPlugin().getConfig().getString("name"); //musi zostać, musi być w configu

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

        Bukkit.getWorlds().forEach(world -> {
            WorldBorder border = world.getWorldBorder();

            Function<Integer, Double> coord = x -> x * width + width / 2.0;

            border.setCenter(new Location(world, coord.apply(n % count), 0, coord.apply(n / count)));
            border.setSize(width + 2);
            border.setWarningDistance(0);
            border.setDamageBuffer(5);
        });

    }


    public static String getServer(int s) {
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



    public static void forwardCoordinates(String subchannel, String target, Player player) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(target);
            out.writeUTF(subchannel); // "customchannel" for example

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("world", player.getLocation().getWorld().getName());
            jsonObject.put("pitch", player.getLocation().getPitch());
            jsonObject.put("yaw", player.getLocation().getYaw());
            jsonObject.put("x", player.getLocation().getX());
            jsonObject.put("y", player.getLocation().getY());
            jsonObject.put("z", player.getLocation().getZ());

            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);

            sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        sendPluginMessage(player, byteArrayOutputStream.toByteArray());
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
