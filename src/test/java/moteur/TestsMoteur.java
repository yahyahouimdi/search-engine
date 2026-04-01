package moteur;

import moteur.comparaison.ComparateurAvecJaroWinkler;
import moteur.comparaison.ComparateurAvecLevenshtein;
import moteur.generation.GenerateurParTaille;
import moteur.generation.GenerateurParToken;
import moteur.modele.CoupleDeString;
import moteur.modele.ListeDesNoms;
import moteur.modele.NomAvecScore;
import moteur.pretraitement.PretraiteurAvecTokenisation;
import moteur.pretraitement.PretraiteurSansAccents;
import moteur.pretraitement.PretraiteurSansCaracteresNiChiffres;
import moteur.pretraitement.PretraiteurToLowerCase;
import moteur.recuperation.RecuperateurFichierCsv;
import moteur.selection.SelectionneurDesNmeilleurs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests sans dépendance externe : {@code java -cp out/classes:out/test-classes moteur.TestsMoteur}.
 * Le code de sortie vaut 1 si au moins un test échoue.
 */
public class TestsMoteur {
    private static int reussis;
    private static final List<String> echecs = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        test("Jaro-Winkler : exemple de référence MARTHA/MARHTA", () ->
                proche(0.9611, new ComparateurAvecJaroWinkler().comparer("martha", "marhta")));
        test("Jaro-Winkler : chaînes identiques et vides", () ->
                new ComparateurAvecJaroWinkler().comparer("abc", "abc") == 1.0
                        && new ComparateurAvecJaroWinkler().comparer("", "abc") == 0.0);
        test("Levenshtein : kitten/sitting = 3 modifications", () ->
                ComparateurAvecLevenshtein.distanceLevenshtein("kitten", "sitting") == 3
                        && proche(1 - 3.0 / 7, new ComparateurAvecLevenshtein().comparer("kitten", "sitting")));

        test("Prétraitement : minuscules", () ->
                new PretraiteurToLowerCase().pretraiter("Houda AMER").equals("houda amer"));
        test("Prétraitement : accents", () ->
                new PretraiteurSansAccents().pretraiter("Hélène Müller").equals("Helene Muller"));
        test("Prétraitement : ponctuation et chiffres", () ->
                new PretraiteurSansCaracteresNiChiffres().pretraiter("Boois,Dawid (2) O'Brien").equals("Boois Dawid OBrien"));
        test("Prétraitement : tri des mots", () ->
                new PretraiteurAvecTokenisation().pretraiter("  dawid   boois ").equals("boois dawid"));

        test("CSV : guillemets, virgules et lignes incomplètes", () -> {
            Path fichier = Files.createTempFile("noms", ".csv");
            Files.writeString(fichier, "id,name\nA-1,\"Boois, Dawid\"\nA-2,Simple\nA-3\n", StandardCharsets.UTF_8);
            List<CoupleDeString> lignes = new RecuperateurFichierCsv(fichier.toString()).recuperer();
            Files.delete(fichier);
            return lignes.size() == 2
                    && lignes.get(0).getElement1().equals("A-1")
                    && lignes.get(0).getElement2().equals("Boois, Dawid");
        });

        ListeDesNoms reference = new ListeDesNoms(List.of(
                new CoupleDeString("NK-1", "Mustapha Abubakar"),
                new CoupleDeString("NK-2", "ABUBAKAR, Mustapha"),
                new CoupleDeString("NK-3", "Vazha Todua"),
                new CoupleDeString("NK-4", "Hélène Dupont")));

        test("Recherche : retrouve un nom mal orthographié, ID intact", () -> {
            List<NomAvecScore> r = new MoteurDeRecherche(Configuration.parDefaut())
                    .rechercher(liste("Q", "Mustafa Abubakr"), reference);
            return !r.isEmpty() && r.get(0).getCouple().getNom2().getId().equals("NK-1")
                    && r.get(0).getCouple().getNom2().getNomOriginal().equals("Mustapha Abubakar");
        });
        test("Recherche : accents ignorés", () -> {
            List<NomAvecScore> r = new MoteurDeRecherche(Configuration.parDefaut())
                    .rechercher(liste("Q", "helene dupont"), reference);
            return r.size() == 1 && r.get(0).getScore() == 1.0;
        });
        test("Recherche : générateur par taille", () -> {
            Configuration config = Configuration.parDefaut();
            config.setGenerateur(new GenerateurParTaille());
            List<NomAvecScore> r = new MoteurDeRecherche(config).rechercher(liste("Q", "Vazha Todua"), reference);
            return r.size() == 1 && r.get(0).getCouple().getNom2().getId().equals("NK-3");
        });
        test("Déduplication : un seul couple, sans auto-correspondance", () -> {
            List<NomAvecScore> r = new MoteurDeRecherche(Configuration.parDefaut()).dedupliquer(reference);
            return r.size() == 1
                    && r.get(0).getCouple().getNom1().getId().equals("NK-1")
                    && r.get(0).getCouple().getNom2().getId().equals("NK-2");
        });
        test("Comparaison : résultats triés par score décroissant", () -> {
            Configuration config = Configuration.parDefaut();
            config.setGenerateur(new GenerateurParToken());
            List<NomAvecScore> r = new MoteurDeRecherche(config).comparer(
                    new ListeDesNoms(List.of(new CoupleDeString("B-1", "Mustafa Abubakar"),
                            new CoupleDeString("B-2", "Vazha Todua"))), reference);
            for (int i = 1; i < r.size(); i++) {
                if (r.get(i - 1).getScore() < r.get(i).getScore()) {
                    return false;
                }
            }
            return r.size() >= 3;
        });
        test("Sélection des N meilleurs : N plus grand que la liste", () ->
                new SelectionneurDesNmeilleurs(10).selectionner(new ArrayList<>()).isEmpty());

        System.out.println();
        System.out.println(reussis + " test(s) réussi(s), " + echecs.size() + " échec(s)");
        if (!echecs.isEmpty()) {
            echecs.forEach(e -> System.out.println("  ECHEC : " + e));
            System.exit(1);
        }
    }

    private static ListeDesNoms liste(String id, String nom) {
        return new ListeDesNoms(List.of(new CoupleDeString(id, nom)));
    }

    private static boolean proche(double attendu, double obtenu) {
        return Math.abs(attendu - obtenu) < 1e-3;
    }

    private interface Verification {
        boolean verifier() throws Exception;
    }

    private static void test(String nom, Verification verification) {
        boolean ok;
        try {
            ok = verification.verifier();
        } catch (Exception | AssertionError e) {
            ok = false;
            nom += " (" + e + ")";
        }
        System.out.println((ok ? "  OK     " : "  ECHEC  ") + nom);
        if (ok) {
            reussis++;
        } else {
            echecs.add(nom);
        }
    }
}
