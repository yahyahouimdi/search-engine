package moteur.modele;

/** Une ligne brute du fichier : element1 = identifiant, element2 = nom. */
public class CoupleDeString {
    private final String element1;
    private final String element2;

    public CoupleDeString(String element1, String element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    public String getElement1() {
        return element1;
    }

    public String getElement2() {
        return element2;
    }
}
