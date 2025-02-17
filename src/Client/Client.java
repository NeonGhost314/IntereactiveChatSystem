package Client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import  java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class Client {
    private static Socket socket;
    private static final int MIN_PORT = 5000;
    private static final int MAX_PORT = 5050;

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        String serverAddress ;
        while ( true ) {
            System.out.println("Entrez IP Address:");
            serverAddress = scanner.nextLine();

            while (!isValidIPAddress(serverAddress)) {
                System.out.println("Adresse IP invalide, veuillez reessayer");
                serverAddress = scanner.nextLine();
            }

            System.out.println("Entrez numero de PORT:");
            int port = scanner.nextInt();
            scanner.nextLine();

            try {
                socket = new Socket(serverAddress, port);
                if ( socket.isConnected() ) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Connexion impossible avec les donnees fournies, veuillez reessayer");
            }
        }

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final AtomicBoolean over = new AtomicBoolean(false);

        Thread terminalInput = new Thread(() -> {
            try (Scanner terminalScanner = new Scanner(System.in)) {
                while (true) {
                    String message = terminalScanner.nextLine();
                    if ( message.length() > 250){
                        System.out.println("limite de characteres est de 250");
                        continue;
                    }
                    if ( message.equals("exit")){
                        System.out.println("deconnexion...");
                        over.set(true);
                        socket.close();
                        break;
                    }
                    out.println(message);
                }
            } catch (Exception e) {
                System.out.println("erreur dans la deconnexion");
            }
        });
        terminalInput.start();

        try {
            String serverMessage;
            while (!over.get() && (serverMessage = in.readLine()) != null ) {
                System.out.println(serverMessage);
            }
        } finally {
            System.out.println("Deconnexion reussie, a la prochaine");
            System.exit(0);
        }
    }


    private static boolean isValidPortNumber(int portNumber) {
        return portNumber >= MIN_PORT && portNumber <= MAX_PORT;
    }
    public static boolean isValidIPAddress(String addressIp) {
        String[] parts = addressIp.split("\\.");
        if (parts.length != 4) return false;
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255)
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}


