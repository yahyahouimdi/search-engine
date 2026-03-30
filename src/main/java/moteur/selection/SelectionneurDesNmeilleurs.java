package moteur.selection;

import moteur.modele.NomAvecScore;

import java.util.ArrayList;
import java.util.List;

/** Garde les N couples ayant les meilleurs scores. */
public class SelectionneurDesNmeilleurs implements Selectionneur {
    private int n;

    public SelectionneurDesNmeilleurs(int n) {
        this.n = n;
    }

    @Override
    public List<NomAvecScore> selectionner(List<NomAvecScore> list) {
        List<NomAvecScore> triee = new ArrayList<>(list);
        triee.sort(new ComparateurDeScore());
        return new ArrayList<>(triee.subList(0, Math.min(n, triee.size())));
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
