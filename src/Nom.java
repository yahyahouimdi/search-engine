
public class Nom {
	private final String id;
	private String nom;
	private String nomComposee;
	public Nom(String id, String nom, String nomComposee) {
		super();
		this.id = id;
		this.nom = nom;
		this.nomComposee = nomComposee;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getNomComposee() {
		return nomComposee;
	}
	public void setNomComposee(String nomComposee) {
		this.nomComposee = nomComposee;
	}
	public String getId() {
		return id;
	}
	
	
}
