import java.util.List;
import java.util.ArrayList;

public class mainDePretraiteur {

    public static void main(String[] args) {
        // Chargement du fichier CSV
        Recuperateurfichecsv r = new Recuperateurfichecsv("peps_names_2k.csv");
        List<CoupleDeString> liste1 = r.recuperer();

        // Création de la liste initiale
        ListeDesNoms liste = new ListeDesNoms(liste1);

        // Définition des prétraiteurs
        List<Pretraiteur> p = new ArrayList<>();
        p.add(new PretraiteurToLowerCase());
        p.add(new PretraPretraiteurSansCaracteresNiChiffres());
        p.add(new PretraiteurAvecTokenisation());

        // Application successive des prétraiteurs
        ListePretraiter listCSV = null;
        for (Pretraiteur pretraiteur : p) {
            listCSV = pretraiteur.pretraiter(liste); // applique le prétraitement
            liste = new ListeDesNoms(listCSV.list()); // met à jour l'entrée pour le prochain
        }

        // Affichage des résultats
        System.out.println("Résultat après tous les prétraitements :");
        for (CoupleDeString c : listCSV.list()) {
            System.out.println(c.getElement1() + " | " + c.getElement2());
        }
    }
}
