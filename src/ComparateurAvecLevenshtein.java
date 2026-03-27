public class ComparateurAvecLevenshtein implements ComparateurDeChaine {

    @Override
    public double comparer(String nom1, String nom2) {
        if (nom1 == null || nom2 == null) {
            return 0.0;
        }

        int distance = distanceLevenshtein(nom1, nom2);

        int maxLength = Math.max(nom1.length(), nom2.length());
        double score = (maxLength == 0) ? 1.0 : 1.0 - ((double) distance / maxLength);
        return score;
    }

    private int distanceLevenshtein(String s1, String s2) {
        int n = s1.length();
        int m = s2.length();

        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int coutSubstitution = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + coutSubstitution
                );
            }
        }

        return dp[n][m];
    }
}
