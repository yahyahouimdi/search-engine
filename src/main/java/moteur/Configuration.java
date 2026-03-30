package moteur;

import moteur.comparaison.Comparateur;
import moteur.comparaison.ComparateurAvecJaroWinkler;
import moteur.comparaison.ComparateurDeNomParChaine;
import moteur.generation.GenerateurDeCandidat;
import moteur.generation.GenerateurParToken;
import moteur.pretraitement.Pretraiteur;
import moteur.pretraitement.PretraiteurAvecTokenisation;
import moteur.pretraitement.PretraiteurSansAccents;
import moteur.pretraitement.PretraiteurSansCaracteresNiChiffres;
import moteur.pretraitement.PretraiteurToLowerCase;
import moteur.selection.Selectionneur;
import moteur.selection.SelectionneurParSeuil;

import java.util.ArrayList;
import java.util.List;

/** Les stratégies utilisées par le moteur à chaque étape du pipeline. */
public class Configuration {
    private List<Pretraiteur> pretraiteurs;
    private GenerateurDeCandidat generateur;
    private Comparateur comparateur;
    private Selectionneur selectionneur;

    public Configuration(List<Pretraiteur> pretraiteurs, GenerateurDeCandidat generateur,
                         Comparateur comparateur, Selectionneur selectionneur) {
        this.pretraiteurs = new ArrayList<>(pretraiteurs);
        this.generateur = generateur;
        this.comparateur = comparateur;
        this.selectionneur = selectionneur;
    }

    /** Minuscules, sans accents, sans ponctuation, mots triés ; index par mot ; Jaro-Winkler >= 0.8. */
    public static Configuration parDefaut() {
        return new Configuration(
                List.of(new PretraiteurToLowerCase(),
                        new PretraiteurSansAccents(),
                        new PretraiteurSansCaracteresNiChiffres(),
                        new PretraiteurAvecTokenisation()),
                new GenerateurParToken(),
                new ComparateurDeNomParChaine(new ComparateurAvecJaroWinkler()),
                new SelectionneurParSeuil(0.8));
    }

    public List<Pretraiteur> getPretraiteurs() {
        return pretraiteurs;
    }

    public void setPretraiteurs(List<Pretraiteur> pretraiteurs) {
        this.pretraiteurs = new ArrayList<>(pretraiteurs);
    }

    public GenerateurDeCandidat getGenerateur() {
        return generateur;
    }

    public void setGenerateur(GenerateurDeCandidat generateur) {
        this.generateur = generateur;
    }

    public Comparateur getComparateur() {
        return comparateur;
    }

    public void setComparateur(Comparateur comparateur) {
        this.comparateur = comparateur;
    }

    public Selectionneur getSelectionneur() {
        return selectionneur;
    }

    public void setSelectionneur(Selectionneur selectionneur) {
        this.selectionneur = selectionneur;
    }
}
