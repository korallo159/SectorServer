package koral.sectorserver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SocketClient {
    static Socket socket;

    static final List<Class<? extends ForwardChannelListener>> listeners = new ArrayList<>();

    private static boolean first;
    public static void connectToSocketServer() {
        new Thread(() -> {
            try {
                first = true;
                connect();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void connect() throws IOException, InterruptedException {
        try {
            socket = new Socket(SectorServer.getPlugin().getConfig().getString("ipsocket"),SectorServer.getPlugin().getConfig().getInt("socketport"));
            socket.setKeepAlive(true);

            System.out.println("Nawiązano łączność z socketem");

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            if (first) {
                first = false;
                out.writeUTF("start");
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                String config = bufferedReader.readLine();
                getConfiguration(config);
            }
            out.writeUTF("id");
            out.writeUTF(SectorServer.serverName);
            runMainLoop();
            return;
        } catch (ConnectException e2) {
            e2.printStackTrace();
            SectorServer.getPlugin().getLogger().warning("Brak łączności socketów");
        } catch (Throwable e) {
            System.out.println("Błąd z socketami!");
            e.printStackTrace();
        }
        Thread.sleep(1000L);
        connect();
    }

    private static void runMainLoop() throws IOException, InterruptedException {
        try {
            mainloop(new DataInputStream(socket.getInputStream()));
        } catch (SocketException e) {
            e.printStackTrace();
            connect();
        }
    }
    private static void mainloop(DataInputStream datain) throws IOException, InterruptedException {
        boolean loop = true;
        while(loop) {
            String received;

            try {
                received = datain.readUTF();
            } catch (UTFDataFormatException e) {
                System.out.println("Problem z UTF!");
                e.printStackTrace();
                socket.close();
                connect();
                break;
            }

            switch(received) {
                case "exit":
                    socket.close();
                    System.out.println("Serwer rozlaczyl klienta");
                    loop = false;
                    break;
                default: // forward
                    int len = datain.readShort();
                    byte[] data = new byte[len];
                    datain.readFully(data);

                    for (int i = 0; i < listeners.size(); i++) {
                        Class<? extends ForwardChannelListener> clazz = listeners.get(i);
                        try {
                            Method met = clazz.getDeclaredMethod(received, DataInputStream.class);
                            met.setAccessible(true);
                            met.invoke(null, new DataInputStream(new ByteArrayInputStream(data)));
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            System.out.println("Awaryjne łączenie");
                            e.printStackTrace();
                            try {
                                socket.close();
                            } finally {
                                connect();
                                break;
                            }
                        } catch (NoSuchMethodException e) {
                        }
                    }
                    break;
            }
        }
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
