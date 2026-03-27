public class AnalyseMemoire {

    public static void afficherMemoire(String etape) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // suggère au garbage collector de nettoyer
        long memoireUtilisee = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Mémoire utilisée " + etape + " : " + (memoireUtilisee / (1024 * 1024)) + " Mo");
    }
}
