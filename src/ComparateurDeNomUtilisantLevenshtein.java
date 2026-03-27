
public class ComparateurDeNomUtilisantLevenshtein implements Comparateur{
    private ComparateurDeChaine c;

    public ComparateurDeNomUtilisantLevenshtein(ComparateurDeChaine c) {
        super();
        this.c = c;

    }
    @Override
    public double comparer(Nom nom1, Nom nom2) {

        return c.comparer(nom1.getNom(), nom2.getNom());
    }










}
