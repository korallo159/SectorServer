package koral.sectorserver.commands;

import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Cooldowns;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

import static koral.sectorserver.SectorServer.msg;

//TODO: przesylac blokady na kazdy serwer.
//TODO: komenda /ignore
public class Msg implements CommandExecutor, TabExecutor {

    public static Map<String, String> rMap = new HashMap<>();
    public static Set<String> msgMute = new HashSet<>();
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return false;

        if (command.getName().equals("msgtoggle")) {
            if (msgMute.contains(sender.getName())) {
                msgMute.remove(sender.getName());
                sender.sendMessage("§c Nie ignorujesz już wiadomości innych graczy");
            } else {
                msgMute.add(sender.getName());
                sender.sendMessage("§c Ignorujesz wiadomosci innych graczy.");
            }
            return true;
        }

        if(!cooldown.hasCooldown((Player) sender, 2, "§cMusisz jeszcze chwilę odczeskać aby ponownie wyśłać wiadomość.")) {
            if (args.length < 1)
                return false;

            if (label.equalsIgnoreCase("r")) {
                String receiver = rMap.get(sender.getName());
                if (receiver == null)
                    return msg(sender, "&cNie masz komu odpisać");

                if (SectorServer.getPlayerServer(receiver) == null)
                    return msg(sender, "&c%s niedawno wszedł w tyb Offline", receiver);

                String msg = String.join(" ", args);
                SectorServer.msg(receiver, format(sender.getName(), "Ty", msg));
                return msg(sender, format("Ty", receiver, msg));
            }

            if (args.length < 2)
                return false;

            args[0] = SectorServer.getExactPlayerName(args[0]);
            if (args[0] == null)
                return msg(sender, "&cNiepoprawny gracz " + args[0]);
            if (args.length > 0) {
                if (!msgMute.contains(args[0])) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sender.getName()).append(" ");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    forwardMessageToPlayer((Player) sender, args[0], sb.toString());
                    String message = sb.toString().replaceFirst(args[0], "").replaceFirst(sender.getName(), "");

                    SectorServer.sendToServer("msgUpdateRMap", "ALL", out -> {
                        out.writeUTF(sender.getName());
                        out.writeUTF(args[0]);
                    });

                    sender.sendMessage("§6[" + "§b" + "Ty" + "§6 -> §b" + args[0] + "§6]§7" + message.replaceFirst(" ", ""));
                }
                else sender.sendMessage("§cTen gracz blokuje wysyłanie wiadomości");
            }

            cooldown.setSystemTime(((Player) sender));
        }

        return true;
    }
    public static String format(String sender, String receiver, String msg) {
        return "§6[§b" + sender + "§6 -> §b" + receiver + "§6]§7 " + msg;
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
