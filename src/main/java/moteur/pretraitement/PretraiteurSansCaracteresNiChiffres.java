package moteur.pretraitement;

/**
 * Supprime tout sauf les lettres (accentuées comprises) et normalise les espaces.
 * Les apostrophes sont retirées ("O'Brien" -> "OBrien"), les autres séparateurs
 * deviennent des espaces ("Boois,Dawid" -> "Boois Dawid").
 */
public class PretraiteurSansCaracteresNiChiffres implements Pretraiteur {

    @Override
    public String pretraiter(String nom) {
        return nom.replaceAll("['’`]", "")
                .replaceAll("[^\\p{L}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
