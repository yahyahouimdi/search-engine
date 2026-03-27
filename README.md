# Moteur de Recherche (Name Matching Engine)

A Java project for fuzzy name matching across large CSV datasets.

The application supports:
- Searching one name against a dataset
- Comparing two datasets
- Deduplicating a dataset
- Configuring preprocessing and similarity strategies
- Running in both CLI mode and Desktop GUI mode

## Project Concept

This project is a configurable **record linkage / fuzzy matching engine** for person names.

The pipeline is:
1. Load CSV files (`id`, `name`)
2. Apply preprocessing chain (lowercase, token sort, remove non-letters)
3. Generate candidate pairs using indexing strategies
4. Compute similarity score (Jaro-Winkler, Levenshtein, or exact equality)
5. Select final matches using a threshold selector

This architecture is strategy-based, so components can be swapped without rewriting the full engine.

## Features

- Multiple actions:
  - Search: one query name against dataset
  - Compare: dataset A vs dataset B
  - Deduplicate: dataset vs itself
- Multiple matching strategies:
  - Jaro-Winkler
  - Levenshtein
  - Exact equality
- Multiple candidate generators:
  - Token/prefix-based (`Generateur1`)
  - Length-based (`GenerateurParTaille`)
- Configurable preprocessing pipeline
- Similarity threshold filtering
- GUI result table with execution stats

## Tech Stack

- Java (standard library + Swing)
- CSV input files bundled in `src/`, `bin/`, and `Lib/`

## Repository Structure

- `src/`: Java source files and sample CSV datasets
- `bin/`: compiled artifacts and additional datasets
- `Lib/`: extra sample dataset files

Main entry points:
- `Main` (CLI menu)
- `InterfaceRecherche` (desktop GUI)

## Requirements

- JDK 17+ recommended (JDK 11+ should also work)
- Windows, macOS, or Linux

## Build

From project root:

```bash
javac src/*.java
```

On Windows PowerShell:

```powershell
javac src\*.java
```

## Run

### 1) Desktop GUI (recommended)

From project root:

```bash
java -cp src InterfaceRecherche
```

The GUI lets you:
- choose action (search / compare / deduplicate)
- choose dataset(s)
- enter query name
- choose comparator and generator
- set threshold and max results
- view results in a table

### Interface Demonstration

![Interface demonstration](src/interface%20demonstration.png)

### 2) CLI menu

From project root:

```bash
java -cp src Main
```

## CSV Format

Expected columns:
1. `id`
2. `name`

The loader skips the first line as header.

Example:

```csv
id,name
1234,Houda Amer
9876,Mustapha Abubakar
```

## Configuration Notes

- Threshold is used by `SelectionneurParSeuil`.
- Preprocessing order can affect scores and result ranking.
- Large datasets can increase memory usage depending on generation strategy.

## Troubleshooting

### Class not found / `Main` not found
Use classpath with `src` from project root:

```bash
java -cp src Main
```

### CSV file not found
`Recuperateurfichecsv` resolves files from common project paths (`.`, `src`, `bin`, `Lib`, `src/Lib`, `bin/Lib`).

### Garbled French characters in Windows terminal
Use UTF-8 in PowerShell:

```powershell
chcp 65001
$OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
java -Dfile.encoding=UTF-8 -cp src Main
```

## Future Improvements

- Export results to CSV/JSON
- Better ranking and blocking strategies
- Unit and integration test suite
- Maven/Gradle build and dependency management
- Logging and profiling dashboards

## License

Add your preferred license (for example, MIT) before publishing.

## Author

Project by the repository owner.
