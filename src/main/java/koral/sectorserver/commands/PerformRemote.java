package koral.sectorserver.commands;

import com.google.common.collect.Iterables;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PerformRemote implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++)
                sb.append(args[i]).append(" ").substring(0, args.length - 1);
            forwardCommandToServer(args[0], sb.toString());
            sender.sendMessage("Wysłałeś komendę jako gracz ");
        }
        return true;
    }

    private void forwardCommandToServer(String server, String message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF("RemoteChannel");
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
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

        return (args.length == 1) ? Stream.concat(SectorServer.servers.stream(), SectorServer.spawns.stream()).collect(Collectors.toList()) : Collections.singletonList("wiadomosc komendy do wykonania");
    }
}
