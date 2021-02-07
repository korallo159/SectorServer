package koral.sectorserver.commands;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;

import static koral.sectorserver.SectorServer.connectAnotherServer;
import static koral.sectorserver.SectorServer.sendPluginMessage;

public class Tpa implements CommandExecutor, TabExecutor {
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    HashMap<String, String> localTpMap = new HashMap<>();
    final String tpaPrefix = "§2[§aTPA§2] ";
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
                if(!cooldown.hasCooldown((((Player) sender).getPlayer()), 5, tpaPrefix + "§aMusisz odczekać jeszcze chwilę zanim wyślesz propozycję o tpa ponownie")) {
                    if (Bukkit.getPlayer(args[0]) != null && !args[0].equalsIgnoreCase(player.getName())) {
                        localTpMap.put(args[0].toLowerCase(), player.getName());
                        player.sendMessage(tpaPrefix + "§aWysłałeś prośbę o teleportację do " + args[0]);
                        Bukkit.getPlayer(args[0]).sendMessage(tpaPrefix + "§cDostałeś prośbę o teleportację od gracza " + player.getName() +
                                " aby zaakceptować wpisz /tpaccept");
                        return true;
                    } else {
                        sendTpaRequest("TpaChannel", "ALL", (Player) sender, args[0]);
                        player.sendMessage(tpaPrefix + "§aWysłałeś prośbę o teleportację do " + args[0]);
                    }
                    cooldown.setSystemTime(player);
                }
            }

            if(command.getName().equalsIgnoreCase("tpaccept")) {
                if (localTpMap.containsKey(player.getName().toLowerCase())) {
                    Player target = Bukkit.getPlayer(localTpMap.remove(player.getName().toLowerCase()));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 11*20, 0, false, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 11*20, 2, false, false, false));
                    tpaLocalTimer(player, target, target.getLocation(), 10);
                }
                else if (PluginChannelListener.tpaMap.containsKey(sender.getName().toLowerCase())) {
                    sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName().toLowerCase()), true);
                    sender.sendMessage(tpaPrefix + "§aZaakceptowałes prośbę o teleportacje, gracz pojawi się obok Ciebie");
                    PluginChannelListener.tpaMap.remove(sender.getName());
                } else sender.sendMessage(tpaPrefix + "§cNie posiadasz żadnej prośby o teleportacje!");
            }
            if(command.getName().equalsIgnoreCase("tpdeny")){
                if(localTpMap.containsKey(player.getName().toLowerCase())) {
                    localTpMap.remove(player.getName().toLowerCase());
                    sender.sendMessage(tpaPrefix + "§cOdrzuciłeś prośbę o teleportację");
                }
                else if(PluginChannelListener.tpaMap.containsKey(sender.getName().toLowerCase())) {
                    sendTpaAcceptation("TpaChannel", "ALL", (Player) sender, PluginChannelListener.tpaMap.get(sender.getName().toLowerCase()), false);
                    sender.sendMessage(tpaPrefix + "§cOdrzuciłeś prośbę o teleportację");
                }
                else sender.sendMessage(tpaPrefix + "§cNie posiadasz żadnej prośby o teleportacje!");
            }
        }
        return  true;
    }

    private void tpaLocalTimer(Player player, Player target, Location lastLocation, int secondsLeft) {
        if (target.getLocation().distance(lastLocation) > 1) {
            target.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            target.removePotionEffect(PotionEffectType.CONFUSION);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            target.teleport(player);
        } else {
            target.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(SectorServer.plugin, () -> tpaLocalTimer(player, target, lastLocation,secondsLeft - 1), 20);
        }
    }

    public static void sendPlayerTpInfo(String subchannel, String server, Player player, String target ) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel); // "customchannel" for example

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            jsonObject.put("accepted", true);
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);

            sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendTpaRequest(String subchannel, String server, Player player, String target) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);
            sendPluginMessage(player, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTpaAcceptation(String subchannel, String server, Player player, String target, boolean accept) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("player", player.getName());
            jsonObject.put("target", target);
            jsonObject.put("accept", accept);
            jsonObject.put("server", SectorServer.getPlugin().getConfig().getString("name"));
            String s = jsonObject.toJSONString();
            byte[] data = s.getBytes();
            out.writeShort(data.length);
            out.write(data);
            sendPluginMessage(player, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tpaTimer(String server, Player player, JSONObject jsonObject, Location lastLocation, int secondsLeft) {
        if (player.getLocation().distance(lastLocation) > 1) {
            player.sendMessage("§cPoruszyłeś się! Teleportacja anulowana.");
            player.removePotionEffect(PotionEffectType.CONFUSION);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (secondsLeft <= 0) {
            sendPlayerTpInfo("TpaChannel", server, player, jsonObject.get("player").toString());
            connectAnotherServer(server, player);
        } else {
            player.sendTitle("§5§lTeleportacja", "§9Nie ruszaj się, za §a " + secondsLeft + "s §9 zostaniesz przeteleportowany", 0, 22, 5);
            Bukkit.getScheduler().runTaskLater(SectorServer.plugin, () -> tpaTimer(server, player, jsonObject, lastLocation,secondsLeft - 1), 20);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return PluginChannelListener.playerCompleterList;
    }
}
