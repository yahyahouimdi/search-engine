package moteur.recuperation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/** Localise les fichiers CSV fournis avec le projet (dossier {@code data/}). */
public final class Datasets {

    private static final List<Path> DOSSIERS = List.of(
            Path.of("data"),
            Path.of("..", "data"),
            Path.of(".")
    );

    private Datasets() {
    }

    /** Retourne le chemin tel quel s'il existe, sinon le cherche dans les dossiers de données. */
    public static Path resoudre(String chemin) {
        Path direct = Path.of(chemin);
        if (Files.exists(direct)) {
            return direct;
        }
        for (Path dossier : DOSSIERS) {
            Path candidat = dossier.resolve(chemin);
            if (Files.exists(candidat)) {
                return candidat;
            }
        }
        return direct;
    }

    /** Liste les CSV disponibles, du plus petit au plus gros. */
    public static List<Path> lister() {
        Set<Path> trouves = new LinkedHashSet<>();
        for (Path dossier : DOSSIERS) {
            if (!Files.isDirectory(dossier)) {
                continue;
            }
            try (Stream<Path> fichiers = Files.list(dossier)) {
                fichiers.filter(f -> f.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                        .map(f -> f.toAbsolutePath().normalize())
                        .forEach(trouves::add);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        List<Path> resultat = new ArrayList<>(trouves);
        resultat.sort(Comparator.comparingLong(Datasets::taille));
        return resultat;
    }

    private static long taille(Path fichier) {
        try {
            return Files.size(fichier);
        } catch (IOException e) {
            return Long.MAX_VALUE;
        }
    }
}
