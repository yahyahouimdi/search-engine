import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class SelectionneurParSeuil implements Selectionneur {
    private double seuil;

    public SelectionneurParSeuil(double seuil) {
        this.seuil = seuil;
    }

    @Override
    public List<NomAvecScore> selectionner(List<NomAvecScore> list) {
        // Trier la liste par score décroissant
        Collections.sort(list, new ComparateurDeScore());
        Collections.reverse(list);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getScore() < seuil) {
                return list.subList(0, i); // retourne les éléments au-dessus du seuil
            }
        }

        // Tous les éléments sont >= seuil, retourne la liste entière
        return new ArrayList<>(list);
    }

    public double getSeuil() {
        return seuil;
    }

    public void setSeuil(double seuil) {
        this.seuil = seuil;
    }
}
