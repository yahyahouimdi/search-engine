import java.util.List;

public class Configuration {
	private List<Pretraiteur> pretraiteurs;
	private  GenerateurDeCandidat generateur;
	private ComparateurDeChaine comparateurDeChaine;
	protected   Comparateur comparateurNom;
	private  Selectionneur selectionneur;
	private  double seuil;

	public Configuration(List<Pretraiteur> pretraiteurs, GenerateurDeCandidat generateur, ComparateurDeChaine comparateurDeChaine, Comparateur comparateurNom, Selectionneur selectionneur, double seuil) {
		this.pretraiteurs = pretraiteurs;
		this.generateur = generateur;
		this.comparateurDeChaine = comparateurDeChaine;
		this.comparateurNom = comparateurNom;
		this.selectionneur = selectionneur;
		this.seuil = seuil;
	}

	public GenerateurDeCandidat getGenerateur() {
		return generateur;
	}

	public List<Pretraiteur> getPretraiteurs() {
		return pretraiteurs;
	}

	public Comparateur getComparateurNom() {
		return comparateurNom;
	}

	public Selectionneur getSelectionneur() {
		return selectionneur;
	}

	public double getSeuil() {
		return seuil;
	}

	public void setGenerateur(GenerateurDeCandidat generateur) {
		this.generateur = generateur;
	}

	public void setPretraiteurs(List<Pretraiteur> pretraiteurs) {
		this.pretraiteurs = pretraiteurs;
	}

	public void setComparateurNom(Comparateur comparateurNom) {
		this.comparateurNom = comparateurNom;
	}

	public void setSelectionneur(Selectionneur selectionneur) {
		this.selectionneur = selectionneur;
	}

	public void setSeuil(double seuil) {
		this.seuil = seuil;
	}

	public ComparateurDeChaine getComparateurDeChaine() {
		return comparateurDeChaine;
	}

	public void setComparateurDeChaine(ComparateurDeChaine comparateurDeChaine) {
		this.comparateurDeChaine = comparateurDeChaine;
	}
}

