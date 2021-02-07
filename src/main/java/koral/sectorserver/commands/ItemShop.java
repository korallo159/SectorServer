package koral.sectorserver.commands;

import com.google.common.collect.Iterables;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Date;

public class ItemShop implements CommandExecutor {

    File file = new File(SectorServer.getPlugin().getDataFolder(), "itemshoplog.txt");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++)
            sb.append(args[i]).append(" ");
        forwardCommandToPlayerServer(args[0], sb.toString());
        return false;
    }

    private void forwardCommandToPlayerServer(String target, String message) {
        try {
            if(Bukkit.getServer().getOnlinePlayers().isEmpty()){
               throw new NullPointerException();
            }
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("ForwardToPlayer");
            out.writeUTF(target);
            out.writeUTF("ItemShopChannel");
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            byte[] data = message.getBytes();
            out.writeShort(data.length);
            out.write(data);

            SectorServer.sendPluginMessage(player, b.toByteArray());
        } catch (NullPointerException | IOException ex) {
            try {
                FileWriter writer = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(writer);
                bw.write(new Date() + " Nieudana próba zakupu ze strony. Nikt nie byl online na serwerze obslugujacym itemshop: " + message );
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }


}
