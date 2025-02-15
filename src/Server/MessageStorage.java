package Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MessageStorage {
    private static final String FILE_PATH = "messages.txt";
    private static final int MAX_MESSAGES = 15;


    /**
     * Sauvegarder un message dans la base de donnees local
     *
     * @param username le nom de l'utilisateur
     * @param message  le message de l'utilisateur
     */
    public static void saveMessage(String message) {
        try {
            Path filePath = Paths.get(FILE_PATH);
            List<String> messages;
            if (Files.exists(filePath)) {
                messages = Files.readAllLines(filePath);
            } else {
                messages = new ArrayList<>();
            }
            //retiré l'ancien message lorsqu'un nouveau est arrivé et que ça dépasse les 15 lignes
            if (messages.size() >= MAX_MESSAGES) {
                messages.remove(0);
            }
            messages.add(message);
            Files.write(filePath, messages, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charger les 15 derniers messages
     *
     * @return les 15 derniers messages
     */
    public static List<String> loadLastMessages() {
        try {
            Path filePath = Paths.get(FILE_PATH);
            List<String> messages;
            if (Files.exists(filePath)) {
                messages = Files.readAllLines(filePath);
            } else {
                messages = new ArrayList<>();
            }
            if (messages.size() > MAX_MESSAGES) {
                // return les 15 derniers messages
                return messages.subList(messages.size() - MAX_MESSAGES, messages.size());
            } else {
                return messages;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
