package moteur.generation;

import moteur.modele.ListeGeneree;
import moteur.modele.ListePretraiter;

/**
 * Produit les couples (nom recherché, nom de référence) qui méritent d'être comparés,
 * pour éviter de comparer chaque nom à tous les autres.
 */
public interface GenerateurDeCandidat {
    ListeGeneree generer(ListePretraiter aChercher, ListePretraiter reference);
}
