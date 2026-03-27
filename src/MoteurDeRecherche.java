import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class MoteurDeRecherche {
    private Configuration config;

    public MoteurDeRecherche(Configuration configuration) {
        this.config = configuration;
    }

    public List<NomAvecScore> rechercher(ListeDesNoms listAchercher, ListeDesNoms listNom,Scanner scanner) {

        List<NomAvecScore> candidats = new ArrayList<>();

        ListePretraiter listCSV = new ListePretraiter(listNom.liste());
        ListePretraiter listAcherchee = new ListePretraiter(listAchercher.liste());

        for (int i = 0; i < config.getPretraiteurs().size(); i++) {
            listCSV = config.getPretraiteurs().get(i).pretraiter(listNom);
            listNom = new ListeDesNoms(listCSV.list());
            listAcherchee = config.getPretraiteurs().get(i).pretraiter(listAchercher);
            listAchercher = new ListeDesNoms(listAcherchee.list());
        }

        ListeGeneree couplesGeneres = config.getGenerateur().generer(listAcherchee, listCSV);

        for (CoupleDeNom nomAcomparee : couplesGeneres.liste()) {
            Nom nom1 = nomAcomparee.getNom1();
            Nom nom2 = nomAcomparee.getNom2();
            double score = config.getComparateurNom().comparer(nom1, nom2);
            candidats.add(new NomAvecScore(nomAcomparee, score));
        }

        return config.getSelectionneur().selectionner(candidats);
    }

    public List<NomAvecScore> Dedupliquer(ListeDesNoms listCSV,Scanner scanner) {
        return comparer(listCSV,listCSV, scanner);
    }

    public List<NomAvecScore> comparer(ListeDesNoms autreListe,ListeDesNoms list ,Scanner scanner) {
        List<NomAvecScore> candidats = new ArrayList<>();

        int total = autreListe.liste().size();
        int blockSize = total / 10;
        if (blockSize == 0) blockSize = 1;

        for (int blockStart = 0; blockStart < total; blockStart += blockSize) {
            int blockEnd = Math.min(blockStart + blockSize, total);
            List<CoupleDeString> sousListe = autreListe.liste().subList(blockStart, blockEnd);
            ListeDesNoms Arechercher = new ListeDesNoms(sousListe);
            candidats.addAll(rechercher(Arechercher, list,scanner));
        }

        return candidats;
    }

    public void configurerParametres(Scanner scanner) {
        boolean configActive = true;
        while (configActive) {
            System.out.println("\n--- Configuration ---");
            System.out.println("1. Modifier les prétraitements");
            System.out.println("2. Choisir une structure d'index");
            System.out.println("3. Choisir un comparateur de chaînes");
            System.out.println("4. Modifier le seuil de similarité");
            System.out.println("5. Retour au menu principal");
            System.out.print("Votre choix : ");
            int choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> {
                    config.getPretraiteurs().clear();
                    System.out.println("Prétraitements disponibles :");
                    System.out.println("1. Minuscule");
                    System.out.println("2. Token Sorting");
                    System.out.println("3. Soundex");
                    System.out.print("Entrez vos choix séparés par virgules : ");
                    String[] choixPre = scanner.nextLine().split(",");
                    for (String opt : choixPre) {
                        switch (opt.trim()) {
                            case "1" -> config.getPretraiteurs().add(new PretraiteurToLowerCase());
                            case "2" -> config.getPretraiteurs().add(new PretraiteurAvecTokenisation());
                            case "3" -> config.getPretraiteurs().add(new PretraPretraiteurSansCaracteresNiChiffres());
                            default -> System.out.println("Option inconnue : " + opt);
                        }
                    }
                }
                case 2 -> {
                    System.out.println("Structures disponibles :");
                    System.out.println("1. Par prefix");
                    System.out.println("2. Par Taille");
                    int choixIndex = scanner.nextInt();
                    scanner.nextLine();
                    config.setGenerateur((choixIndex == 2) ? new GenerateurParTaille() : new Generateur1());
                }
                case 3 -> {
                    System.out.println("Comparateurs disponibles :");
                    System.out.println("1. Levenshtein");
                    System.out.println("2. Jaro-Winkler");
                    System.out.println("3. Egalité");
                    int cmp = scanner.nextInt();
                    scanner.nextLine();
                    switch (cmp) {
                        case 1 -> {
                            config.setComparateurDeChaine(new ComparateurAvecLevenshtein());
                            config.setComparateurNom(new ComparateurDeNomUtilisantLevenshtein(config.getComparateurDeChaine()));
                        }
                        case 2 -> {
                            config.setComparateurDeChaine(new ComparateurAvecJaroWinkler());
                            config.setComparateurNom(new ComparateurJaro(config.getComparateurDeChaine()));
                        }
                        case 3 -> config.setComparateurNom(new ComparateurSimple());
                        default -> System.out.println("Comparateur inconnu !");
                    }
                }
                case 4 -> {
                    System.out.print("Nouveau seuil (ex: 0.8) : ");
                    double nouveauSeuil = scanner.nextDouble();
                    config.setSeuil(nouveauSeuil);
                    if (config.getSelectionneur() instanceof SelectionneurParSeuil selectionneurParSeuil) {
                        selectionneurParSeuil.setSeuil(nouveauSeuil);
                    }
                    scanner.nextLine();
                }
                case 5 -> configActive = false;
                default -> System.out.println("Option invalide !");
            }
        }
    }
}
