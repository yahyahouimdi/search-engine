package moteur.comparaison;

import moteur.modele.Nom;

/** Égalité stricte : 1 si les noms (prétraités) sont identiques, 0 sinon. */
public class ComparateurSimple implements Comparateur {

    @Override
    public double comparer(Nom nom1, Nom nom2) {
        return nom1.getNom().equals(nom2.getNom()) ? 1 : 0;
    }
}
