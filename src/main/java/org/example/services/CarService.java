package org.example.services;

import org.example.model.Car;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.example.connection.Conexao.getConnection;

public class CarService {

    Statement statement = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    String sql = "";

    private PermanenceService permanenceService;

    private ClientService clientService;


    public void saveCar(String name, Integer idCliente, String plate) {
        try {
            if (getConnection() != null) {
                sql = "INSERT INTO carros (nome_carro, id_cliente, placa, data_entrada, hora_entrada) VALUES (?, ?, ?, CURRENT_DATE, CURRENT_TIME)";
                preparedStatement = getConnection().prepareStatement(sql);

                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, idCliente);
                preparedStatement.setString(3, plate);


                int checkIfAdded = preparedStatement.executeUpdate();

                if (checkIfAdded > 0) {
                    System.out.println(" ==== Carro adicionado com sucesso ====");
                } else {
                    System.out.println("==== Falha ao adicionar o Carro ====");
                }
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("[Error] Não foi possível cadastrar um novo Carro.");
            e.printStackTrace();
        }
    }

//    public void entryCarDeep(String nomeCarro, String placa, String nomeCliente, String cpfCliente) {
//        try {
//            int idCliente = clientService.searchCustomerByCpf(cpfCliente);
//
//            if (idCliente == -1) {
//                idCliente = clientService.registerClient(nomeCliente, cpfCliente);
//            }
//
//            if (getConnection() != null) {
//                sql = "INSERT INTO carros (nome_carro, id_cliente, placa, data_entrada, hora_entrada) VALUES (?, ?, ?, CURRENT_DATE, CURRENT_TIME)";
//                preparedStatement = getConnection().prepareStatement(sql);
//
//                preparedStatement.setString(1, nomeCarro);
//                preparedStatement.setInt(2, idCliente);
//                preparedStatement.setString(3, placa);
//                preparedStatement.executeUpdate();
//                preparedStatement.close();
//
//                System.out.println("Entrada registrada com sucesso!");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public void entryCarDeep(String nomeCarro, String placa, String nomeCliente, String cpfCliente) {
        try {
            int idCliente = buscarClientePorCPF(cpfCliente);

            if (idCliente == -1) {
                idCliente = cadastrarCliente(nomeCliente, cpfCliente);
            }

            if (getConnection() != null) {
                PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO carros (nome_carro, id_cliente, placa, data_entrada, hora_entrada) "
                        + "VALUES (?, ?, ?, CURRENT_DATE, CURRENT_TIME)");
                stmt.setString(1, nomeCarro);
                stmt.setInt(2, idCliente);
                stmt.setString(3, placa);
                stmt.executeUpdate();
                stmt.close();

                System.out.println("Entrada registrada com sucesso!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int buscarClientePorCPF(String cpf) {
        int idCliente = -1;

        try {
            if (getConnection() != null) {
                PreparedStatement stmt = getConnection().prepareStatement("SELECT id FROM clientes WHERE cpf = ?");
                stmt.setString(1, cpf);
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    idCliente = resultSet.getInt("id");
                }

                resultSet.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return idCliente;
    }

    public int cadastrarCliente(String nome, String cpf) {
        int idCliente = -1;

        try {
            if (getConnection() != null) {
                PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO clientes (nome, cpf) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nome);
                stmt.setString(2, cpf);
                stmt.executeUpdate();

                ResultSet resultSet = stmt.getGeneratedKeys();
                if (resultSet.next()) {
                    idCliente = resultSet.getInt(1);
                }

                resultSet.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return idCliente;
    }

    public String exitStatusCar(int idCar) {
        String statusCarro = "estacionado";
        try {
            if (getConnection() != null) {
                sql = "UPDATE carros SET status = saiu WHERE id = " + idCar;
                preparedStatement = getConnection().prepareStatement(sql);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                return statusCarro = "saiu";

            }
        } catch (SQLException e) {
            System.out.println("[Error] Não foi possível atualizar o status do carro.");
            e.printStackTrace();
        }
        return statusCarro;
    }

    public double registrarSaidaCarro(String place) {
        double veluePermanence = 0.0;

        try {
            if (getConnection() != null) {
                preparedStatement = getConnection().prepareStatement("SELECT id, nome_carro, id_cliente, data_entrada, hora_entrada FROM carros WHERE placa = ?");
                preparedStatement.setString(1, place);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int idCar = resultSet.getInt("id");
                    Date dateEntry = resultSet.getDate("data_entrada");
                    Time entryTime = resultSet.getTime("hora_entrada");

                    veluePermanence = permanenceService.calculatePermanenceValue(dateEntry, entryTime);

                    preparedStatement = getConnection().prepareStatement("INSERT INTO permanencias (idCarro, data_saida, hora_saida, valor) VALUES (?, CURRENT_DATE, CURRENT_TIME, ?)");
                    preparedStatement.setInt(1, idCar);
                    preparedStatement.setDouble(2, veluePermanence);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                    String updateStatusCarSQL = "UPDATE carros SET status = saiu WHERE id = " + idCar;
                    PreparedStatement preparedStatementUpdateCar = getConnection().prepareStatement(updateStatusCarSQL);
                    preparedStatementUpdateCar.executeUpdate();
                    preparedStatementUpdateCar.close();

                } else {
                    System.out.println("Carro já não está no estacionamento.");
                }
                resultSet.close();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return veluePermanence;
    }

    public void consultarCarros() {
        String sql = "SELECT carros.nome_carro, carros.placa, carros.data_entrada,carros.hora_entrada, clientes.nome " +
                "FROM carros " +
                "INNER JOIN clientes ON carros.id_cliente = clientes.id " +
                "WHERE carros.status = TRUE";

        try {
            if (getConnection() != null) {

                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    System.out.println("carro: " + resultSet.getString("nome_carro")
                            + " | placa: " + resultSet.getString("placa")
                            + " | cliente: " + resultSet.getString("nome")
                            + " | data entrada: " + resultSet.getDate("data_entrada").toLocalDate()
                            + " | hora entrada: " + resultSet.getTime("hora_entrada").toLocalTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

