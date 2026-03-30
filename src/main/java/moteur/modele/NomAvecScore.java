package moteur.modele;

public class NomAvecScore {
    private final CoupleDeNom couple;
    private final double score;

    public NomAvecScore(CoupleDeNom couple, double score) {
        this.couple = couple;
        this.score = score;
    }

    public CoupleDeNom getCouple() {
        return couple;
    }

    public double getScore() {
        return score;
    }
}
