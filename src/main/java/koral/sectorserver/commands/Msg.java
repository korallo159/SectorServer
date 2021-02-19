package koral.sectorserver.commands;

import com.google.common.collect.Iterables;
import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Cooldowns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Msg implements CommandExecutor, TabExecutor {


    public static Set<String> msgMute = new HashSet<>();
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2)
            return false;

        if(!(sender instanceof Player)) return false;
//TODO: przesylac blokady na kazdy serwer.
//TODO: komenda /ignore
        if(!cooldown.hasCooldown((Player) sender, 2, "§cMusisz jeszcze chwilę odczeskać aby ponownie wyśłać wiadomość.")) {
            if (command.getName().equalsIgnoreCase("msgtoggle")) {
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
                if (!msgMute.contains(args[0])) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sender.getName()).append(" ");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    forwardMessageToPlayer((Player) sender, args[0], sb.toString());
                    String message = sb.toString().replaceFirst(args[0], "").replaceFirst(sender.getName(), "");

                    sender.sendMessage("§6[" + "§b" + "Ty" + "§6 -> §b" + args[0] + "§6]§7" + message.replaceFirst(" ", ""));
                }
                else sender.sendMessage("§cTen gracz blokuje wysyłanie wiadomości");
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



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

            return PluginChannelListener.playerCompleterList;

    }
}
