package org.example.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {
    public static Connection getConnection(){
        try{

            Properties properties = loadDbProperties();
            String url = properties.getProperty("dburl");
            Connection startDbConnection = DriverManager.getConnection(url, properties);

            if (startDbConnection != null) {
                return startDbConnection;
            }  else {
                return null;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    private static Properties loadDbProperties() {
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/db.properties")) {
            Properties propriedades = new Properties();
            propriedades.load(inputStream);
            return propriedades;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
