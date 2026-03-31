package moteur.cli;

import moteur.Configuration;
import moteur.MoteurDeRecherche;
import moteur.comparaison.ComparateurAvecJaroWinkler;
import moteur.comparaison.ComparateurAvecLevenshtein;
import moteur.comparaison.ComparateurDeNomParChaine;
import moteur.comparaison.ComparateurSimple;
import moteur.generation.GenerateurParTaille;
import moteur.generation.GenerateurParToken;
import moteur.modele.CoupleDeString;
import moteur.modele.ListeDesNoms;
import moteur.modele.Nom;
import moteur.modele.NomAvecScore;
import moteur.pretraitement.Pretraiteur;
import moteur.pretraitement.PretraiteurAvecTokenisation;
import moteur.pretraitement.PretraiteurSansAccents;
import moteur.pretraitement.PretraiteurSansCaracteresNiChiffres;
import moteur.pretraitement.PretraiteurToLowerCase;
import moteur.recuperation.Datasets;
import moteur.recuperation.RecuperateurFichierCsv;
import moteur.selection.SelectionneurParSeuil;
import moteur.util.AnalyseMemoire;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Interface en ligne de commande.
 * Usage : {@code Main [fichierPrincipal.csv] [fichierSecondaire.csv]}
 */
public class Main {
    private static final int RESULTATS_AFFICHES = 50;

    private final Scanner scanner = new Scanner(System.in);
    private final MoteurDeRecherche moteur = new MoteurDeRecherche(Configuration.parDefaut());

    private String fichierPrincipal;
    private String fichierSecondaire;
    private ListeDesNoms listePrincipale;
    private ListeDesNoms listeSecondaire;

    private String nomComparateur = "Jaro-Winkler";
    private String nomGenerateur = "Par mot (token)";
    private String nomPretraitements = "minuscules, accents, ponctuation, tri des mots";
    private double seuil = 0.8;

    public Main(String fichierPrincipal, String fichierSecondaire) {
        this.fichierPrincipal = fichierPrincipal;
        this.fichierSecondaire = fichierSecondaire;
    }

    public static void main(String[] args) {
        String principal = args.length > 0 ? args[0] : "peps_names_8k.csv";
        String secondaire = args.length > 1 ? args[1] : "peps_names_2k.csv";
        new Main(principal, secondaire).lancer();
    }

    private void lancer() {
        boolean running = true;
        while (running) {
            afficherEnTete();
            System.out.println("  1. Rechercher un nom dans le fichier principal");
            System.out.println("  2. Comparer le fichier principal au fichier secondaire");
            System.out.println("  3. Dédupliquer le fichier principal");
            System.out.println("  4. Configurer la recherche");
            System.out.println("  5. Changer de fichiers");
            System.out.println("  0. Quitter");

            switch (lireEntier("Votre choix : ")) {
                case 1 -> {
                    String nom = lireTexte("Nom à rechercher : ");
                    if (nom.isBlank()) {
                        System.out.println("Nom vide, recherche annulée.");
                        break;
                    }
                    ListeDesNoms requete = new ListeDesNoms(List.of(new CoupleDeString("REQUETE", nom)));
                    executer("la recherche", () -> moteur.rechercher(requete, principale()));
                }
                case 2 -> executer("la comparaison", () -> moteur.comparer(principale(), secondaire()));
                case 3 -> executer("la déduplication", () -> moteur.dedupliquer(principale()));
                case 4 -> configurer();
                case 5 -> changerFichiers();
                case 0 -> {
                    System.out.println("Au revoir !");
                    running = false;
                }
                default -> System.out.println("Option invalide !");
            }
        }
    }

    private void afficherEnTete() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("            MOTEUR DE RECHERCHE DE NOMS");
        System.out.println("==================================================");
        System.out.println("  Fichier principal  : " + fichierPrincipal);
        System.out.println("  Fichier secondaire : " + fichierSecondaire);
        System.out.printf(Locale.ROOT, "  Comparateur        : %s (seuil %.2f)%n", nomComparateur, seuil);
        System.out.println("  Générateur         : " + nomGenerateur);
        System.out.println("  Prétraitements     : " + nomPretraitements);
        System.out.println("--------------------------------------------------");
    }

    private void executer(String etape, Supplier<List<NomAvecScore>> action) {
        try {
            long debut = System.currentTimeMillis();
            List<NomAvecScore> resultats = action.get();
            long duree = System.currentTimeMillis() - debut;
            afficher(resultats);
            System.out.println("Temps total : " + duree + " ms");
            AnalyseMemoire.afficherMemoire("après " + etape);
        } catch (RuntimeException e) {
            System.out.println("Erreur pendant " + etape + " : " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.out.println("Mémoire insuffisante : essayez un fichier plus petit ou augmentez -Xmx.");
        }
    }

    private void afficher(List<NomAvecScore> resultats) {
        if (resultats.isEmpty()) {
            System.out.println("Aucun résultat trouvé.");
            return;
        }
        System.out.println();
        System.out.printf("%-6s  %-28s  %-34s  %-34s%n", "Score", "ID du nom 2", "Nom 1", "Nom 2");
        System.out.println("-".repeat(108));
        for (NomAvecScore resultat : resultats.subList(0, Math.min(RESULTATS_AFFICHES, resultats.size()))) {
            Nom nom1 = resultat.getCouple().getNom1();
            Nom nom2 = resultat.getCouple().getNom2();
            System.out.printf(Locale.ROOT, "%.4f  %-28s  %-34s  %-34s%n",
                    resultat.getScore(), tronquer(nom2.getId(), 28),
                    tronquer(nom1.getNomOriginal(), 34), tronquer(nom2.getNomOriginal(), 34));
        }
        if (resultats.size() > RESULTATS_AFFICHES) {
            System.out.println("... et " + (resultats.size() - RESULTATS_AFFICHES) + " autres résultats");
        }
        System.out.println(resultats.size() + " résultat(s).");
    }

    private void configurer() {
        boolean configActive = true;
        while (configActive) {
            System.out.println("\n--- Configuration ---");
            System.out.println("  1. Modifier les prétraitements");
            System.out.println("  2. Choisir le générateur de candidats");
            System.out.println("  3. Choisir le comparateur de chaînes");
            System.out.println("  4. Modifier le seuil de similarité");
            System.out.println("  0. Retour au menu principal");

            switch (lireEntier("Votre choix : ")) {
                case 1 -> choisirPretraitements();
                case 2 -> {
                    System.out.println("  1. Par mot commun (rapide, recommandé)");
                    System.out.println("  2. Par taille (longueurs proches)");
                    if (lireEntier("Générateur : ") == 2) {
                        moteur.getConfiguration().setGenerateur(new GenerateurParTaille());
                        nomGenerateur = "Par taille";
                    } else {
                        moteur.getConfiguration().setGenerateur(new GenerateurParToken());
                        nomGenerateur = "Par mot (token)";
                    }
                }
                case 3 -> {
                    System.out.println("  1. Jaro-Winkler");
                    System.out.println("  2. Levenshtein");
                    System.out.println("  3. Égalité stricte");
                    switch (lireEntier("Comparateur : ")) {
                        case 1 -> {
                            moteur.getConfiguration().setComparateur(new ComparateurDeNomParChaine(new ComparateurAvecJaroWinkler()));
                            nomComparateur = "Jaro-Winkler";
                        }
                        case 2 -> {
                            moteur.getConfiguration().setComparateur(new ComparateurDeNomParChaine(new ComparateurAvecLevenshtein()));
                            nomComparateur = "Levenshtein";
                        }
                        case 3 -> {
                            moteur.getConfiguration().setComparateur(new ComparateurSimple());
                            nomComparateur = "Égalité stricte";
                        }
                        default -> System.out.println("Comparateur inconnu !");
                    }
                }
                case 4 -> {
                    double nouveauSeuil = lireDecimal("Nouveau seuil entre 0 et 1 (ex: 0.8) : ");
                    if (nouveauSeuil < 0 || nouveauSeuil > 1) {
                        System.out.println("Le seuil doit être compris entre 0 et 1.");
                    } else {
                        seuil = nouveauSeuil;
                        moteur.getConfiguration().setSelectionneur(new SelectionneurParSeuil(seuil));
                    }
                }
                case 0 -> configActive = false;
                default -> System.out.println("Option invalide !");
            }
        }
    }

    private void choisirPretraitements() {
        System.out.println("  1. Minuscules");
        System.out.println("  2. Suppression des accents");
        System.out.println("  3. Suppression de la ponctuation et des chiffres");
        System.out.println("  4. Tri des mots (token sort)");
        String[] choix = lireTexte("Vos choix dans l'ordre, séparés par des virgules (ex: 1,2,3,4) : ").split(",");

        List<Pretraiteur> pretraiteurs = new ArrayList<>();
        List<String> noms = new ArrayList<>();
        for (String option : choix) {
            switch (option.trim()) {
                case "1" -> { pretraiteurs.add(new PretraiteurToLowerCase()); noms.add("minuscules"); }
                case "2" -> { pretraiteurs.add(new PretraiteurSansAccents()); noms.add("accents"); }
                case "3" -> { pretraiteurs.add(new PretraiteurSansCaracteresNiChiffres()); noms.add("ponctuation"); }
                case "4" -> { pretraiteurs.add(new PretraiteurAvecTokenisation()); noms.add("tri des mots"); }
                case "" -> { }
                default -> System.out.println("Option inconnue : " + option.trim());
            }
        }
        moteur.getConfiguration().setPretraiteurs(pretraiteurs);
        nomPretraitements = noms.isEmpty() ? "aucun" : String.join(", ", noms);
    }

    private void changerFichiers() {
        List<Path> disponibles = Datasets.lister();
        System.out.println("Fichiers disponibles :");
        for (int i = 0; i < disponibles.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + disponibles.get(i).getFileName());
        }
        System.out.println("(numéro de la liste ou chemin vers un autre CSV, vide = inchangé)");

        String principal = choisirFichier("Fichier principal : ", disponibles);
        if (principal != null) {
            fichierPrincipal = principal;
            listePrincipale = null;
        }
        String secondaire = choisirFichier("Fichier secondaire : ", disponibles);
        if (secondaire != null) {
            fichierSecondaire = secondaire;
            listeSecondaire = null;
        }
    }

    private String choisirFichier(String invite, List<Path> disponibles) {
        String saisie = lireTexte(invite).trim();
        if (saisie.isEmpty()) {
            return null;
        }
        try {
            int index = Integer.parseInt(saisie) - 1;
            if (index >= 0 && index < disponibles.size()) {
                return disponibles.get(index).toString();
            }
            System.out.println("Numéro invalide, fichier inchangé.");
            return null;
        } catch (NumberFormatException e) {
            return saisie;
        }
    }

    private ListeDesNoms principale() {
        if (listePrincipale == null) {
            listePrincipale = charger(fichierPrincipal);
        }
        return listePrincipale;
    }

    private ListeDesNoms secondaire() {
        if (listeSecondaire == null) {
            listeSecondaire = charger(fichierSecondaire);
        }
        return listeSecondaire;
    }

    private static ListeDesNoms charger(String fichier) {
        System.out.println("Chargement de " + fichier + "...");
        ListeDesNoms liste = new ListeDesNoms(new RecuperateurFichierCsv(fichier).recuperer());
        System.out.println(liste.liste().size() + " noms chargés.");
        return liste;
    }

    private int lireEntier(String invite) {
        while (true) {
            String saisie = lireTexte(invite).trim();
            try {
                return Integer.parseInt(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier.");
            }
        }
    }

    private double lireDecimal(String invite) {
        while (true) {
            String saisie = lireTexte(invite).trim().replace(',', '.');
            try {
                return Double.parseDouble(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre (ex: 0.85).");
            }
        }
    }

    private String lireTexte(String invite) {
        System.out.print(invite);
        if (!scanner.hasNextLine()) {
            System.exit(0); // entrée standard fermée
        }
        return scanner.nextLine().replace("﻿", ""); // BOM éventuel en entrée redirigée
    }

    private static String tronquer(String texte, int max) {
        return texte.length() <= max ? texte : texte.substring(0, max - 3) + "...";
    }
}
