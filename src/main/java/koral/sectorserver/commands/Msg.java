package koral.sectorserver.commands;

import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Cooldowns;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Msg implements CommandExecutor {


    public static Set<String> msgMute = new HashSet<>();
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return false;
//TODO: Pobrac liste graczy z calej sieci
//TODO: przesylac blokady na kazdy serwer.
//TODO: komenda /ignore
        if(!cooldown.hasCooldown((Player) sender, 5)) {
            if (args[0].equalsIgnoreCase("toggle")) {
                if (msgMute.contains(sender.getName())) {
                    msgMute.remove(sender.getName());
                    sender.sendMessage("§c Nie ignorujesz już wiadomości innych graczy");
                } else {
                    msgMute.add(sender.getName());
                    sender.sendMessage("§c Ignorujesz wiadomosci innych graczy.");
                }
                return true;
            }

            if (args.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(sender.getName()).append(" ");
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                forwardMessageToPlayer((Player) sender, args[0], sb.toString());
                sender.sendMessage( "§6[" + "§b" + "Ty" + "§6 -> §b" + args[0]  + "§6]§7" + sb.toString().replace(args[0], ""));
            }
            cooldown.setSystemTime(((Player) sender));
        }

        return true;
    }

    private void forwardMessageToPlayer(Player player, String target, String message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("ForwardToPlayer");
            out.writeUTF(target);
            out.writeUTF("MsgChannel"); // "customchannel" for example

            byte[] data = message.getBytes();
            out.writeShort(data.length);
            out.write(data);

            SectorServer.sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
