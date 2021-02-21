package koral.sectorserver;

import java.util.Objects;

public class PlayerData {
    public final String nick;
    public final String server;

    public PlayerData(String nick, String server) {
        this.nick = nick;
        this.server = server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return Objects.equals(nick, that.nick) && Objects.equals(server, that.server);
    }
    @Override
    public int hashCode() {
        return Objects.hash(nick, server);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "nick='" + nick + '\'' +
                ", server='" + server + '\'' +
                '}';
    }
}
