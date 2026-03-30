package moteur.modele;

/**
 * Un nom prêt à être comparé : identifiant, forme normalisée (après prétraitement)
 * et forme originale (telle que lue dans le fichier, utilisée pour l'affichage).
 */
public class Nom {
    private final String id;
    private final String nom;
    private final String nomOriginal;

    public Nom(String id, String nom, String nomOriginal) {
        this.id = id;
        this.nom = nom;
        this.nomOriginal = nomOriginal;
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    @Override
    public String toString() {
        return id + " " + nomOriginal;
    }
}
