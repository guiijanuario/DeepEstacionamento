package org.example.services;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import static org.example.connection.Conexao.getConnection;

public class Service {

    Statement statement = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    String sql = "";

    public void consultCar() {
        String sql = "SELECT carros.nome_carro, carros.placa, carros.data_entrada,carros.hora_entrada, clientes.nome " +
                "FROM carros " +
                "INNER JOIN clientes ON carros.id_cliente = clientes.id " +
                "WHERE carros.status = saiu";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.println("-> Carro: " + resultSet.getString("nome_carro")
                        + "-> Placa: " + resultSet.getString("placa")
                        + "-> Cliente: " + resultSet.getString("nome")
                        + "-> Data entrada: " + resultSet.getDate("data_entrada").toLocalDate()
                        + "-> Hora entrada: " + resultSet.getTime("hora_entrada").toLocalTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void entradaCarro(String nomeCarro, String placa, String nomeCliente, String cpfCliente) {
        try {
            int idCliente = buscarClientePorCPF(cpfCliente);

            if (idCliente == -1) {
                idCliente = cadastrarCliente(nomeCliente, cpfCliente);
            }

            PreparedStatement stmt = Objects.requireNonNull(getConnection()).prepareStatement("INSERT INTO carros (nome_carro, id_cliente, placa, data_entrada, hora_entrada) "
                    + "VALUES (?, ?, ?, CURRENT_DATE, CURRENT_TIME)");
            stmt.setString(1, nomeCarro);
            stmt.setInt(2, idCliente);
            stmt.setString(3, placa);
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Entrada registrada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int buscarClientePorCPF(String cpf) {
        int idCliente = -1;

        try {
            PreparedStatement stmt = Objects.requireNonNull(getConnection()).prepareStatement("SELECT id FROM clientes WHERE cpf = ?");
            stmt.setString(1, cpf);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                idCliente = resultSet.getInt("id");
            }

            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return idCliente;
    }

    private static int cadastrarCliente(String nome, String cpf) {
        int idCliente = -1;

        try {
            PreparedStatement stmt = Objects.requireNonNull(getConnection()).prepareStatement("INSERT INTO clientes (nome, cpf) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nome);
            stmt.setString(2, cpf);
            stmt.executeUpdate();

            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next()) {
                idCliente = resultSet.getInt(1);
            }

            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return idCliente;
    }

    public double registrarSaidaCarro(String placa) {
        double valorPermanencia = 0.0;

        try {
            PreparedStatement stmt = Objects.requireNonNull(getConnection()).prepareStatement("SELECT id, nome_carro, id_cliente, data_entrada, hora_entrada FROM carros WHERE placa = ?");
            stmt.setString(1, placa);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int idCarro = resultSet.getInt("id");
                Date dataEntrada = resultSet.getDate("data_entrada");
                Time horaEntrada = resultSet.getTime("hora_entrada");

                valorPermanencia = calcularValorPermanencia(dataEntrada, horaEntrada);

                stmt = Objects.requireNonNull(getConnection()).prepareStatement("INSERT INTO permanencias (idCarro, data_saida, hora_saida, valor) VALUES (?, CURRENT_DATE, CURRENT_TIME, ?)");
                stmt.setInt(1, idCarro);
                stmt.setDouble(2, valorPermanencia);
                stmt.executeUpdate();
                stmt.close();

                String atualizarStatusCarroSQL = "UPDATE carros SET status = saiu WHERE id = "+idCarro;
                PreparedStatement stmtRemoverCarro = Objects.requireNonNull(getConnection()).prepareStatement(atualizarStatusCarroSQL);
                stmtRemoverCarro.executeUpdate();
                stmtRemoverCarro.close();

            } else {
                System.out.println("Carro não encontrado no estacionamento.");
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return valorPermanencia;
    }

    private static double calcularValorPermanencia(Date dataEntrada, Time horaEntrada) {
        LocalDate dataAtual = LocalDate.now();
        LocalTime horaAtual = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dataEntradaFormatada = LocalDateTime.parse(dataEntrada + " "+horaEntrada, formatter);
        LocalDateTime dataSaidaFormatada = LocalDateTime.parse(dataAtual+" "+horaAtual, formatter);

        Duration permanenciaEstacionamento = Duration.between(dataEntradaFormatada,dataSaidaFormatada);
        long horas = permanenciaEstacionamento.toHours();
        long minutosAdicionais = permanenciaEstacionamento.toMinutes()%60;

        double valorPermanencia ;

        if (horas == 0) {
            valorPermanencia = 10.0;
        } else if (horas < 12) {
            valorPermanencia = 10.0 + (horas-1)*2+(minutosAdicionais > 0 ? 2:0);
        } else {
            valorPermanencia = 90.0;
        }

        return valorPermanencia;
    }

    public void consultarPermanencias(){
        String sql = "SELECT permanencias.data_saida, permanencias.hora_saida, permanencias.valor, " +
                "carros.nome_carro, carros.placa, carros.data_entrada, carros.hora_entrada, clientes.nome " +
                "FROM permanencias " +
                "JOIN carros ON permanencias.idCarro = carros.id " +
                "JOIN clientes ON carros.id_cliente = clientes.id";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.println("-> Cliente: " + resultSet.getString("nome")
                        + "->  Carro: " + resultSet.getString("nome_carro")
                        + "->  Placa: " + resultSet.getString("placa")
                        + "->  Data entrada: " + resultSet.getDate("data_entrada").toLocalDate()
                        + "->  Hora entrada: " + resultSet.getTime("hora_entrada").toLocalTime()
                        + "->  Data saída: " + resultSet.getDate("data_entrada").toLocalDate()
                        + "->  Hora saída: " + resultSet.getTime("hora_entrada").toLocalTime()
                        + "->  Valor: " + resultSet.getDouble("valor"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
