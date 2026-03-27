import java.util.ArrayList;
import java.util.List;

public class mainPourGenerateur {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // Chargement du fichier CSV
        Recuperateurfichecsv r = new Recuperateurfichecsv("peps_names_16k.csv");
        List<CoupleDeString> liste = r.recuperer();

        // Encapsuler dans ListeDesNoms
        ListeDesNoms listNom = new ListeDesNoms(liste);

        // Liste de prétraiteurs
        List<Pretraiteur> p = new ArrayList<>();
        p.add(new PretraiteurToLowerCase());
        p.add(new PretraPretraiteurSansCaracteresNiChiffres());
        p.add(new PretraiteurAvecTokenisation());

        // Prétraitement en chaîne
        ListePretraiter listCSV = null;
        for (Pretraiteur pretraiteur : p) {
            listCSV = pretraiteur.pretraiter(listNom);
            listNom = new ListeDesNoms(listCSV.list());
        }

        // Générateur de candidats
        GenerateurDeCandidat g = new GenerateurParTaille();

        // Générer les couples de noms à comparer (supposons que c'est l'objectif)
        List<CoupleDeString> nomsAchercher = new ArrayList<>();
        nomsAchercher.add(new CoupleDeString("NK-SZvshqfuEf7jn269Qyspv2", "Mustapha Abubakar"));
        ListePretraiter list1=new ListePretraiter(nomsAchercher);
        ListeGeneree list = g.generer(list1,listCSV);

        // Pour test : afficher les résultats générés
        for (CoupleDeNom couple : list.liste()) {
            System.out.println("nom1="+couple.getNom1().getNom()+"nom2="+couple.getNom2().getNom());
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total time: "+ totalTime +" ms");
        AnalyseMemoire.afficherMemoire("après la recherche");


    }
}