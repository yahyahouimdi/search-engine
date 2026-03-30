package moteur.selection;

import moteur.modele.NomAvecScore;

import java.util.ArrayList;
import java.util.List;

/** Garde tous les couples, triés par score décroissant. */
public class SelectionneurTout implements Selectionneur {

    @Override
    public List<NomAvecScore> selectionner(List<NomAvecScore> list) {
        List<NomAvecScore> triee = new ArrayList<>(list);
        triee.sort(new ComparateurDeScore());
        return triee;
    }
}
