import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PretraiteurAvecTokenisation implements Pretraiteur {

    @Override
    public ListePretraiter pretraiter(ListeDesNoms list) {
        List<CoupleDeString> Originale = list.liste();
        List<CoupleDeString> finale = new ArrayList<>();

        for (CoupleDeString couple : Originale) {
            String chaine1 = couple.getElement1();
            String chaine2 = couple.getElement2();

            // Tokenize, sort and rejoin
            String[] tokens = chaine2.split("\\s+");
            Arrays.sort(tokens);
            String sorted = String.join(" ", tokens);

            finale.add(new CoupleDeString(chaine1, sorted));
        }

        return new ListePretraiter(finale);
    }
}