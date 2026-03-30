package moteur.comparaison;

import moteur.modele.Nom;

/** Similarité entre deux noms, de 0 (différents) à 1 (identiques). */
public interface Comparateur {
    double comparer(Nom nom1, Nom nom2);
}
