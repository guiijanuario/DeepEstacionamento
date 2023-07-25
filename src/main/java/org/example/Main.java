package org.example;


import org.example.view.MenuView;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MenuView menuView  = new MenuView();

        int opcao;
        do {
            menuView.homeMenu();
            opcao = new Scanner(System.in).nextInt();

            switch (opcao) {
                case 1:
                    menuView.menuEntryCar();
                    break;
                case 2:
                    menuView.menuExitCar();
                    break;
                case 3:
                    menuView.menuExibirCar();
                    break;
                case 4:
                    menuView.menuConsultarPermanencias();
                    break;
                case 5:
                    System.out.println("Encerrando o programa...");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }
        } while (opcao != 5);

    }
}