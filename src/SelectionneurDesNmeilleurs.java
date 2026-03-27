import java.util.Collections;
import java.util.List;

public class SelectionneurDesNmeilleurs implements Selectionneur {
	private int N;

	public SelectionneurDesNmeilleurs(int n) {
		super();
		N = n;
	}

	@Override
	public List<NomAvecScore> selectionner(List<NomAvecScore> list) {
		
		Collections.sort(list, new ComparateurDeScore());
        Collections.reverse(list);
		
		return list.subList(0, N);
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}
	
	

}
