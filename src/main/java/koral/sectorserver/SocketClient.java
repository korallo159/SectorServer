package koral.sectorserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketClient {
    static Socket socket;
    //TODO: odebrać jsona, załadować podstawowe dane które są.
    public static void connectToSocketServer(){
        new Thread(() ->{
            try {
                socket = new Socket("localhost",5500);
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);

                String string = bufferedReader.readLine();
                System.out.println("server:" + string);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
