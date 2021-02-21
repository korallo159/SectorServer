package koral.sectorserver.commands;

import koral.sectorserver.SectorServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;

public class helpop implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SectorServer.sendToServer("helpop", "ALL", out ->{
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i< args.length; i++){
                sb.append(args[i]).append(" ");
            }
            out.writeUTF(sender.getName());
            out.writeUTF(sb.toString());
        });
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
