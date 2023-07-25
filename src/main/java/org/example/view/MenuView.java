package org.example.view;

import org.example.services.CarService;
import org.example.services.ClientService;
import org.example.services.PermanenceService;
import org.example.services.Service;

import java.util.Scanner;

public class MenuView {

    private CarService carService;
    private ClientService clientService;
    private PermanenceService permanenceService;
    private Service service;


    public MenuView() {
        carService = new CarService();
        clientService = new ClientService();
        permanenceService = new PermanenceService();
    }

    public void homeMenu(){
        System.out.println("--------------------------------------------");
        System.out.println("[ -> 1 Entrada de Carro                    ]");
        System.out.println("[ -> 2 Saída de carro                      ]");
        System.out.println("[ -> 3 Consultar carros no estacionamento  ]");
        System.out.println("[ -> 4 Verificar histórico de permanências ]");
        System.out.println("[ -> 5 Sair do sistema                     ]");
        System.out.println("--------------------------------------------");
    }

    public void menuEntryCar(){
        System.out.println("1. Informe o carro do cliente:");
        String nameCar = new Scanner(System.in).nextLine();

        System.out.println("2. Informe o nome do cliente:");
        String nameClient = new Scanner(System.in).nextLine();

        System.out.println("3. Informe a placa do carro:");
        String place = new Scanner(System.in).nextLine();

        System.out.println("4. Informe o cpf do cliente:");
        String cpfClient = new Scanner(System.in).nextLine();

        carService.entryCarDeep(nameCar,place,nameClient,cpfClient);
    }

    public void menuExitCar(){
        System.out.println("Informe a placa do carro que está saindo:");
        String placaSaida = new Scanner(System.in).nextLine();

        double valorAPagar = service.registrarSaidaCarro(placaSaida);
        System.out.println("Valor a ser pago: R$"+ valorAPagar);
    }

    public void menuExibirCar(){
        service.consultCar();
    }

    public void menuConsultarPermanencias(){
        service.consultarPermanencias();
    }




}
