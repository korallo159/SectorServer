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

                final double shiftx = (double) jsonObject.get("shiftx");
                final double shiftz = (double) jsonObject.get("shiftz");
                final int width = (int) (long) jsonObject.get("width");
                final int protectedBlocks = (int) (long) jsonObject.get("protectedBlocks");

                SectorServer.reloadPlugin(servery, width, shiftx, shiftz, protectedBlocks);
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
