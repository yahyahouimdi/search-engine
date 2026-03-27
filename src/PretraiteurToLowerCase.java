import java.util.List;
import java.util.ArrayList;

public class PretraiteurToLowerCase implements Pretraiteur {

    public ListePretraiter pretraiter(ListeDesNoms list) {
        List<CoupleDeString> listeOriginale = list.liste();
        List<CoupleDeString> listeModifiee = new ArrayList<>();

        for (CoupleDeString couple : listeOriginale) {
            String element1 = couple.getElement1();
            String element2 = couple.getElement2().toLowerCase();
            listeModifiee.add(new CoupleDeString(element1, element2));
        }
        

        return new ListePretraiter(listeModifiee);
    }
}
