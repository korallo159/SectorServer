package koral.sectorserver.database.statements;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static koral.sectorserver.database.DatabaseConnection.hikari;

public class Players {

    public static void createPlayerQuery(Player player) {
        Connection connection = null;

        String update = "INSERT INTO Players (NICK, UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE NICK=?";


        PreparedStatement statement = null;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            statement = connection.prepareStatement(update);
            statement.setString(1, player.getName());
            statement.setString(2, player.getUniqueId().toString());
            statement.setString(3, player.getName());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO: Aktualizowanie EQ przy wyjsciu gracza z serwera// jak bedzie gotowe, to zrobic jeszcze zeby zapisywalo co 5 min eq kazdego.
    public static void updatePlayerData(Player player){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            String update = "UPDATE Players SET playerdata=? WHERE NICK=?";
            statement = connection.prepareStatement(update);
            statement.setString(1, "Testowy string - tutaj ma byc cale data entity"); //TODO: wrzucic tutaj jsona  data entity z danymi gracza
            statement.setString(2, player.getName());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO: Odbieranie eq z bazy z ktorej dostalismy
    public static void getMysqlPlayerData(Player player){
      Connection connection = null;
      PreparedStatement statement = null;
      String sql = "SELECT playerdata FROM Players WHERE NICK=?";
      try{
          connection = hikari.getConnection();
          statement = connection.prepareStatement(sql);
          statement.setString(1, player.getName());
          ResultSet resultSet = statement.executeQuery();
          while(resultSet.next()){
              String data = resultSet.getString("playerdata");
              System.out.println(data); // data <- string z jsonem data entity
          }
      }catch (SQLException e){

      }
      finally {
          if(connection != null){
              try {
                  connection.close();
              } catch (SQLException e1) {
                  e1.printStackTrace();
              }
          }
          if(statement != null){
              try {
                  statement.close();
              } catch (SQLException e2) {
                  e2.printStackTrace();
              }
          }
      }
  }
}
