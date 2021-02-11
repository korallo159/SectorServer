package koral.sectorserver.commands;

import koral.sectorserver.SocketClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class SocketTest implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            DataOutputStream out = new DataOutputStream(SocketClient.socket.getOutputStream());
            out.writeUTF("forward");
            out.writeUTF("s2");
            out.writeUTF("SocketTestChannel");
            out.writeUTF("test");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
