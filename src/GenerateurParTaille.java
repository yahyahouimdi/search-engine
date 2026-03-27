

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GenerateurParTaille implements GenerateurDeCandidat {



    public Map<Integer, List<CoupleDeString>> indexer(ListePretraiter listeNoms) {
        Map<Integer, List<CoupleDeString>> index = new HashMap<>();

        for (CoupleDeString couple : listeNoms.list()) {
            String chaine2 = couple.getElement2().trim();
            if (chaine2.isEmpty()) continue;

            int key = chaine2.length(); // Clé = longueur en caractères
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(couple);
        }

        return index;
    }


    public int extraireCle(String chaine) {
        return chaine.trim().length(); // Retourne directement la longueur
    }


    public ListeGeneree generer(ListePretraiter aChercher, ListePretraiter csv) {
        Map<Integer, List<CoupleDeString>> index1 = indexer(aChercher);
        Map<Integer, List<CoupleDeString>> index2 = indexer(csv);
        Map<Integer, List<CoupleDeString>> result = new HashMap<>();
        List<CoupleDeString> candidats = new ArrayList<>();
        List<CoupleDeNom> couples = new ArrayList<>();


        for(int cle1 :index1.keySet()) {
            result.computeIfAbsent(cle1, k -> new ArrayList<>()).addAll(index1.get(cle1));
            for(int cle2 : index2.keySet()) {
                if(cle1<=1.5*cle2) {
                    result.computeIfAbsent(cle1, k -> new ArrayList<>()).addAll(index2.get(cle2));
                }
            }
            candidats.clear();
            candidats.addAll(result.get(cle1));
            for(int i=0;i<candidats.size();i++) {
                for(int j=i+1;j<candidats.size();j++) {
                    Nom nom1=new Nom(candidats.get(i).getElement1(),candidats.get(i).getElement2(),null);
                    Nom nom2=new Nom(candidats.get(j).getElement1(),candidats.get(j).getElement2(),null);
                    couples.add(new CoupleDeNom(nom1, nom2));
                }
            }

        }




        return new ListeGeneree(couples);
    }
}

