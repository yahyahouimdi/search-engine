package moteur.selection;

import moteur.modele.NomAvecScore;

import java.util.List;
import java.util.stream.Collectors;

/** Garde les couples dont le score est supérieur ou égal au seuil. */
public class SelectionneurParSeuil implements Selectionneur {
    private double seuil;

    public SelectionneurParSeuil(double seuil) {
        this.seuil = seuil;
    }

    @Override
    public List<NomAvecScore> selectionner(List<NomAvecScore> list) {
        return list.stream()
                .filter(n -> n.getScore() >= seuil)
                .sorted(new ComparateurDeScore())
                .collect(Collectors.toList());
    }

    public double getSeuil() {
        return seuil;
    }

    public void setSeuil(double seuil) {
        this.seuil = seuil;
    }
}
