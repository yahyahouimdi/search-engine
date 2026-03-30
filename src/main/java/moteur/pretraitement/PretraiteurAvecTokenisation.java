package moteur.pretraitement;

import java.util.Arrays;

/** Trie les mots du nom : "Dawid Boois" et "Boois Dawid" deviennent identiques. */
public class PretraiteurAvecTokenisation implements Pretraiteur {

    @Override
    public String pretraiter(String nom) {
        String[] tokens = nom.trim().split("\\s+");
        Arrays.sort(tokens);
        return String.join(" ", tokens);
    }
}
