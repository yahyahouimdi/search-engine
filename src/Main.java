

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Recuperateurfichecsv r = new Recuperateurfichecsv("peps_names_658k.csv");
        Recuperateurfichecsv r1 = new Recuperateurfichecsv("peps_names_32k.csv");


        List<CoupleDeString> liste = r.recuperer();
        List<CoupleDeString> liste1 = r1.recuperer();



        ListeDesNoms listCSV=new ListeDesNoms(liste);
        ListeDesNoms listCSV1 =new ListeDesNoms(liste1);



        List<Pretraiteur> p = new ArrayList<>();
        p.add(new PretraiteurToLowerCase());
        p.add(new PretraiteurAvecTokenisation());
        p.add(new PretraPretraiteurSansCaracteresNiChiffres());


        GenerateurDeCandidat g = new Generateur1();




        ComparateurDeChaine CompChaine=new ComparateurAvecJaroWinkler() ;
        ComparateurJaro c = new ComparateurJaro(CompChaine);


        Selectionneur s = new SelectionneurParSeuil(0.8);



        Configuration configuration=new Configuration(p,g,CompChaine,c,s,0.8);



        // Liste des noms à chercher
        List<CoupleDeString> nomsAchercher = new ArrayList<>();
        nomsAchercher.add(new CoupleDeString("NK-SZvshqfuEf7jn269Qyspv2", "Houda Amer "));

        // Création d'un objet ListeDesNoms
        ListeDesNoms listAchercher = new ListeDesNoms(nomsAchercher);



        MoteurDeRecherche moteur = new MoteurDeRecherche(configuration);



        Scanner scanner = new Scanner(System.in);





        boolean running = true;
        while (running) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1. Recherche dans un fichier");
            System.out.println("2. Comparaison de deux fichiers");
            System.out.println("3. Déduplication d’un fichier");
            System.out.println("4. Configurer la recherche");
            System.out.println("5. Quitter");
            System.out.print("Votre choix : ");
            int choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> {
                    List<NomAvecScore> resultats = moteur.rechercher(listAchercher, listCSV,scanner);
                    afficher(resultats);
                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;
                    System.out.println("Total time: "+totalTime+" ms");
                    AnalyseMemoire.afficherMemoire("après la recherche");
                }

                case 2 -> {
                    List<NomAvecScore> resultats = moteur.comparer(listCSV,listCSV1, scanner);
                    afficher(resultats);
                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;
                    System.out.println("Total time: "+totalTime+" ms");
                    AnalyseMemoire.afficherMemoire("après la comparaison");
                }
                case 3 -> {
                    List<NomAvecScore> resultats = moteur.Dedupliquer(listCSV, scanner);
                    afficher(resultats);
                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;
                    System.out.println("Total time: "+totalTime+" ms");
                    AnalyseMemoire.afficherMemoire("après la déduplication");
                }
                case 4 ->
                        moteur.configurerParametres(scanner);
                case 5 -> {
                    System.out.println(" Au revoir !");
                    running = false;
                }
                default -> System.out.println(" Option invalide !");
            }
        }

        scanner.close();















        // Initialisation du moteur de recherche



        // Recherche




    }

    private static void afficher(List<NomAvecScore> resultats) {
        if (resultats != null && !resultats.isEmpty()) {
            for (NomAvecScore resultat : resultats) {
                System.out.println("id1=" + resultat.getCouple().getNom1().getId() + resultat.getCouple().getNom1().getNom() + " <-> " + "id2=" + resultat.getCouple().getNom2().getId() + resultat.getCouple().getNom2().getNom() +
                        resultat.getCouple().getNom2().getNom() + " : " +
                        resultat.getScore());
            }
        } else {
            System.out.println("Aucun résultat trouvé.");
        }
    }
}




