package moteur.comparaison;

import moteur.modele.Nom;

/** Compare deux noms en déléguant à un comparateur de chaînes (Jaro-Winkler, Levenshtein...). */
public class ComparateurDeNomParChaine implements Comparateur {
    private final ComparateurDeChaine comparateurDeChaine;

    public ComparateurDeNomParChaine(ComparateurDeChaine comparateurDeChaine) {
        this.comparateurDeChaine = comparateurDeChaine;
    }

    @Override
    public double comparer(Nom nom1, Nom nom2) {
        return comparateurDeChaine.comparer(nom1.getNom(), nom2.getNom());
    }
}
