package org.example.services;

import org.example.model.Permanence;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.example.connection.Conexao.getConnection;

public class PermanenceService {
    Statement statement = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    String sql = "";

    private CarService carService;


//    public double calculatePermanenceValue1(Date dataEntrada, Time horaEntrada) {
//        LocalDate dataAtual = LocalDate.now();
//        LocalTime horaAtual = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dataEntradaFormatada = LocalDateTime.parse(dataEntrada + " "+ horaEntrada, formatter);
//        LocalDateTime dataSaidaFormatada = LocalDateTime.parse(dataAtual+" "+horaAtual, formatter);
//
//        Duration permanenciaEstacionamento = Duration.between(dataEntradaFormatada,dataSaidaFormatada);
//        long horas = permanenciaEstacionamento.toHours();
//        long minutosAdicionais = permanenciaEstacionamento.toMinutes()%60;
//
//        double valuePermanence ;
//
//        if (horas == 0) {
//            valuePermanence = 10.0;
//        } else if (horas < 12) {
//            valuePermanence = 10.0 + (horas-1)*2+(minutosAdicionais > 0 ? 2:0);
//        } else {
//            valuePermanence = 90.0;
//        }
//
//        return valuePermanence;
//    }


    public double calculatePermanenceValue(Date dataEntrada, Time horaEntrada) {
        LocalDateTime dataEntradaFormatada = convertToDateTime(dataEntrada, horaEntrada);
        LocalDateTime dataSaidaFormatada = LocalDateTime.now();

        Duration permanenciaEstacionamento = Duration.between(dataEntradaFormatada, dataSaidaFormatada);
        long horas = permanenciaEstacionamento.toHours();
        long minutosAdicionais = permanenciaEstacionamento.toMinutes() % 60;

        return calculatePermanenceValueFromDuration(horas, minutosAdicionais);
    }

    public LocalDateTime convertToDateTime(Date date, Time time) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        LocalDate localDate = zonedDateTime.toLocalDate();
        LocalTime localTime = time.toLocalTime().truncatedTo(ChronoUnit.SECONDS);
        return LocalDateTime.of(localDate, localTime);
    }


    public double calculatePermanenceValueFromDuration(long horas, long minutosAdicionais) {
        final double FIRST_HOUR_PRICE = 10.0;
        final double ADDITIONAL_HOUR_PRICE = 2.0;
        final double MAX_PRICE = 90.0;

        if (horas == 0) {
            return FIRST_HOUR_PRICE;
        } else if (horas < 12) {
            double additionalHoursPrice = (horas - 1) * ADDITIONAL_HOUR_PRICE;
            double additionalMinutesPrice = minutosAdicionais > 0 ? ADDITIONAL_HOUR_PRICE : 0;
            return FIRST_HOUR_PRICE + additionalHoursPrice + additionalMinutesPrice;
        } else {
            return MAX_PRICE;
        }
    }

    public void consultStays() {
        String sql = "SELECT permanencias.data_saida, permanencias.hora_saida, permanencias.valor, " +
                "carros.nome_carro, carros.placa, carros.data_entrada, carros.hora_entrada, clientes.nome " +
                "FROM permanencias " +
                "JOIN carros ON permanencias.idCarro = carros.id " +
                "JOIN clientes ON carros.id_cliente = clientes.id";

        try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String client = resultSet.getString("nome");
                String nameCar = resultSet.getString("nome_carro");
                String place = resultSet.getString("placa");
                LocalDate dateEntry = resultSet.getDate("data_entrada").toLocalDate();
                LocalTime entryTime = resultSet.getTime("hora_entrada").toLocalTime();
                LocalDate departureDate = resultSet.getDate("data_saida").toLocalDate();
                LocalTime departureTime = resultSet.getTime("hora_saida").toLocalTime();
                double value = resultSet.getDouble("valor");

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                String formattedDataEntrada = dateEntry.format(dateFormatter);
                String formattedHoraEntrada = entryTime.format(timeFormatter);
                String formattedDataSaida = departureDate.format(dateFormatter);
                String formattedHoraSaida = departureTime.format(timeFormatter);

                System.out.println(
                        "-> Cliente: " + client
                        + " -> Carro: " + nameCar
                        + " -> Placa: " + place
                        + " -> Data entrada: " + formattedDataEntrada
                        + " -> Hora entrada: " + formattedHoraEntrada
                        + " -> Data saída: " + formattedDataSaida
                        + " -> Hora saída: " + formattedHoraSaida
                        + " -> Valor pago: " + value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
