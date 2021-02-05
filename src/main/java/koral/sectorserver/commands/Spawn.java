package koral.sectorserver.commands;

import koral.sectorserver.SectorServer;
import koral.sectorserver.listeners.PlayerRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Spawn implements CommandExecutor {
    PlayerRespawn playerRespawn = new PlayerRespawn();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 11*20, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 11*20, 2, false, false, false));
        spawnTimer(player, player.getLocation(), 10);
        }
        return true;
    }

    private void spawnTimer(Player sender, Location lastLocation, int secondsLeft) {
        if (sender.getLocation().distance(lastLocation) > 1) {
            sender.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            sender.removePotionEffect(PotionEffectType.CONFUSION);
            sender.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            SectorServer.connectAnotherServer(playerRespawn.getLessLoadedSpawn(sender),sender);
        } else {
            sender.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(SectorServer.getPlugin(), () -> spawnTimer(sender, lastLocation,secondsLeft - 1), 20);
        }
    }
}
