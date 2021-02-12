package koral.sectorserver.commands;

import koral.sectorserver.SectorServer;
import koral.sectorserver.SocketClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class SocketTest implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SectorServer.sendToServer("SocketTestChannel", "s2", out -> {
            out.writeUTF("test 1 dla s2");
            out.writeUTF("test 2 dla s2");
            out.writeShort(12452);
        });
        SectorServer.sendToServer("SocketTestChannel", "ALL", out -> {
            out.writeUTF("test 1 dla ALL");
            out.writeUTF("test 2 dla ALL");
            out.writeShort(2112);
        });
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
