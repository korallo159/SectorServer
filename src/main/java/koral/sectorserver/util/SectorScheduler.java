package koral.sectorserver.util;

import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SectorScheduler {
    static Map<String, Queue<Runnable>> map = new HashMap<>();


    // Async
    public static void addTaskToQueue(String id, Runnable asyncTask) {
        Queue<Runnable> queue = map.get(id);
        if (queue != null)
            queue.add(asyncTask);
        else {
            queue = new ConcurrentLinkedQueue<>();
            map.put(id, queue);

            queue.add(asyncTask);

            runQueue(id, queue);
        }
    }
    private static void runQueue(String id, Queue<Runnable> queue) {
        Bukkit.getScheduler().runTaskAsynchronously(SectorServer.getPlugin(), () -> {
            Runnable runnable;
            while ((runnable = queue.poll()) != null)
                runnable.run();
            map.remove(id);
        });

    }
}
