package moteur.comparaison;

/** Similarité entre deux chaînes, de 0 (différentes) à 1 (identiques). */
public interface ComparateurDeChaine {
    double comparer(String nom1, String nom2);
}
