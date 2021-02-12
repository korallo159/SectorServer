package koral.sectorserver.listeners;

import koral.sectorserver.ForwardChannelListener;

import java.io.DataInputStream;
import java.io.IOException;

public class SocketTestListener implements ForwardChannelListener {
    static void SocketTestChannel(DataInputStream in) throws IOException {
        System.out.println("Listener");
        System.out.println(in.readUTF());
        System.out.println(in.readUTF());
        System.out.println(in.readShort());
    }
}
