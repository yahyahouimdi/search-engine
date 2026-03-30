package moteur.selection;

import moteur.modele.NomAvecScore;

import java.util.List;

/** Filtre et ordonne les couples scorés (par score décroissant). */
public interface Selectionneur {
    List<NomAvecScore> selectionner(List<NomAvecScore> list);
}
