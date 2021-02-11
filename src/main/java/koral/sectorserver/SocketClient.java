package koral.sectorserver;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {
    public static Socket socket;
    public static void connectToSocketServer() {

        new Thread(() -> {
            try {
                String received;
                socket = new Socket(SectorServer.getPlugin().getConfig().getString("ipsocket"),SectorServer.getPlugin().getConfig().getInt("socketport"));

                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                out.writeUTF("start");
                String config = bufferedReader.readLine();
                getConfiguration(config);
                out.writeUTF("id");
                out.writeUTF(SectorServer.serverName);
                boolean loop = true;
                while(loop){
                    received = bufferedReader.readLine();
                    switch(received){
                        case"SocketTestChannel":
                            String data = bufferedReader.readLine();
                            System.out.println("SocketTestChannel " + data);
                            break;
                        case"exit":
                            socket.close();
                            System.out.println("Serwer rozlaczyl klienta");
                            loop = false;
                            break;
                    }
                }
                bufferedReader.close();
                in.close();
                out.close();
            } catch (ConnectException e2) {
                System.out.println("Brak łączności z SocketServerem");
                Bukkit.getScheduler().runTaskLater(SectorServer.plugin, SocketClient::connectToSocketServer, 200);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public static void getConfiguration(String string){
        try {
    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(string);

    String strings = (String) jsonObject.get("servers");
    JSONArray jsonArray = (JSONArray) parser.parse(strings);
    final List<String> servery = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
        servery.add(jsonArray.get(i).toString());
    }

    String strings2 = (String) jsonObject.get("spawns");
    JSONArray jsonArray1 = (JSONArray) parser.parse(strings2);
    final List<String> spawns = new ArrayList<>();
    for (int i = 0; i < jsonArray1.size(); i++) {
        spawns.add(jsonArray1.get(i).toString());
    }

    String strings3 = (String) jsonObject.get("blockedcmds");
    JSONArray jsonArray2 = (JSONArray) parser.parse(strings3);
    final List<String> blockedCmds = new ArrayList<>();
    for (int i = 0; i < jsonArray2.size(); i++) {
        blockedCmds.add(jsonArray2.get(i).toString());
    }

    final double shiftx = (double) jsonObject.get("shiftx");
    final double shiftz = (double) jsonObject.get("shiftz");
    final int width = (int) (long) jsonObject.get("width");
    final int protectedBlocks = (int) (long) jsonObject.get("protectedBlocks");
    final int bossbarDistance = (int) (long) jsonObject.get("bossbarDistance");

    SectorServer.reloadPlugin(servery, spawns, width, shiftx, shiftz, protectedBlocks, blockedCmds, bossbarDistance);
}
catch (ParseException e){
    e.printStackTrace();
}
    }
}
