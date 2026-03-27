public class ComparateurAvecJaroWinkler implements ComparateurDeChaine {

    @Override
    public double comparer(String nom1, String nom2) {
    if (nom1 == null || nom2 == null) {
      return 0.0;
    }

    if (nom1.equals(nom2)) {
      return 1.0;
    }

    int len1 = nom1.length();
    int len2 = nom2.length();
    if (len1 == 0 || len2 == 0) {
      return 0.0;
    }

    int matchDistance = Math.max(len1, len2) / 2 - 1;
    if (matchDistance < 0) {
      matchDistance = 0;
    }

    boolean[] s1Matches = new boolean[len1];
    boolean[] s2Matches = new boolean[len2];

    int matches = 0;
    for (int i = 0; i < len1; i++) {
      int start = Math.max(0, i - matchDistance);
      int end = Math.min(i + matchDistance + 1, len2);
      for (int j = start; j < end; j++) {
        if (s2Matches[j]) {
          continue;
        }
        if (nom1.charAt(i) != nom2.charAt(j)) {
          continue;
        }
        s1Matches[i] = true;
        s2Matches[j] = true;
        matches++;
        break;
      }
    }

    if (matches == 0) {
      return 0.0;
    }

    double transpositions = 0;
    int k = 0;
    for (int i = 0; i < len1; i++) {
      if (!s1Matches[i]) {
        continue;
      }
      while (!s2Matches[k]) {
        k++;
      }
      if (nom1.charAt(i) != nom2.charAt(k)) {
        transpositions++;
      }
      k++;
    }
    transpositions /= 2.0;

    double m = matches;
    double jaro = (m / len1 + m / len2 + (m - transpositions) / m) / 3.0;

    int prefix = 0;
    int maxPrefix = Math.min(4, Math.min(len1, len2));
    for (int i = 0; i < maxPrefix; i++) {
      if (nom1.charAt(i) == nom2.charAt(i)) {
        prefix++;
      } else {
        break;
      }
    }

    double scalingFactor = 0.1;
    return jaro + prefix * scalingFactor * (1.0 - jaro);
    }
}
