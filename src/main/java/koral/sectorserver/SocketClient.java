package koral.sectorserver;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {
    static Socket socket;
    private static boolean connecting = false;
    public static void connectToSocketServer() {
        if (connecting)
            return;
        connecting = true;
        new Thread(() -> {
            try {
                socket = new Socket(SectorServer.getPlugin().getConfig().getString("ipsocket"),SectorServer.getPlugin().getConfig().getInt("socketport"));

                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                String string = bufferedReader.readLine();

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(string);

                String strings = (String) jsonObject.get("servers");
                JSONArray jsonArray = (JSONArray) parser.parse(strings);
                final List<String> servery = new ArrayList<>();
                for(int i = 0; i<jsonArray.size(); i++) {
                    servery.add(jsonArray.get(i).toString());
                }

                String strings2 = (String) jsonObject.get("spawns");
                JSONArray jsonArray1 = (JSONArray) parser.parse(strings2);
                final List<String> spawns = new ArrayList<>();
                for(int i = 0; i<jsonArray1.size(); i++){
                    spawns.add(jsonArray1.get(i).toString());
                }

                String strings3 = (String) jsonObject.get("blockedcmds");
                JSONArray jsonArray2 = (JSONArray) parser.parse(strings3);
                final List<String> blockedCmds = new ArrayList<>();
                for(int i = 0; i<jsonArray2.size(); i++){
                    blockedCmds.add(jsonArray2.get(i).toString());
                }

                final double shiftx = (double) jsonObject.get("shiftx");
                final double shiftz = (double) jsonObject.get("shiftz");
                final int width = (int) (long) jsonObject.get("width");
                final int protectedBlocks = (int) (long) jsonObject.get("protectedBlocks");
                final int bossbarDistance = (int) (long) jsonObject.get("bossbarDistance");

                SectorServer.reloadPlugin(servery, spawns, width, shiftx, shiftz, protectedBlocks, blockedCmds, bossbarDistance);
            } catch (ConnectException e2) {
                System.out.println("Brak łączności z SocketServerem");
                Bukkit.getScheduler().runTaskLater(SectorServer.plugin, SocketClient::connectToSocketServer, 200);
            } catch (IOException | ParseException e) {
                connecting = false;
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    connecting = false;
                } catch (IOException exception) {

                }
            }
        }).start();

    }
}
