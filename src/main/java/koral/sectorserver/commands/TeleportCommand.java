package koral.sectorserver.commands;

import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TeleportCommand implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> match = new ArrayList<>();
        if(args.length == 1){
            String search = args[0].toLowerCase();
            for(String player : PluginChannelListener.playerCompleterList){
                if(player.toLowerCase().startsWith(search)){
                    match.add(player);
                }
            }
        }
        return match;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Location loc = null;
        Player p = null;
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
                    //TODO: usunięte, przywrócić
                    break;
                default:
                    return false;
            }

            Teleport.teleport(p, loc);
            sender.sendMessage("Przeteleportowano");

        } catch (ClassCastException e) {
            sender.sendMessage("Niepoprawny gracz");
        } catch (NumberFormatException e) {
            sender.sendMessage("Niepoprawne liczby");
        }


        return true;
    }
}
