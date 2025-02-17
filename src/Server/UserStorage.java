package Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe qui gère les interactions avec la base de données locales des utilisateurs
 */
public class UserStorage {
    private static final String FILE_PATH = "users.txt";

    // charger les clients dans une hashMap pour un temps de vérification rapide
    public static Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();

        try {
            //Chaque ligne represente un utilisateur et son mot de passe séparré d'une virgule
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);  // user: [clé:nom d'utilisateur: valeur:mot de passe]
                }
            }

        } catch (IOException e) {

        }
        return users;
    }

    /**
     * Sauvegarder un utilisateteur dans la base de données locales
     * @param username le nom de l'utilisateur
     * @param password  le mot de passe de l'utilisateur
     */
    public static void saveUser(String username, String password) {
        try {
            String entry = username + "," + password + "\n";
            Files.write(Paths.get(FILE_PATH), entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie le mot de passe d'un utilisateur existant est correct
     * @param username le nom de l'utilisateur
     * @param password  le mot de passe de l'utilisateur
     * @return vrai si l'utilisateur a le bon mot de passe
     */
    public static boolean validateUser(String username, String password) {
        Map<String, String> users = loadUsers();
        return users.containsKey(username) && users.get(username).equals(password) ;
    }

    /**
     * Vérifie si l'utilisateur existe dans la base de données locale
     * @param username
     * @return vrai si l'utilsateur existe
     */
    public static boolean userExists(String username) {
        return loadUsers().containsKey(username);
    }

}
