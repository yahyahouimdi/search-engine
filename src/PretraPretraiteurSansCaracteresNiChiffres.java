import java.util.ArrayList;
import java.util.List;

public class PretraPretraiteurSansCaracteresNiChiffres implements Pretraiteur {

    @Override
    public ListePretraiter pretraiter(ListeDesNoms list) {
        List<CoupleDeString> originale = list.liste();
        List<CoupleDeString> nettoyee = new ArrayList<>();

        for (CoupleDeString couple : originale) {
            String chaine1 = nettoyer(couple.getElement1());
            String chaine2 = nettoyer(couple.getElement2());

            nettoyee.add(new CoupleDeString(chaine1, chaine2));
        }

        return new ListePretraiter(nettoyee);
    }


    private String nettoyer(String input) {
        return input.replaceAll("[^a-zA-Z\\s]", ""); // supprime tout sauf les lettres et les espaces
    }
}