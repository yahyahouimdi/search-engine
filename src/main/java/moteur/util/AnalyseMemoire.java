package moteur.util;

public final class AnalyseMemoire {

    private AnalyseMemoire() {
    }

    public static long memoireUtiliseeMo() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    public static void afficherMemoire(String etape) {
        Runtime.getRuntime().gc(); // suggère au garbage collector de nettoyer
        System.out.println("Mémoire utilisée " + etape + " : " + memoireUtiliseeMo() + " Mo");
    }
}
