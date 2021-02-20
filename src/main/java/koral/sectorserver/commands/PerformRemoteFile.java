package koral.sectorserver.commands;

import com.google.common.collect.Lists;
import koral.sectorserver.SectorServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PerformRemoteFile implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = Lists.newArrayList(args);

        String server = args[0];
        list.remove(0);

        String path = String.join(" ", list);

        File file = new File(path);

        if (!file.exists())
            sender.sendMessage("Ten plik nie istnieje!");
        else if (!file.isFile())
            sender.sendMessage("Ten plik nie jest plikiem!");
        else {
            SectorServer.sendToServer("remoteCommandFile", server, out -> {
                out.writeUTF(String.join(" ", list));
                byte[] data;
                try (FileInputStream fis = new FileInputStream(file)) {
                    data = new byte[(int) file.length()];
                    fis.read(data);
                }
                out.writeShort(data.length);
                out.write(data);
            });

            sender.sendMessage("Wysłałeś plik " + path + " na serwer " + server);
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return (args.length == 1) ? Stream.concat(SectorServer.servers.stream(), SectorServer.spawns.stream())
                .collect(Collectors.toList()) : Collections.singletonList("path pliku");
    }
}
