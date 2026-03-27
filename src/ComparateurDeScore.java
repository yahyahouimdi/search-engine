import java.util.Comparator;

public class ComparateurDeScore implements Comparator<NomAvecScore>{

	@Override
	public int compare(NomAvecScore n1, NomAvecScore n2) {
		
		return Double.compare(n1.getScore(),n2.getScore());
	}

}
