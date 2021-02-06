package koral.sectorserver.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TeleportCommand implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return (List<String>) PluginChannelListener.collection;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Location loc = null;
        Player p = null;
        Player p2;
        if (sender instanceof Player)
            p = (Player) sender;

        BiFunction<Player, Integer, Location> fromCoords = (player, start) ->
                new Location(player.getWorld(), Integer.parseInt(args[start]), Integer.parseInt(args[start + 1]), Integer.parseInt(args[start + 2]));

        Function<String, Player> getPlayer = nick -> {
            try {
                return (Player) Bukkit.selectEntities(sender, nick).get(0);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        };

        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF("tpCommand");

            out.writeBoolean(p == null);
            out.writeUTF(p == null ? SectorServer.serverName : sender.getName());
            out.writeShort(args.length);

            boolean wysyłać = false;


            switch (args.length) {
                // /tp <player>
                case 1:
                    if (p == null) return false;
                    Teleport.teleport(sender, p.getName(), args[0]);
                    return true;
                // /tp <player> (player)
                case 2:
                    Teleport.teleport(sender, args[0], args[1]);
                    return true;
                // /tp <x> <y> <z>
                case 3:
                    if (p != null)
                        loc = fromCoords.apply(p, 0);
                    else
                        return false;
                    break;
                // /tp <player> <x> <y> <z>
                case 4:
                    p = getPlayer.apply(args[0]);
                    if (p == null) {
                        wysyłać = true;
                        out.writeUTF(args[0]);
                        out.writeInt(Integer.parseInt(args[1]));
                        out.writeInt(Integer.parseInt(args[2]));
                        out.writeInt(Integer.parseInt(args[3]));
                    } else
                        loc = fromCoords.apply(p, 1);
                    break;
                default:
                    return false;
            }

            if (wysyłać)
                Bukkit.getServer().sendPluginMessage(SectorServer.plugin, "BungeeCord", out.toByteArray());
            else {
                Teleport.teleport(p, loc);
                sender.sendMessage("Przeteleportowano");
            }

        } catch (ClassCastException e) {
            sender.sendMessage("Niepoprawny gracz");
        } catch (NumberFormatException e) {
            sender.sendMessage("Niepoprawne liczby");
        }


        return true;
    }
}
