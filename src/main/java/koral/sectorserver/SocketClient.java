package koral.sectorserver;

import com.google.gson.JsonArray;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {
    public static double shiftx;
    public static double shiftz;
    public static int width;

    public static List<String> servery = new ArrayList<>();;


    static Socket socket;
    public static void connectToSocketServer(){
        new Thread(() ->{
            try {
                socket = new Socket(SectorServer.getPlugin().getConfig().getString("ipsocket"),SectorServer.getPlugin().getConfig().getInt("socketport"));
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                String string = bufferedReader.readLine();
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(string);
                String strings = (String) jsonObject.get("servers");
                JSONArray jsonArray = (JSONArray) parser.parse(strings);
                for(int i = 0; i<jsonArray.size(); i++){
                    servery.add(jsonArray.get(i).toString());
                }
                shiftx = (double) jsonObject.get("shiftx");
                shiftz = (double) jsonObject.get("shiftz");
                width = (int) (long) jsonObject.get("width");
                System.out.println(servery);
                SectorServer.reloadPlugin();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();

    }
}
