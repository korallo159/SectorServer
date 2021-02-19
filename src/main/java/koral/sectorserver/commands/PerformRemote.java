package koral.sectorserver.commands;

import com.google.common.collect.Lists;
import koral.sectorserver.SectorServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PerformRemote implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String server = args[0];

        List<String> list = Lists.newArrayList(args);
        list.remove(0);

        SectorServer.sendToServer("remoteCommand", server, out -> out.writeUTF(String.join(" ", list)));

        sender.sendMessage("Wysłałeś komendę na serwer " + server);

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return (args.length == 1) ? Stream.concat(SectorServer.servers.stream(), SectorServer.spawns.stream())
                .collect(Collectors.toList()) : Collections.singletonList("wiadomosc komendy do wykonania");
    }
}
