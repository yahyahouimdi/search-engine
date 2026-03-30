package moteur.pretraitement;

import java.text.Normalizer;

/** Retire les accents : "Hélène Müller" devient "Helene Muller". */
public class PretraiteurSansAccents implements Pretraiteur {

    @Override
    public String pretraiter(String nom) {
        return Normalizer.normalize(nom, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
