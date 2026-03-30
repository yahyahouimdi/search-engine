package moteur.generation;

import moteur.modele.CoupleDeNom;
import moteur.modele.ListeGeneree;
import moteur.modele.ListePretraiter;
import moteur.modele.Nom;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Ne garde que les couples de longueurs proches : min(l1, l2) / max(l1, l2) >= ratio.
 * Avec le ratio par défaut (0.8), aucun couple pouvant atteindre un score de Levenshtein
 * de 0.8 n'est écarté.
 */
public class GenerateurParTaille implements GenerateurDeCandidat {

    private final double ratio;

    public GenerateurParTaille() {
        this(0.8);
    }

    public GenerateurParTaille(double ratio) {
        this.ratio = ratio;
    }

    public TreeMap<Integer, List<Nom>> indexer(ListePretraiter liste) {
        TreeMap<Integer, List<Nom>> index = new TreeMap<>();
        for (Nom nom : liste.list()) {
            int longueur = nom.getNom().trim().length();
            if (longueur > 0) {
                index.computeIfAbsent(longueur, k -> new ArrayList<>()).add(nom);
            }
        }
        return index;
    }

    @Override
    public ListeGeneree generer(ListePretraiter aChercher, ListePretraiter reference) {
        TreeMap<Integer, List<Nom>> index = indexer(reference);
        List<CoupleDeNom> couples = new ArrayList<>();

        for (Nom nom : aChercher.list()) {
            int longueur = nom.getNom().trim().length();
            if (longueur == 0) {
                continue;
            }
            int min = (int) Math.ceil(longueur * ratio);
            int max = (int) Math.floor(longueur / ratio);
            for (List<Nom> candidats : index.subMap(min, true, max, true).values()) {
                for (Nom candidat : candidats) {
                    couples.add(new CoupleDeNom(nom, candidat));
                }
            }
        }
        return new ListeGeneree(couples);
    }
}
