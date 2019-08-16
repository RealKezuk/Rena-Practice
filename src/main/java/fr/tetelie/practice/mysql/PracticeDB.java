package fr.tetelie.practice.mysql;

import co.aikar.idb.DB;
import co.aikar.idb.DbStatement;
import fr.tetelie.practice.Practice;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PracticeDB {

    public boolean createPlayerManagerTable() {
        return DB.createTransaction(stm -> createPlayerManagerTable(stm));
    }

    private boolean createPlayerManagerTable(DbStatement stm) {
        String player_manager = "CREATE TABLE player_manager ("
                + "ID INT(64) NOT NULL AUTO_INCREMENT,"
                + "name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(64) NOT NULL,"
                + "settings VARCHAR(5) DEFAULT '0:0:0',"
                + "elos VARCHAR(32) DEFAULT '1000',"
                + "PRIMARY KEY (`ID`))";
        try {
            DatabaseMetaData dbm = Practice.getInstance().getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, "player_manager", null);
            if (tables.next()) {
                //table exist
                return false;
            } else {
                //table doesn't exist
                stm.executeUpdateQuery(player_manager);
                System.out.println(Practice.getInstance().prefix + "SUCESS create player_manager table.");
                return true;
            }

        } catch (SQLException e) {
            System.out.println(Practice.getInstance().prefix + "ERROR while creating player_manager table.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean createPlayerManager(UUID uuid, String name)
    {
        return DB.createTransaction(stm -> createPlayerManager(uuid, name, stm));
    }

    private boolean createPlayerManager(UUID uuid, String name, DbStatement stm) {
        String query = "INSERT INTO player_manager (uuid, name) " +
                "VALUES (?, ?)";
        try {
            return stm.executeUpdateQuery(query, uuid.toString(), name) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePlayerManager(String name, UUID uuid)
    {
        return DB.createTransaction(stm -> updatePlayerManager(name, uuid, stm));
    }

    private boolean updatePlayerManager(String name, UUID uuid, DbStatement stm) {
        String query = "UPDATE player_manager SET name=? WHERE uuid=?";
        try {
            return stm.executeUpdateQuery(query, name, uuid.toString()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existPlayerManager(UUID uuid)
    {
        return DB.createTransaction(stm -> existPlayerManager(uuid, stm));
    }

    private boolean existPlayerManager(UUID uuid, DbStatement stm) {
        String query = "SELECT * FROM player_manager WHERE uuid=?";
        try {
            return stm.executeQueryGetFirstRow(query, uuid.toString()) != null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
