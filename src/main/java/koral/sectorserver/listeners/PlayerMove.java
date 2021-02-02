package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import koral.sectorserver.util.Cooldowns;
import koral.sectorserver.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerMove implements Listener {
    private static String server; // dummy
    Cooldowns cooldown = new Cooldowns(new HashMap<>());
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        server = null;

        forgetBossbars(e.getPlayer());

        for (SectorServer.OtherServer _server : SectorServer.OtherServer.getNonNull())
            SectorServer.doForNonNull(_server, s -> {
                double distance;
                if (s.x != null) distance = Math.abs(s.x - e.getTo().getX() + (s.positive ? 1 : 0));
                else             distance = Math.abs(s.z - e.getTo().getZ() + (s.positive ? 1 : 0));

                if (distance <= .5)
                    server = s.server;
                if (distance <= SectorServer.bossbarDistance)
                    sendBossbar(e.getPlayer(), s.server, distance);
            });

        if (server != null) {
            if(!e.getPlayer().getScoreboardTags().contains("mimiAntyLog")) {
                if(!cooldown.hasCooldown(e.getPlayer(), 5)) {
                    cooldown.setSystemTime(e.getPlayer());
                    Teleport.teleport(e.getPlayer(), e.getTo());
                }
            }
            else {
                e.getPlayer().sendMessage(ChatColor.RED + "Nie możesz zmieniać sektorów w trakcie walki");
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(-3));
            }
        }
    }
    private void forgetBossbars(Player p) {
        try {
            List<BossBar> bossbars = (List<BossBar>) p.getMetadata("sector_bossbars").get(0).value();
            bossbars.forEach(bossbar -> {
                bossbar.setVisible(false);
                bossbar.removePlayer(p);
            });
        } catch (Throwable e) {
        }

    }
    private void sendBossbar(Player p, String server, double distance) {
        BossBar bossbar = Bukkit.createBossBar(
                ChatColor.RED + "Zbliżasz się do sektora " + server + ChatColor.GOLD + " " + ((int) distance) + ChatColor.YELLOW + "m",
                BarColor.RED,
                BarStyle.SOLID
        );

        bossbar.setVisible(true);
        bossbar.setProgress(1 - distance / SectorServer.bossbarDistance);
        bossbar.addPlayer(p);

        List<BossBar> bossbars;
        try {
            bossbars = (List<BossBar>) p.getMetadata("sector_bossbars").get(0).value();
        } catch (Throwable e) {
            bossbars = new ArrayList<>();
        }

        bossbars.add(bossbar);

        p.setMetadata("sector_bossbars", new FixedMetadataValue(SectorServer.getPlugin(), bossbars));
    }

//TODO zrobic lepsze dotykanie tej bariery

    boolean canPass(int s1, int s2) {
        int n = SectorServer.serversPerSide();

        if (Math.min(s1, s2) < 0 || Math.max(s1, s2) >=  SectorServer.serversCount() || s1 == s2)
            return false;

        return  s1 % n == s2 % n ||
                s1 / n == s2 / n;
    }

    /**
     * Na podstawie lokacji zwraca index servera,</br>
     * w przypadku niepowodzenia zwraca -1
     *
     * @param loc zwyczajna lokacja</br>
     *            wewnątrz jest wykonywane {@code loc = SectorServer.shiftLocation(loc)}
     * @return index servera jeśli lokacja należy do jednego z serwerów w innym przypadku -1
     */
    public static int locToServer(Location loc) {
        loc = SectorServer.shiftLocation(loc);
        if (loc.getX() > SectorServer.width * SectorServer.serversPerSide() || loc.getX() < 0)
            return -1;

        int result = (loc.getBlockX() / SectorServer.width) + (loc.getBlockZ() / SectorServer.width * SectorServer.serversPerSide());

        return (result < 0 || result >= SectorServer.serversCount()) ? -1 : result;
        }
}
