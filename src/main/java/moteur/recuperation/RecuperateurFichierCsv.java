package moteur.recuperation;

import moteur.modele.CoupleDeString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit un fichier CSV à deux colonnes (id, nom) encodé en UTF-8.
 * La première ligne (en-tête) est ignorée. Les champs entre guillemets
 * peuvent contenir des virgules ("Boois, Dawid").
 */
public class RecuperateurFichierCsv implements Recuperateur {

    private final String chemin;

    public RecuperateurFichierCsv(String chemin) {
        this.chemin = chemin;
    }

    @Override
    public List<CoupleDeString> recuperer() {
        Path fichier = Datasets.resoudre(chemin);
        List<CoupleDeString> resultat = new ArrayList<>();

        try (BufferedReader lecteur = Files.newBufferedReader(fichier, StandardCharsets.UTF_8)) {
            String ligne = lecteur.readLine(); // en-tête
            while ((ligne = lecteur.readLine()) != null) {
                List<String> valeurs = decouper(ligne);
                if (valeurs.size() < 2 || valeurs.get(1).isBlank()) {
                    continue; // ligne incomplète
                }
                resultat.add(new CoupleDeString(valeurs.get(0).trim(), valeurs.get(1).trim()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier " + fichier, e);
        }
        return resultat;
    }

    /** Découpe une ligne CSV en respectant les guillemets ("" = guillemet échappé). */
    static List<String> decouper(String ligne) {
        List<String> champs = new ArrayList<>();
        StringBuilder courant = new StringBuilder();
        boolean entreGuillemets = false;

        for (int i = 0; i < ligne.length(); i++) {
            char c = ligne.charAt(i);
            if (entreGuillemets) {
                if (c == '"' && i + 1 < ligne.length() && ligne.charAt(i + 1) == '"') {
                    courant.append('"');
                    i++;
                } else if (c == '"') {
                    entreGuillemets = false;
                } else {
                    courant.append(c);
                }
            } else if (c == '"') {
                entreGuillemets = true;
            } else if (c == ',') {
                champs.add(courant.toString());
                courant.setLength(0);
            } else if (c != '\uFEFF') { // BOM éventuel
                courant.append(c);
            }
        }
        champs.add(courant.toString());
        return champs;
    }
}
