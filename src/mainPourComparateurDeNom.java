import java.util.List;

public class mainPourComparateurDeNom {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		// Chargement des données à partir d'un fichier CSV
		Recuperateurfichecsv r = new Recuperateurfichecsv("peps_names_2k.csv");
		List<CoupleDeString> liste = r.recuperer();
		ListeDesNoms listNom = new ListeDesNoms(liste);

		// Nom de référence à comparer avec tous les autres
		Nom nomRecherche = new Nom("REF-ID", "Vazha Todua", null);

		// Appliquer les prétraitements (ex: minuscules)
		PretraiteurToLowerCase pretraiteur = new PretraiteurToLowerCase();
		ListeDesNoms temp = new ListeDesNoms(List.of(
				new CoupleDeString(nomRecherche.getId(), nomRecherche.getNom())
		));
		ListePretraiter pretraite = pretraiteur.pretraiter(temp);
		nomRecherche = new Nom(pretraite.list().get(0).getElement1(), pretraite.list().get(0).getElement2(), null);

		// Prétraiter les noms du fichier
		ListePretraiter listePretraitee = pretraiteur.pretraiter(listNom);
		List<CoupleDeString> nomsPretraite = listePretraitee.list();

		// Initialisation du comparateur
		ComparateurDeChaine comparateurChaine = new ComparateurAvecJaroWinkler();
		Comparateur comparateurNom = new ComparateurJaro(comparateurChaine);


		// Comparaison de chaque nom du fichier avec le nom recherché
		int i = 0;
		for (CoupleDeString c : nomsPretraite) {
			Nom nomActuel = new Nom(c.getElement1(), c.getElement2(), null);
			double score = comparateurNom.comparer(nomRecherche, nomActuel);
			System.out.printf("(%d) %s <-> %s => Score: %.4f\n", ++i, nomRecherche.getNom(), nomActuel.getNom(), score);
		}

		// Affichage du temps et de la mémoire
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Temps total: " + totalTime + " ms");

		AnalyseMemoire.afficherMemoire("Apres comparaison");
	}
}
