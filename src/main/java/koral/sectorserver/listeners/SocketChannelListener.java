package koral.sectorserver.listeners;

import koral.sectorserver.ForwardChannelListener;
import koral.sectorserver.PlayerData;
import koral.sectorserver.SectorServer;
import koral.sectorserver.commands.Msg;
import koral.sectorserver.commands.Tpa;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

@SuppressWarnings("unused")
public class SocketChannelListener implements ForwardChannelListener {
    // Aysync

    static void broadcast(DataInputStream in) throws IOException {
        Bukkit.broadcastMessage(in.readUTF());
    }

    static void helpop(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String message = in.readUTF();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if(p.hasPermission("guildsaddons.helpop.receive")){
                p.sendMessage("§4§lHELPOP§7 " + playerName + " §4§l-> " + message);
            }
        });
    }

    static void spartan(DataInputStream in) throws IOException{
        String sektor = in.readUTF();
        if(sektor.equals(SectorServer.getPlugin().getConfig().getString("name"))) return;
        String notification = in.readUTF();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if(p.hasPermission("spartan.notifications")){
                p.sendMessage(notification);
            }
        });
    }

    static void remoteCommand(DataInputStream in) throws IOException {
        String cmd = in.readUTF();

        Bukkit.getScheduler().runTask(SectorServer.plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }
    static void remoteCommandFile(DataInputStream in) throws IOException {
        String path = in.readUTF();
        int len = in.readShort();
        byte[] data = new byte[len];
        in.readFully(data);

        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }

    }

    static void msg(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String msg = in.readUTF();

        SectorServer.doForNonNull(Bukkit.getPlayer(playerName), p -> p.sendMessage(msg));
    }

    static void log(DataInputStream in) throws IOException {
        System.out.println(in.readUTF());
    }


    // msgR
    static void msgUpdateRMap(DataInputStream in) throws IOException {
        String sender = in.readUTF();
        String receiver = in.readUTF();

        Msg.rMap.put(sender, receiver);
    }

    // Teleport core
    static void teleportCoreP2Loc(DataInputStream in) throws IOException, ParseException {
        String playerName = in.readUTF();
        Location loc = SectorServer.toLocation((JSONObject) new JSONParser().parse(in.readUTF()));


        Player p = Bukkit.getPlayer(playerName);
        if (p != null)
            p.teleport(loc);
        else
            PlayerJoin.lokacjaGracza.put(playerName.toLowerCase(), loc);
    }
    static void teleportCoreP2P(DataInputStream in) throws IOException {
        String player1 = in.readUTF();
        String player2 = in.readUTF();
        String server = in.readUTF();
        String sendingServer = in.readUTF();
        boolean isPlayer = in.readBoolean();
        String senderName = in.readUTF();

        SectorServer.sendToServer("teleportCoreP2P_r", server, out -> {
            out.writeUTF(player1);
            out.writeUTF(player2);
            out.writeUTF(sendingServer);
            out.writeBoolean(isPlayer);
            out.writeUTF(senderName);
        });
        SectorServer.connectAnotherServer(server, Bukkit.getPlayer(player1));
    }
    static class TeleportCoreP2P_rData {
        String player1;
        Player player2;
        String sendingServer;
        boolean isPlayer;
        String senderName;

        public TeleportCoreP2P_rData(String player1, Player player2, String sendingServer, boolean isPlayer, String senderName) {
            this.player1 = player1;
            this.player2 = player2;
            this.sendingServer = sendingServer;
            this.isPlayer = isPlayer;
            this.senderName = senderName;
        }

        public void sendMsgToSender() {
            String msg = "Przeteleportowano " + player1 + " do " + player2.getName();
            if (isPlayer)
                SectorServer.sendToServer("msg", sendingServer, out -> {
                    out.writeUTF(senderName);
                    out.writeUTF(msg);
                });
            else
                SectorServer.sendToServer("log", sendingServer, out -> out.writeUTF(msg));
        }
        public boolean teleport() {
            Player p1 = Bukkit.getPlayer(player1);
            if (p1 != null) {
                sendMsgToSender();
                p1.teleport(player2);
                return true;
            } else
                return false;
        }
    }
    static void teleportCoreP2P_r(DataInputStream in) throws IOException {
        String player1 = in.readUTF();
        String player2 = in.readUTF();
        String sendingServer = in.readUTF();
        boolean isPlayer = in.readBoolean();
        String senderName = in.readUTF();

        TeleportCoreP2P_rData data = new TeleportCoreP2P_rData(player1, Bukkit.getPlayer(player2), sendingServer, isPlayer, senderName);

        Bukkit.getScheduler().runTask(SectorServer.getPlugin(), () -> {
            if (!data.teleport())
                PlayerJoin.graczeDoTp.put(player1.toLowerCase(), data);
        });
    }

    static void Tpa(DataInputStream in) throws IOException {
        String sender = in.readUTF();
        String receiver = in.readUTF();

        Tpa.requestsMap.put(receiver.toLowerCase(), sender.toLowerCase());
    }
    static void TpaDeny(DataInputStream in) throws IOException {
        String sender = in.readUTF();
        String receiver = in.readUTF();

        Tpa.requestsMap.remove(receiver.toLowerCase(), sender.toLowerCase());
    }
    static void TpaAccept(DataInputStream in) throws IOException {
        String sender = in.readUTF();
        String receiver = in.readUTF();

        Tpa.requestsMap.remove(receiver.toLowerCase(), sender.toLowerCase());

        SectorServer.doForNonNull(Bukkit.getPlayer(sender), target ->
                Bukkit.getScheduler().runTask(SectorServer.getPlugin(), () -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 11*20, 0, false, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 11*20, 2, false, false, false));
                    Tpa.tpaLocalTimer(receiver, target, target.getLocation(), 10);
        }));
    }

    // players server map
    static void playerJoinEvent(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String server = in.readUTF();

        SectorServer.setPlayerData(playerName, new PlayerData(playerName, server));
    }
    static void playerQuitEvent(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        String server = in.readUTF();

        SectorServer.removePlayerData(playerName, new PlayerData(playerName, server));
    }

}
