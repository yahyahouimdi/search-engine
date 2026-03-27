
public class ComparateurSimple implements Comparateur {

	public double comparer(Nom nom1, Nom nom2) {
		if(nom1.getNom().equals(nom2.getNom())) {
			return 1;
		}
		return 0;
		
	}
	
	
	
	


}
