
public class NomAvecScore {
	private CoupleDeNom couple;
	private double score;
	public NomAvecScore(CoupleDeNom couple, double score) {
		super();
		this.couple = couple;
		this.score = score;
	}
	public CoupleDeNom getCouple() {
		return couple;
	}
	public void setCouple(CoupleDeNom couple) {
		this.couple = couple;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	
}
