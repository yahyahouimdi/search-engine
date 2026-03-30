package moteur;

import moteur.modele.CoupleDeNom;
import moteur.modele.CoupleDeString;
import moteur.modele.ListeDesNoms;
import moteur.modele.ListePretraiter;
import moteur.modele.Nom;
import moteur.modele.NomAvecScore;
import moteur.pretraitement.Pretraiteur;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline de rapprochement de noms :
 * prétraitement -> génération de candidats -> comparaison -> sélection.
 */
public class MoteurDeRecherche {
    /** Les gros fichiers sont traités par blocs pour limiter le nombre de couples en mémoire. */
    private static final int NOMBRE_DE_BLOCS = 10;
    private static final int TAILLE_BLOC_MIN = 1000;

    private final Configuration config;

    public MoteurDeRecherche(Configuration configuration) {
        this.config = configuration;
    }

    public Configuration getConfiguration() {
        return config;
    }

    /** Cherche chaque nom de {@code aChercher} dans {@code reference}. */
    public List<NomAvecScore> rechercher(ListeDesNoms aChercher, ListeDesNoms reference) {
        return executer(pretraiter(aChercher), pretraiter(reference), false);
    }

    /** Rapproche les noms de deux fichiers différents. */
    public List<NomAvecScore> comparer(ListeDesNoms liste1, ListeDesNoms liste2) {
        return executer(pretraiter(liste1), pretraiter(liste2), false);
    }

    /** Trouve les doublons d'un fichier (chaque couple n'apparaît qu'une fois, sans auto-correspondance). */
    public List<NomAvecScore> dedupliquer(ListeDesNoms liste) {
        ListePretraiter pretraitee = pretraiter(liste);
        return executer(pretraitee, pretraitee, true);
    }

    /** Applique la chaîne de prétraitements au nom, en conservant l'identifiant et le nom original. */
    public ListePretraiter pretraiter(ListeDesNoms liste) {
        List<Nom> noms = new ArrayList<>(liste.liste().size());
        for (CoupleDeString couple : liste.liste()) {
            String nom = couple.getElement2();
            for (Pretraiteur pretraiteur : config.getPretraiteurs()) {
                nom = pretraiteur.pretraiter(nom);
            }
            noms.add(new Nom(couple.getElement1(), nom, couple.getElement2()));
        }
        return new ListePretraiter(noms);
    }

    private List<NomAvecScore> executer(ListePretraiter aChercher, ListePretraiter reference, boolean deduplication) {
        List<Nom> noms = aChercher.list();
        int tailleBloc = Math.max(TAILLE_BLOC_MIN, (noms.size() + NOMBRE_DE_BLOCS - 1) / NOMBRE_DE_BLOCS);
        List<NomAvecScore> retenus = new ArrayList<>();

        for (int debut = 0; debut < noms.size(); debut += tailleBloc) {
            ListePretraiter bloc = new ListePretraiter(noms.subList(debut, Math.min(debut + tailleBloc, noms.size())));
            List<NomAvecScore> candidats = new ArrayList<>();

            for (CoupleDeNom couple : config.getGenerateur().generer(bloc, reference).liste()) {
                if (deduplication && !estCoupleDeDoublonsAGarder(couple)) {
                    continue;
                }
                double score = config.getComparateur().comparer(couple.getNom1(), couple.getNom2());
                candidats.add(new NomAvecScore(couple, score));
            }
            retenus.addAll(config.getSelectionneur().selectionner(candidats));
        }

        // Re-sélection globale : tri final et filtrage cohérent entre les blocs
        return config.getSelectionneur().selectionner(retenus);
    }

    /** En déduplication, ignore (a, a) et ne garde qu'un seul sens parmi (a, b) / (b, a). */
    private static boolean estCoupleDeDoublonsAGarder(CoupleDeNom couple) {
        return couple.getNom1().getId().compareTo(couple.getNom2().getId()) < 0;
    }
}
