package moteur.pretraitement;

import java.util.Locale;

public class PretraiteurToLowerCase implements Pretraiteur {

    @Override
    public String pretraiter(String nom) {
        return nom.toLowerCase(Locale.ROOT);
    }
}
