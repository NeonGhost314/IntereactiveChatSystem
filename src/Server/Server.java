package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static ServerSocket server;
    public static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<ClientHandler>();
    private static final int MIN_PORT = 5000;
    private static final int MAX_PORT = 5050;


    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String serverAddress = getValidatedServerIPAddress(scanner);
        int port = getValidatedServerPort(scanner);
        startServer(serverAddress, port);
    }

    private static String getValidatedServerIPAddress(Scanner scanner) {
        String serverAddress;
        do {
            System.out.print("Veuillez configurer l'adresse du server : ");
            serverAddress = scanner.nextLine();
            if (!isValidIPAddress(serverAddress)) {
                System.out.println("Format de l'adresse IP n'est pas valide. Assurez-vous que c'est bien 4 octects (ex: 192.168.1.1).");
            }
        } while (!isValidIPAddress(serverAddress));
        return serverAddress;
    }

    private static int getValidatedServerPort(Scanner scanner) {
        int port;
        while (true) {
            System.out.print("Veuillez choisir un port pour le server (entre 5000 et 5050): ");
            try {
                port = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre");
                continue;
            }
            if (!isValidPortNumber(port)) {
                System.out.println("Le port est invalide. Il doit absolument être entre 5000 et 5050.");
            } else {
                return port;
            }
        }
    }

    private static void startServer(String serverAddress, int port) throws IOException {
        server = new ServerSocket();
        server.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        server.bind(new InetSocketAddress(serverIP, port));

        System.out.format("Le serveur roule sur %s:%d%n", serverAddress, port);

        int clientNumber = 0;
        try {
            while (true) {
                Socket clientSocket = server.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientNumber++);
                clients.add(clientHandler);
                clientHandler.start();
                System.out.println("New connection with client#" + clientNumber + " at" + clientSocket);
            }
        } finally {
            server.close();
        }
    }


    public static boolean isValidPortNumber(int portNumber) {
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


class ClientHandler extends Thread {
    private Socket clientSocket;
    private int clientNumber;
    private BufferedReader in;
    // TODO change DataOutputStream to printWriter so that there is no flush() management necessary
    private PrintWriter out;


    public ClientHandler(Socket clientSocket, int clientNumber) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Hello from server - you are client#" + clientNumber);
            out.println("Veuillez entrer votre nom d'utilisateur:");

            String username = in.readLine();
            if (username == null || username.isBlank()) {
                out.println("Nom d'utilisateur invalide");
                clientSocket.close();
                return;
            }
            out.println("Veuillez entrer votre mot de passe");
            String password = in.readLine();
            if (password == null || password.isBlank()) {
                out.println("Erreur dans la saisie du mot de passe");
                clientSocket.close();
                return;
            }

            if (UserStorage.userExists(username)) {
                if (!UserStorage.validateUser(username, password)) {
                    out.println("Erreur dans la saisie du mot de passe");
                    clientSocket.close();
                    return;
                }
            } else {
                UserStorage.saveUser(username, password);
            }

            List<String> lastMessages = MessageStorage.loadLastMessages();
            for (String msg : lastMessages) {
                out.println(msg);
            }

            String message;
            while ((message = in.readLine()) != null) {
                if (message.length() > 200) {
                    out.println("Le message dépasse 200 caractères, merci de réduire la taille.");
                    continue;
                }
                String timestamp = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss").format(new Date());
                String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                String formattedMessage = String.format("[%s - %s - %s]: %s", username, clientInfo, timestamp, message);
                System.out.println(formattedMessage);
                MessageStorage.saveMessage(formattedMessage);
                broadcastToAll(formattedMessage);
            }


        } catch (IOException e) {
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        } finally {
            try {
                clientSocket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("ERREUR: Le socket n'a pas pu être fermé correctentment?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }


    public void broadcastToAll(String message) {
        for (ClientHandler client : Server.clients) {
            if (client != this) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void sendMessage(String message) {
        out.println(message);
    }
}


