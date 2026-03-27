import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InterfaceRecherche extends JFrame {
    private final JComboBox<String> actionCombo;
    private final JComboBox<String> dataset1Combo;
    private final JComboBox<String> dataset2Combo;
    private final JTextField queryNameField;
    private final JTextField queryIdField;
    private final JComboBox<String> comparateurCombo;
    private final JComboBox<String> generateurCombo;
    private final JCheckBox lowerCaseCheck;
    private final JCheckBox tokenSortCheck;
    private final JCheckBox cleanCharsCheck;
    private final JSpinner seuilSpinner;
    private final JSpinner maxResultsSpinner;
    private final JLabel statusLabel;
    private final JLabel statsLabel;
    private final DefaultTableModel resultModel;
    private final Map<String, String> datasetsByDisplayName;

    public InterfaceRecherche() {
        setTitle("Moteur de recherche - Interface");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        datasetsByDisplayName = chargerDatasets();

        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        actionCombo = new JComboBox<>(new String[]{
                "Recherche (nom -> dataset)",
                "Comparaison (dataset A vs dataset B)",
                "Deduplication (dataset)"
        });

        dataset1Combo = new JComboBox<>(datasetsByDisplayName.keySet().toArray(new String[0]));
        dataset2Combo = new JComboBox<>(datasetsByDisplayName.keySet().toArray(new String[0]));

        queryNameField = new JTextField("Houda Amer", 30);
        queryIdField = new JTextField("QUERY-1", 20);

        comparateurCombo = new JComboBox<>(new String[]{"Jaro-Winkler", "Levenshtein", "Egalite"});
        generateurCombo = new JComboBox<>(new String[]{"Token/prefix", "Par taille"});

        lowerCaseCheck = new JCheckBox("Lower case", true);
        tokenSortCheck = new JCheckBox("Token sort", true);
        cleanCharsCheck = new JCheckBox("Remove non letters", true);

        seuilSpinner = new JSpinner(new SpinnerNumberModel(0.80, 0.0, 1.0, 0.01));
        maxResultsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 10));

        JButton runButton = new JButton("Executer");
        runButton.addActionListener(e -> executerRecherche());

        statusLabel = new JLabel("Pret");
        statsLabel = new JLabel("Aucun resultat");

        int row = 0;
        addLabeled(configPanel, gbc, row++, "Action", actionCombo);
        addLabeled(configPanel, gbc, row++, "Dataset principal", dataset1Combo);
        addLabeled(configPanel, gbc, row++, "Dataset secondaire", dataset2Combo);
        addLabeled(configPanel, gbc, row++, "Nom a chercher", queryNameField);
        addLabeled(configPanel, gbc, row++, "ID de la requete", queryIdField);
        addLabeled(configPanel, gbc, row++, "Comparateur", comparateurCombo);
        addLabeled(configPanel, gbc, row++, "Generateur", generateurCombo);

        JPanel checksPanel = new JPanel();
        checksPanel.add(lowerCaseCheck);
        checksPanel.add(tokenSortCheck);
        checksPanel.add(cleanCharsCheck);
        addLabeled(configPanel, gbc, row++, "Pretraitements", checksPanel);

        addLabeled(configPanel, gbc, row++, "Seuil", seuilSpinner);
        addLabeled(configPanel, gbc, row++, "Max resultats", maxResultsSpinner);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        configPanel.add(runButton, gbc);

        gbc.gridy = row++;
        configPanel.add(statusLabel, gbc);
        gbc.gridy = row;
        configPanel.add(statsLabel, gbc);

        resultModel = new DefaultTableModel(new String[]{"ID1", "Nom 1", "ID2", "Nom 2", "Score"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(resultModel);
        JScrollPane scrollPane = new JScrollPane(table);

        setLayout(new BorderLayout());
        add(configPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addLabeled(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label + " :"), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void executerRecherche() {
        if (datasetsByDisplayName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun fichier CSV detecte.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setText("Execution en cours...");
        resultModel.setRowCount(0);

        SwingWorker<ResultatExecution, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultatExecution doInBackground() {
                long start = System.currentTimeMillis();

                String dataset1Path = datasetsByDisplayName.get((String) dataset1Combo.getSelectedItem());
                String dataset2Path = datasetsByDisplayName.get((String) dataset2Combo.getSelectedItem());

                ListeDesNoms liste1 = new ListeDesNoms(new Recuperateurfichecsv(dataset1Path).recuperer());
                ListeDesNoms liste2 = new ListeDesNoms(new Recuperateurfichecsv(dataset2Path).recuperer());

                Configuration configuration = construireConfiguration();
                MoteurDeRecherche moteur = new MoteurDeRecherche(configuration);

                String action = (String) actionCombo.getSelectedItem();
                List<NomAvecScore> resultats;

                if ("Recherche (nom -> dataset)".equals(action)) {
                    String nom = queryNameField.getText().trim();
                    String id = queryIdField.getText().trim().isEmpty() ? "QUERY-1" : queryIdField.getText().trim();
                    ListeDesNoms aChercher = new ListeDesNoms(List.of(new CoupleDeString(id, nom)));
                    resultats = moteur.rechercher(aChercher, liste1, null);
                } else if ("Comparaison (dataset A vs dataset B)".equals(action)) {
                    resultats = moteur.comparer(liste1, liste2, null);
                } else {
                    resultats = moteur.Dedupliquer(liste1, null);
                }

                int max = (Integer) maxResultsSpinner.getValue();
                List<NomAvecScore> limites = limiter(resultats, max);
                long elapsed = System.currentTimeMillis() - start;

                return new ResultatExecution(limites, elapsed, liste1.liste().size(), liste2.liste().size());
            }

            @Override
            protected void done() {
                try {
                    ResultatExecution execution = get();
                    afficherResultats(execution.resultats());
                    statusLabel.setText("Termine");
                    statsLabel.setText("Resultats: " + execution.resultats().size()
                            + " | Temps: " + execution.tempsMs() + " ms"
                            + " | Taille dataset1: " + execution.tailleDataset1()
                            + " | Taille dataset2: " + execution.tailleDataset2());
                } catch (Exception ex) {
                    statusLabel.setText("Echec");
                    JOptionPane.showMessageDialog(InterfaceRecherche.this,
                            "Erreur pendant l'execution: " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private List<NomAvecScore> limiter(List<NomAvecScore> source, int max) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        if (source.size() <= max) {
            return source;
        }
        return source.subList(0, max);
    }

    private void afficherResultats(List<NomAvecScore> resultats) {
        resultModel.setRowCount(0);
        for (NomAvecScore resultat : resultats) {
            resultModel.addRow(new Object[]{
                    resultat.getCouple().getNom1().getId(),
                    resultat.getCouple().getNom1().getNom(),
                    resultat.getCouple().getNom2().getId(),
                    resultat.getCouple().getNom2().getNom(),
                    String.format(Locale.US, "%.4f", resultat.getScore())
            });
        }
    }

    private Configuration construireConfiguration() {
        List<Pretraiteur> pretraiteurs = new ArrayList<>();
        if (lowerCaseCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurToLowerCase());
        }
        if (tokenSortCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurAvecTokenisation());
        }
        if (cleanCharsCheck.isSelected()) {
            pretraiteurs.add(new PretraPretraiteurSansCaracteresNiChiffres());
        }

        GenerateurDeCandidat generateur = "Par taille".equals(generateurCombo.getSelectedItem())
                ? new GenerateurParTaille()
                : new Generateur1();

        ComparateurDeChaine compChaine;
        Comparateur comparateurNom;
        String choixComparateur = (String) comparateurCombo.getSelectedItem();

        if ("Levenshtein".equals(choixComparateur)) {
            compChaine = new ComparateurAvecLevenshtein();
            comparateurNom = new ComparateurDeNomUtilisantLevenshtein(compChaine);
        } else if ("Egalite".equals(choixComparateur)) {
            compChaine = new ComparateurAvecJaroWinkler();
            comparateurNom = new ComparateurSimple();
        } else {
            compChaine = new ComparateurAvecJaroWinkler();
            comparateurNom = new ComparateurJaro(compChaine);
        }

        double seuil = ((Number) seuilSpinner.getValue()).doubleValue();
        Selectionneur selectionneur = new SelectionneurParSeuil(seuil);

        return new Configuration(pretraiteurs, generateur, compChaine, comparateurNom, selectionneur, seuil);
    }

    private Map<String, String> chargerDatasets() {
        Map<String, String> pathsByName = new LinkedHashMap<>();
        List<File> folders = List.of(
                new File("."),
                new File("src"),
                new File("bin"),
                new File("Lib"),
                new File("src/Lib"),
                new File("bin/Lib")
        );

        Map<String, Integer> duplicates = new HashMap<>();

        for (File folder : folders) {
            if (!folder.exists() || !folder.isDirectory()) {
                continue;
            }
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".csv"));
            if (files == null) {
                continue;
            }
            for (File file : files) {
                String baseName = file.getName();
                String key = baseName;
                if (pathsByName.containsKey(key)) {
                    int index = duplicates.getOrDefault(baseName, 1) + 1;
                    duplicates.put(baseName, index);
                    key = baseName + " [" + file.getParentFile().getPath() + "]";
                } else {
                    duplicates.put(baseName, 1);
                }
                pathsByName.put(key, file.getPath());
            }
        }

        return pathsByName;
    }

    private record ResultatExecution(List<NomAvecScore> resultats, long tempsMs, int tailleDataset1, int tailleDataset2) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfaceRecherche app = new InterfaceRecherche();
            app.setVisible(true);
        });
    }
}
