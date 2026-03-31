package moteur.ui;

import moteur.Configuration;
import moteur.MoteurDeRecherche;
import moteur.comparaison.Comparateur;
import moteur.comparaison.ComparateurAvecJaroWinkler;
import moteur.comparaison.ComparateurAvecLevenshtein;
import moteur.comparaison.ComparateurDeNomParChaine;
import moteur.comparaison.ComparateurSimple;
import moteur.generation.GenerateurDeCandidat;
import moteur.generation.GenerateurParTaille;
import moteur.generation.GenerateurParToken;
import moteur.modele.CoupleDeString;
import moteur.modele.ListeDesNoms;
import moteur.modele.Nom;
import moteur.modele.NomAvecScore;
import moteur.pretraitement.Pretraiteur;
import moteur.pretraitement.PretraiteurAvecTokenisation;
import moteur.pretraitement.PretraiteurSansAccents;
import moteur.pretraitement.PretraiteurSansCaracteresNiChiffres;
import moteur.pretraitement.PretraiteurToLowerCase;
import moteur.recuperation.Datasets;
import moteur.recuperation.RecuperateurFichierCsv;
import moteur.selection.SelectionneurParSeuil;
import moteur.util.AnalyseMemoire;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/** Interface graphique (Swing) du moteur de recherche. */
public class InterfaceRecherche extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final Color ACCENT = new Color(0x2F, 0x5D, 0xA8);
    private static final Color ACCENT_FONCE = new Color(0x1F, 0x3F, 0x75);
    private static final Color FOND_ENTETE = new Color(0x24, 0x2B, 0x38);
    private static final Color SCORE_FORT = new Color(0x3C, 0xA5, 0x5C);
    private static final Color SCORE_MOYEN = new Color(0xE0, 0xA1, 0x2E);
    private static final Color SCORE_FAIBLE = new Color(0x9A, 0xA5, 0xB1);

    private enum Action {
        RECHERCHE("Rechercher un nom", "Cherche un nom saisi dans le fichier principal"),
        COMPARAISON("Comparer deux fichiers", "Rapproche les noms du fichier principal et du fichier secondaire"),
        DEDUPLICATION("Dédupliquer un fichier", "Trouve les doublons à l'intérieur du fichier principal");

        final String libelle;
        final String description;

        Action(String libelle, String description) {
            this.libelle = libelle;
            this.description = description;
        }
    }

    private final Map<Action, JRadioButton> boutonsAction = new java.util.EnumMap<>(Action.class);
    private final JComboBox<Path> fichier1Combo = new JComboBox<>();
    private final JComboBox<Path> fichier2Combo = new JComboBox<>();
    private final JButton parcourir1Button = new JButton("…");
    private final JButton parcourir2Button = new JButton("…");
    private final JTextField nomField = new JTextField("Mustafa Abubakr");
    private final JComboBox<String> comparateurCombo = new JComboBox<>(new String[]{"Jaro-Winkler", "Levenshtein", "Égalité stricte"});
    private final JComboBox<String> generateurCombo = new JComboBox<>(new String[]{"Par mot commun", "Par taille"});
    private final JCheckBox minusculesCheck = new JCheckBox("Minuscules", true);
    private final JCheckBox accentsCheck = new JCheckBox("Supprimer les accents", true);
    private final JCheckBox ponctuationCheck = new JCheckBox("Supprimer ponctuation et chiffres", true);
    private final JCheckBox triMotsCheck = new JCheckBox("Trier les mots (token sort)", true);
    private final JSlider seuilSlider = new JSlider(50, 100, 80);
    private final JLabel seuilLabel = new JLabel("0.80");
    private final JSpinner maxResultatsSpinner = new JSpinner(new SpinnerNumberModel(500, 1, 1_000_000, 100));
    private final JButton lancerButton = new JButton("Lancer");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextField filtreField = new JTextField(18);
    private final JButton exporterButton = new JButton("Exporter CSV");
    private final JLabel compteurLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel("Prêt");
    private final JLabel statsLabel = new JLabel(" ");
    private final DefaultTableModel resultModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private javax.swing.table.TableColumn colonneId1;

    private final Map<Path, ListeDesNoms> cacheFichiers = new ConcurrentHashMap<>();

    public InterfaceRecherche() {
        super("Moteur de recherche de noms");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

        resultModel = new DefaultTableModel(new String[]{"#", "Score", "Nom 1", "ID 1", "Nom 2", "ID 2"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Integer.class;
                    case 1 -> Double.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(resultModel);
        sorter = new TableRowSorter<>(resultModel);

        setLayout(new BorderLayout());
        add(creerEntete(), BorderLayout.NORTH);
        add(creerPanneauConfiguration(), BorderLayout.WEST);
        add(creerPanneauResultats(), BorderLayout.CENTER);
        add(creerBarreDeStatut(), BorderLayout.SOUTH);

        chargerListeFichiers();
        mettreAJourChamps();
    }

    // ---------------------------------------------------------------- construction de l'interface

    private JComponent creerEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(FOND_ENTETE);
        entete.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titre = new JLabel("Moteur de recherche de noms");
        titre.setForeground(Color.WHITE);
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 20f));

        JLabel sousTitre = new JLabel("Rapprochement flou de noms de personnes : recherche, comparaison et déduplication de fichiers CSV");
        sousTitre.setForeground(new Color(0xB8, 0xC2, 0xD0));

        JPanel textes = new JPanel();
        textes.setOpaque(false);
        textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
        textes.add(titre);
        textes.add(Box.createVerticalStrut(2));
        textes.add(sousTitre);
        entete.add(textes, BorderLayout.CENTER);
        return entete;
    }

    private JComponent creerPanneauConfiguration() {
        JPanel panneau = new JPanel();
        panneau.setLayout(new BoxLayout(panneau, BoxLayout.Y_AXIS));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Action
        JPanel actions = section("1. Action");
        ButtonGroup groupe = new ButtonGroup();
        for (Action action : Action.values()) {
            JRadioButton bouton = new JRadioButton(action.libelle, action == Action.RECHERCHE);
            bouton.setToolTipText(action.description);
            bouton.addActionListener(e -> mettreAJourChamps());
            groupe.add(bouton);
            boutonsAction.put(action, bouton);
            actions.add(bouton);
        }
        panneau.add(actions);

        // Données
        JPanel donnees = section("2. Données");
        donnees.setLayout(new GridBagLayout());
        configurerComboFichier(fichier1Combo);
        configurerComboFichier(fichier2Combo);
        parcourir1Button.setToolTipText("Choisir un autre fichier CSV");
        parcourir2Button.setToolTipText("Choisir un autre fichier CSV");
        parcourir1Button.addActionListener(e -> parcourir(fichier1Combo));
        parcourir2Button.addActionListener(e -> parcourir(fichier2Combo));
        ligneFichier(donnees, 0, "Fichier principal", fichier1Combo, parcourir1Button);
        ligneFichier(donnees, 1, "Fichier secondaire", fichier2Combo, parcourir2Button);
        panneau.add(donnees);

        // Requête
        JPanel requete = section("3. Nom à rechercher");
        requete.setLayout(new BorderLayout());
        nomField.addActionListener(e -> lancer());
        nomField.setToolTipText("Appuyez sur Entrée pour lancer la recherche");
        requete.add(nomField, BorderLayout.CENTER);
        panneau.add(requete);

        // Algorithmes
        JPanel algorithmes = section("4. Algorithmes");
        algorithmes.setLayout(new GridBagLayout());
        comparateurCombo.setToolTipText("Mesure de similarité entre deux noms");
        generateurCombo.setToolTipText("Stratégie de sélection des couples à comparer");
        ligne(algorithmes, 0, "Comparateur", comparateurCombo);
        ligne(algorithmes, 1, "Générateur", generateurCombo);
        panneau.add(algorithmes);

        // Prétraitements
        JPanel pretraitements = section("5. Prétraitements");
        pretraitements.add(minusculesCheck);
        pretraitements.add(accentsCheck);
        pretraitements.add(ponctuationCheck);
        pretraitements.add(triMotsCheck);
        panneau.add(pretraitements);

        // Sélection
        JPanel selection = section("6. Sélection");
        selection.setLayout(new GridBagLayout());
        seuilSlider.setOpaque(false);
        seuilSlider.addChangeListener(e -> seuilLabel.setText(String.format(Locale.ROOT, "%.2f", seuil())));
        seuilLabel.setFont(seuilLabel.getFont().deriveFont(Font.BOLD));
        JPanel seuilPanel = new JPanel(new BorderLayout(6, 0));
        seuilPanel.setOpaque(false);
        seuilPanel.add(seuilSlider, BorderLayout.CENTER);
        seuilPanel.add(seuilLabel, BorderLayout.EAST);
        ligne(selection, 0, "Seuil", seuilPanel);
        ligne(selection, 1, "Max résultats", maxResultatsSpinner);
        panneau.add(selection);

        // Lancer
        // BasicButtonUI : le look and feel Windows ignore la couleur de fond des boutons
        lancerButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        lancerButton.setFont(lancerButton.getFont().deriveFont(Font.BOLD, 14f));
        lancerButton.setBackground(ACCENT);
        lancerButton.setForeground(Color.WHITE);
        lancerButton.setOpaque(true);
        lancerButton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        lancerButton.setFocusPainted(false);
        lancerButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        lancerButton.getModel().addChangeListener(e -> lancerButton.setBackground(
                !lancerButton.isEnabled() ? SCORE_FAIBLE
                        : lancerButton.getModel().isRollover() ? ACCENT_FONCE : ACCENT));
        lancerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lancerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        lancerButton.addActionListener(e -> lancer());
        getRootPane().setDefaultButton(lancerButton);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setVisible(false);

        panneau.add(Box.createVerticalStrut(8));
        panneau.add(lancerButton);
        panneau.add(Box.createVerticalStrut(6));
        panneau.add(progressBar);
        panneau.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panneau,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xD5, 0xDA, 0xE1)));
        scroll.setPreferredSize(new Dimension(380, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JComponent creerPanneauResultats() {
        table.setRowSorter(sorter);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xE6, 0xE9, 0xEE));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getColumnModel().getColumn(1).setCellRenderer(new RenduScore());

        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        centre.setForeground(Color.GRAY);
        table.getColumnModel().getColumn(0).setCellRenderer(centre);

        int[] largeurs = {50, 130, 260, 200, 260, 200};
        for (int i = 0; i < largeurs.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(largeurs[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        colonneId1 = table.getColumnModel().getColumn(3);

        filtreField.setToolTipText("Filtrer les résultats affichés (nom ou identifiant)");
        filtreField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { appliquerFiltre(); }
            public void removeUpdate(DocumentEvent e) { appliquerFiltre(); }
            public void changedUpdate(DocumentEvent e) { appliquerFiltre(); }
        });
        exporterButton.setEnabled(false);
        exporterButton.addActionListener(e -> exporter());

        JPanel barreOutils = new JPanel(new BorderLayout());
        barreOutils.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        JLabel titre = new JLabel("Résultats");
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 15f));
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        gauche.add(titre);
        gauche.add(compteurLabel);
        compteurLabel.setForeground(Color.GRAY);
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        droite.add(new JLabel("Filtrer :"));
        droite.add(filtreField);
        droite.add(exporterButton);
        barreOutils.add(gauche, BorderLayout.WEST);
        barreOutils.add(droite, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD5, 0xDA, 0xE1)));

        JPanel panneau = new JPanel(new BorderLayout());
        panneau.add(barreOutils, BorderLayout.NORTH);
        panneau.add(scroll, BorderLayout.CENTER);
        return panneau;
    }

    private JComponent creerBarreDeStatut() {
        JPanel barre = new JPanel(new BorderLayout());
        barre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD5, 0xDA, 0xE1)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        statsLabel.setForeground(Color.DARK_GRAY);
        barre.add(statusLabel, BorderLayout.WEST);
        barre.add(statsLabel, BorderLayout.EAST);
        return barre;
    }

    private static JPanel section(String titre) {
        JPanel panneau = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                // occupe toute la largeur, mais jamais plus que sa hauteur naturelle
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panneau.setLayout(new BoxLayout(panneau, BoxLayout.Y_AXIS));
        panneau.setAlignmentX(Component.LEFT_ALIGNMENT);
        panneau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC9, 0xD0, 0xDA)),
                        titre, 0, 0, null, ACCENT_FONCE),
                BorderFactory.createEmptyBorder(4, 4, 8, 4)));
        return panneau;
    }

    private static void ligne(JPanel panneau, int ligne, String libelle, JComponent composant) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = ligne;
        gbc.insets = new Insets(3, 0, 3, 8);
        gbc.anchor = GridBagConstraints.WEST;
        panneau.add(new JLabel(libelle), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 0, 3, 0);
        panneau.add(composant, gbc);
    }

    private static void ligneFichier(JPanel panneau, int ligne, String libelle, JComboBox<Path> combo, JButton parcourir) {
        JPanel champ = new JPanel(new BorderLayout(4, 0));
        champ.setOpaque(false);
        champ.add(combo, BorderLayout.CENTER);
        parcourir.setMargin(new Insets(2, 8, 2, 8));
        champ.add(parcourir, BorderLayout.EAST);
        ligne(panneau, ligne, libelle, champ);
    }

    private static void configurerComboFichier(JComboBox<Path> combo) {
        combo.setPrototypeDisplayValue(Path.of("peps_names_658k.csv"));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Path chemin) {
                    setText(chemin.getFileName() + "  (" + tailleLisible(chemin) + ")");
                    setToolTipText(chemin.toString());
                }
                return this;
            }
        });
    }

    // ---------------------------------------------------------------- comportement

    private Action actionChoisie() {
        return boutonsAction.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(Action.RECHERCHE);
    }

    private double seuil() {
        return seuilSlider.getValue() / 100.0;
    }

    /** Active uniquement les champs utiles à l'action choisie. */
    private void mettreAJourChamps() {
        Action action = actionChoisie();
        nomField.setEnabled(action == Action.RECHERCHE);
        fichier2Combo.setEnabled(action == Action.COMPARAISON);
        parcourir2Button.setEnabled(action == Action.COMPARAISON);
        lancerButton.setText(switch (action) {
            case RECHERCHE -> "Rechercher";
            case COMPARAISON -> "Comparer";
            case DEDUPLICATION -> "Dédupliquer";
        });
    }

    private void chargerListeFichiers() {
        List<Path> fichiers = Datasets.lister();
        for (Path fichier : fichiers) {
            fichier1Combo.addItem(fichier);
            fichier2Combo.addItem(fichier);
        }
        // Par défaut : un fichier moyen en principal, un petit en secondaire
        selectionnerParNom(fichier1Combo, "peps_names_8k.csv");
        selectionnerParNom(fichier2Combo, "peps_names_2k.csv");
        if (fichiers.isEmpty()) {
            statusLabel.setText("Aucun fichier CSV trouvé dans data/ : utilisez le bouton « … » pour en choisir un.");
        }
    }

    private static void selectionnerParNom(JComboBox<Path> combo, String nom) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getFileName().toString().equals(nom)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void parcourir(JComboBox<Path> combo) {
        JFileChooser chooser = new JFileChooser(Path.of("data").toAbsolutePath().toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (id,nom)", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path chemin = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            if (((javax.swing.DefaultComboBoxModel<Path>) combo.getModel()).getIndexOf(chemin) < 0) {
                combo.addItem(chemin);
            }
            combo.setSelectedItem(chemin);
        }
    }

    private void lancer() {
        Action action = actionChoisie();
        Path fichier1 = (Path) fichier1Combo.getSelectedItem();
        Path fichier2 = (Path) fichier2Combo.getSelectedItem();
        String nom = nomField.getText().trim();

        if (fichier1 == null || (action == Action.COMPARAISON && fichier2 == null)) {
            erreur("Choisissez d'abord un fichier CSV.");
            return;
        }
        if (action == Action.RECHERCHE && nom.isEmpty()) {
            erreur("Saisissez un nom à rechercher.");
            nomField.requestFocusInWindow();
            return;
        }

        Configuration configuration = construireConfiguration();
        int max = (Integer) maxResultatsSpinner.getValue();

        enCours(true);
        statusLabel.setText("Chargement de " + fichier1.getFileName() + "…");
        resultModel.setRowCount(0);

        new SwingWorker<Execution, String>() {
            @Override
            protected Execution doInBackground() {
                long debut = System.currentTimeMillis();
                ListeDesNoms liste1 = charger(fichier1);
                ListeDesNoms liste2 = action == Action.COMPARAISON ? charger(fichier2) : null;
                publish(action.libelle + " en cours…");

                MoteurDeRecherche moteur = new MoteurDeRecherche(configuration);
                List<NomAvecScore> resultats = switch (action) {
                    case RECHERCHE -> moteur.rechercher(
                            new ListeDesNoms(List.of(new CoupleDeString("REQUETE", nom))), liste1);
                    case COMPARAISON -> moteur.comparer(liste1, liste2);
                    case DEDUPLICATION -> moteur.dedupliquer(liste1);
                };

                long duree = System.currentTimeMillis() - debut;
                List<NomAvecScore> affiches = resultats.size() > max ? resultats.subList(0, max) : resultats;
                return new Execution(new ArrayList<>(affiches), resultats.size(), duree,
                        liste1.liste().size(), liste2 == null ? 0 : liste2.liste().size());
            }

            @Override
            protected void process(List<String> messages) {
                statusLabel.setText(messages.get(messages.size() - 1));
            }

            @Override
            protected void done() {
                enCours(false);
                try {
                    afficher(get(), action);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    statusLabel.setText("Échec");
                    erreur(cause instanceof OutOfMemoryError
                            ? "Mémoire insuffisante pour ce traitement.\nEssayez un fichier plus petit, le générateur « Par mot commun », "
                              + "ou relancez Java avec plus de mémoire (ex : -Xmx4g)."
                            : "Erreur pendant l'exécution : " + cause.getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private ListeDesNoms charger(Path fichier) {
        return cacheFichiers.computeIfAbsent(fichier,
                f -> new ListeDesNoms(new RecuperateurFichierCsv(f.toString()).recuperer()));
    }

    private Configuration construireConfiguration() {
        List<Pretraiteur> pretraiteurs = new ArrayList<>();
        if (minusculesCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurToLowerCase());
        }
        if (accentsCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurSansAccents());
        }
        if (ponctuationCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurSansCaracteresNiChiffres());
        }
        if (triMotsCheck.isSelected()) {
            pretraiteurs.add(new PretraiteurAvecTokenisation());
        }

        GenerateurDeCandidat generateur = generateurCombo.getSelectedIndex() == 1
                ? new GenerateurParTaille()
                : new GenerateurParToken();

        Comparateur comparateur = switch (comparateurCombo.getSelectedIndex()) {
            case 1 -> new ComparateurDeNomParChaine(new ComparateurAvecLevenshtein());
            case 2 -> new ComparateurSimple();
            default -> new ComparateurDeNomParChaine(new ComparateurAvecJaroWinkler());
        };

        return new Configuration(pretraiteurs, generateur, comparateur, new SelectionneurParSeuil(seuil()));
    }

    private void afficher(Execution execution, Action action) {
        resultModel.setRowCount(0);
        afficherColonneId1(action != Action.RECHERCHE); // en recherche, l'ID 1 est toujours la requête
        int rang = 1;
        for (NomAvecScore resultat : execution.resultats()) {
            Nom nom1 = resultat.getCouple().getNom1();
            Nom nom2 = resultat.getCouple().getNom2();
            resultModel.addRow(new Object[]{
                    rang++, resultat.getScore(),
                    nom1.getNomOriginal(), nom1.getId(),
                    nom2.getNomOriginal(), nom2.getId()
            });
        }

        int total = execution.total();
        compteurLabel.setText(total == execution.resultats().size()
                ? total + " correspondance(s)"
                : execution.resultats().size() + " affichées sur " + total + " correspondances");
        exporterButton.setEnabled(total > 0);
        statusLabel.setText(total == 0 ? "Terminé : aucune correspondance au-dessus du seuil" : "Terminé");

        String tailles = action == Action.COMPARAISON
                ? String.format(Locale.FRANCE, "%,d × %,d noms", execution.taille1(), execution.taille2())
                : String.format(Locale.FRANCE, "%,d noms", execution.taille1());
        statsLabel.setText(String.format(Locale.FRANCE, "%s  ·  %,d ms  ·  %,d Mo utilisés",
                tailles, execution.dureeMs(), AnalyseMemoire.memoireUtiliseeMo()));
    }

    private void afficherColonneId1(boolean visible) {
        boolean presente = false;
        for (int i = 0; i < table.getColumnCount(); i++) {
            presente |= table.getColumnModel().getColumn(i) == colonneId1;
        }
        if (visible && !presente) {
            table.addColumn(colonneId1);
            table.moveColumn(table.getColumnCount() - 1, 3);
        } else if (!visible && presente) {
            table.removeColumn(colonneId1);
        }
    }

    private void enCours(boolean actif) {
        lancerButton.setEnabled(!actif);
        progressBar.setIndeterminate(actif);
        progressBar.setVisible(actif);
        setCursor(actif ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : null);
    }

    private void appliquerFiltre() {
        String texte = filtreField.getText().trim();
        sorter.setRowFilter(texte.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texte), 2, 3, 4, 5));
    }

    private void exporter() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("resultats.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        try (Writer writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("score,id1,nom1,id2,nom2\n");
            for (int ligne = 0; ligne < table.getRowCount(); ligne++) {
                int modele = table.convertRowIndexToModel(ligne);
                writer.write(String.format(Locale.ROOT, "%.4f,%s,%s,%s,%s%n",
                        (Double) resultModel.getValueAt(modele, 1),
                        csv(resultModel.getValueAt(modele, 3)), csv(resultModel.getValueAt(modele, 2)),
                        csv(resultModel.getValueAt(modele, 5)), csv(resultModel.getValueAt(modele, 4))));
            }
            statusLabel.setText(table.getRowCount() + " lignes exportées vers " + destination.getFileName());
        } catch (IOException ex) {
            erreur("Export impossible : " + ex.getMessage());
        }
    }

    private static String csv(Object valeur) {
        String texte = String.valueOf(valeur);
        return texte.contains(",") || texte.contains("\"")
                ? "\"" + texte.replace("\"", "\"\"") + "\""
                : texte;
    }

    private static String tailleLisible(Path fichier) {
        try {
            long octets = Files.size(fichier);
            return octets >= 1024 * 1024
                    ? String.format(Locale.ROOT, "%.1f Mo", octets / (1024.0 * 1024))
                    : (octets / 1024) + " Ko";
        } catch (IOException e) {
            return "?";
        }
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Moteur de recherche", JOptionPane.WARNING_MESSAGE);
    }

    private record Execution(List<NomAvecScore> resultats, int total, long dureeMs, int taille1, int taille2) {
    }

    /** Affiche le score avec une barre colorée proportionnelle. */
    private static class RenduScore extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private double score;

        RenduScore() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            score = value instanceof Double d ? d : 0;
            setText(String.format(Locale.ROOT, "%.4f  ", score));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color couleur = score >= 0.95 ? SCORE_FORT : score >= 0.85 ? SCORE_MOYEN : SCORE_FAIBLE;
            int largeur = (int) Math.round((getWidth() - 8) * Math.max(0, Math.min(1, score)));
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 70));
            g.fillRoundRect(4, 4, largeur, getHeight() - 8, 6, 6);
            g.setColor(couleur);
            g.fillRoundRect(4, getHeight() - 7, largeur, 3, 3, 3);
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // garde le look and feel par défaut
        }
        SwingUtilities.invokeLater(() -> new InterfaceRecherche().setVisible(true));
    }
}
