package koral.sectorserver.commands;

import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Cooldowns;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tpa implements CommandExecutor, TabExecutor {
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    final String tpaPrefix = "§2[§aTPA§2] §6";

    public static final Map<String, String> requestsMap = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
                if(!cooldown.hasCooldown((((Player) sender).getPlayer()), 5, tpaPrefix + "§aMusisz odczekać jeszcze chwilę zanim wyślesz propozycję o tpa ponownie")) {
                    if (!args[0].equalsIgnoreCase(player.getName())) {
                        String server = SectorServer.getPlayerServer(args[0]);
                        if (server == null)
                            player.sendMessage(tpaPrefix + "§aNiepoprawny gracz " + args[0]);
                        else {
                            SectorServer.sendToServer("Tpa", "ALL", out -> {
                                out.writeUTF(sender.getName());
                                out.writeUTF(args[0]);
                            });
                            SectorServer.msg(args[0], tpaPrefix + "§cDostałeś prośbę o teleportację od gracza " + player.getName() + " aby zaakceptować wpisz /tpaccept");
                            player.sendMessage(tpaPrefix + "§aWysłałeś prośbę o teleportację do " + args[0]);
                        }
                        cooldown.setSystemTime(player);
                    } else
                        player.sendMessage(tpaPrefix + "nie możesz teleportować się do siebie");
                }
            } else if (command.getName().equalsIgnoreCase("tpaccept")) {
                String toTp = requestsMap.get(sender.getName().toLowerCase());
                if (toTp == null)
                    sender.sendMessage(tpaPrefix + "Nie masz żadnych próśb o teleportacje");
                else {
                    sender.sendMessage(tpaPrefix + "Zaakceptowano");
                    SectorServer.sendToServer("TpaAccept", "ALL", out -> {
                        out.writeUTF(toTp);
                        out.writeUTF(sender.getName());
                    });
                }
            } else if(command.getName().equalsIgnoreCase("tpdeny")) {
                String toTp = requestsMap.get(sender.getName().toLowerCase());
                if (toTp == null)
                    sender.sendMessage(tpaPrefix + "Nie masz żadnych próśb o teleportacje");
                else {
                    sender.sendMessage(tpaPrefix + "Odrzucono");
                    SectorServer.msg(toTp, tpaPrefix + "%s odrzucił prośbę o teleportacje", ((Player) sender).getDisplayName());
                    SectorServer.sendToServer("TpaDeny", "ALL", out -> {
                        out.writeUTF(toTp);
                        out.writeUTF(sender.getName());
                    });
                }
            }
        }
        return  true;
    }

    public static void tpaLocalTimer(String player, Player target, Location lastLocation, int secondsLeft) {
        if (target.getLocation().distance(lastLocation) > 1) {
            target.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            target.removePotionEffect(PotionEffectType.CONFUSION);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            Teleport.teleport(target, target.getName(), player);
            SectorServer.msg(target, "§aPrzeteleportowano");
        } else {
            target.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(SectorServer.plugin, () -> tpaLocalTimer(player, target, lastLocation,secondsLeft - 1), 20);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
}
