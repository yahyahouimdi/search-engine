import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Recuperateurfichecsv implements Recuperateur {

    private String filePath;

    // Constructeur qui initialise le chemin du fichier CSV
    public Recuperateurfichecsv(String filePath) {
        this.filePath = filePath;
    }

    // Méthode qui lit le fichier CSV et retourne une liste d’objets ListeDesNoms
    @Override
    public List<CoupleDeString> recuperer() {
        Path resolvedPath = resoudreChemin(filePath);

        try (Stream<String> lines = Files.lines(resolvedPath)) {
            return lines
                    .skip(1) // Ignore la première ligne (en-tête)
                    .map(line -> line.split(",")) // Sépare chaque ligne CSV en tableau
                    .filter(values -> values.length >= 2) // Ignore les lignes incomplètes
                    .map(values -> new CoupleDeString(values[0].trim(), values[1].trim())) // Crée les objets ListeDesNoms
                    .collect(Collectors.toList()); // Convertit le Stream en List (compatible Java 11)
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(); // Retourne une liste vide en cas d’erreur
        }
    }

    private Path resoudreChemin(String chemin) {
        Path direct = Paths.get(chemin);
        if (Files.exists(direct)) {
            return direct;
        }

        Path cwd = Paths.get("").toAbsolutePath();
        Path[] candidats = new Path[] {
                cwd.resolve(chemin),
                cwd.resolve("src").resolve(chemin),
                cwd.resolve("bin").resolve(chemin),
                cwd.resolve("Lib").resolve(chemin),
                cwd.resolve("src").resolve("Lib").resolve(chemin),
                cwd.resolve("bin").resolve("Lib").resolve(chemin)
        };

        for (Path candidat : candidats) {
            if (Files.exists(candidat)) {
                return candidat;
            }
        }

        return direct;
    }
}
