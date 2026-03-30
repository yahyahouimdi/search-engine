package moteur.pretraitement;

/** Transforme un nom avant comparaison. Les prétraiteurs s'enchaînent dans l'ordre de la configuration. */
public interface Pretraiteur {
    String pretraiter(String nom);
}
