package moteur.generation;

import moteur.modele.CoupleDeNom;
import moteur.modele.ListeGeneree;
import moteur.modele.ListePretraiter;
import moteur.modele.Nom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ne garde que les couples qui partagent au moins un début de mot
 * (index inversé préfixe -> noms). Avec des préfixes de 3 lettres,
 * "Mustafa Abubakr" retrouve "Mustapha Abubakar".
 */
public class GenerateurParToken implements GenerateurDeCandidat {

    private final int longueurPrefixe;

    public GenerateurParToken() {
        this(3);
    }

    public GenerateurParToken(int longueurPrefixe) {
        this.longueurPrefixe = longueurPrefixe;
    }

    public Map<String, List<Nom>> indexer(ListePretraiter liste) {
        Map<String, List<Nom>> index = new HashMap<>();
        for (Nom nom : liste.list()) {
            Set<String> cles = Collections.newSetFromMap(new HashMap<>());
            for (String mot : mots(nom.getNom())) {
                if (cles.add(mot)) {
                    index.computeIfAbsent(mot, k -> new ArrayList<>()).add(nom);
                }
            }
        }
        return index;
    }

    @Override
    public ListeGeneree generer(ListePretraiter aChercher, ListePretraiter reference) {
        Map<String, List<Nom>> index = indexer(reference);
        List<CoupleDeNom> couples = new ArrayList<>();

        for (Nom nom : aChercher.list()) {
            Set<Nom> dejaAjoutes = Collections.newSetFromMap(new IdentityHashMap<>());
            for (String mot : mots(nom.getNom())) {
                for (Nom correspondant : index.getOrDefault(mot, List.of())) {
                    if (dejaAjoutes.add(correspondant)) {
                        couples.add(new CoupleDeNom(nom, correspondant));
                    }
                }
            }
        }
        return new ListeGeneree(couples);
    }

    /** Les clés d'un nom : le préfixe de chacun de ses mots. */
    private String[] mots(String nom) {
        String chaine = nom.trim().toLowerCase(Locale.ROOT);
        if (chaine.isEmpty()) {
            return new String[0];
        }
        String[] mots = chaine.split("\\s+");
        for (int i = 0; i < mots.length; i++) {
            mots[i] = mots[i].substring(0, Math.min(longueurPrefixe, mots[i].length()));
        }
        return mots;
    }
}
