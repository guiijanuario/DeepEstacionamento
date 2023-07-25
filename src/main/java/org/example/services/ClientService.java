package org.example.services;

import org.example.model.Client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.example.connection.Conexao.getConnection;

public class ClientService {

    Statement statement = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    String sql = "";
    public void saveClient(String nome, String cpf) {

        try {

            if (getConnection() != null) {
                sql = "INSERT INTO clientes (nome, cpf) VALUES (?, ?)";
                preparedStatement = getConnection().prepareStatement(sql);

                preparedStatement.setString(1, nome);
                preparedStatement.setString(2, cpf);

                int checkIfAdded = preparedStatement.executeUpdate();

                if (checkIfAdded > 0) {
                    System.out.println(" ==== Cliente adicionado com sucesso ====");
                } else {
                    System.out.println("==== Falha ao adicionar o Cliente ====");
                }

                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int searchCustomerByCpf(String cpf) {
        int idClient = -1;

        try {
            if(getConnection() != null){
                sql = "SELECT id FROM clientes WHERE cpf = ?";
                preparedStatement = getConnection().prepareStatement(sql);

                preparedStatement.setString(1, cpf);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    idClient = resultSet.getInt("id");
                }

                resultSet.close();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idClient;
    }


//    public int registerClient(String nome, String cpf) {
//        int idCliente = -1;
//
//        try {
//            if(getConnection() != null) {
//                preparedStatement = getConnection().prepareStatement("INSERT INTO clientes (nome, cpf) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
//                preparedStatement.setString(1, nome);
//                preparedStatement.setString(2, cpf);
//                preparedStatement.executeUpdate();
//
//                ResultSet resultSet = preparedStatement.getGeneratedKeys();
//
//                if (resultSet.next()) {
//                    idCliente = resultSet.getInt(1);
//                }
//
//                resultSet.close();
//                preparedStatement.close();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return idCliente;
//    }


}
