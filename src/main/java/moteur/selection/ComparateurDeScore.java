package moteur.selection;

import moteur.modele.NomAvecScore;

import java.util.Comparator;

/** Ordonne par score décroissant (meilleur résultat en premier). */
public class ComparateurDeScore implements Comparator<NomAvecScore> {

    @Override
    public int compare(NomAvecScore n1, NomAvecScore n2) {
        return Double.compare(n2.getScore(), n1.getScore());
    }
}
