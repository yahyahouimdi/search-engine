package moteur.comparaison;

/** Score = 1 - distance d'édition / longueur du plus long nom. */
public class ComparateurAvecLevenshtein implements ComparateurDeChaine {

    @Override
    public double comparer(String nom1, String nom2) {
        if (nom1 == null || nom2 == null) {
            return 0.0;
        }
        int maxLength = Math.max(nom1.length(), nom2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        return 1.0 - ((double) distanceLevenshtein(nom1, nom2) / maxLength);
    }

    /** Programmation dynamique sur deux lignes seulement (mémoire O(m)). */
    public static int distanceLevenshtein(String s1, String s2) {
        int n = s1.length();
        int m = s2.length();
        int[] precedente = new int[m + 1];
        int[] courante = new int[m + 1];

        for (int j = 0; j <= m; j++) {
            precedente[j] = j;
        }

        for (int i = 1; i <= n; i++) {
            courante[0] = i;
            for (int j = 1; j <= m; j++) {
                int coutSubstitution = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                courante[j] = Math.min(
                        Math.min(precedente[j] + 1, courante[j - 1] + 1),
                        precedente[j - 1] + coutSubstitution
                );
            }
            int[] temp = precedente;
            precedente = courante;
            courante = temp;
        }

        return precedente[m];
    }
}
