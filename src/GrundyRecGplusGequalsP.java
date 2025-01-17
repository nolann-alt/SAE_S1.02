import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Grundy game with AI for the machine
 * This is the 4th version and brings together the optimisations of the previous versions (1.1, 1.2, 1.3)
 * and the simplification of winning piles of the same type.
 * @version 1.4
 * @author N.LESCOP - M.GOUELO
 */
class GrundyRecGplusGequalsP {
    /** Counter to measure the effectiveness of the method estGagnante() */
    long cpt;

    /** Collection of losing positions */
    ArrayList<ArrayList<Integer>> posPerdantes = new ArrayList<ArrayList<Integer>>();

    /** Collection of winning positions */
    ArrayList<ArrayList<Integer>> posGagnantes = new ArrayList<ArrayList<Integer>>();

    /** Table of types  */
    int[] type = new int[] {0, 0, 0, 1, 0, 2, 1, 0, 2, 1, 0,
                        2, 1, 3, 2, 1, 3, 2, 4, 3, 0,
                        4, 3, 0, 4, 3, 0, 4, 1, 2, 3,
                        1, 2, 4, 1, 2, 6, 1, 2, 4, 1,
                        5, 4, 1, 5, 4, 1, 5, 4, 1, 0};

    /**
     * principal() method of the program (calls the test methods and the method for playing against the AI)
     */
    void principal() {
        char test;
        do {
            test = SimpleInput.getChar("Souhaitez-vous voir les méthodes de test ? [Y/N] : ");
        } while(test != 'y' && test != 'Y' && test != 'n' && test != 'N');

        if (test == 'y' || test == 'Y') {
            testJouerGagnant();
            testPremier();
            testSuivant();
            testEstConnuePerdante();
            testEstConnueGagnante();
            testNormaliser();
            testSimplifier();
            testEstGagnanteEfficacite();


        } else if (test == 'n' || test == 'N') {
            JoueurVsMachine();
        }
    }

    /**
     * Main loop of the Grundy game
     */
    void JoueurVsMachine() {
        System.out.println(" ---------- JEU GRUNDY ----------");

        // Création joueur
        String joueur;
        String machine = "Grundy";
        String joueurActuel; // Qui enregistre qui doit jouer pendant le tour actuel

        do {
            joueur = SimpleInput.getString("Quel est votre pseudo (min 3 caractères) : ");
        } while(joueur.length() < 3);

        System.out.println("On détermine aléatoirement qui est ce qui commence...");
        int PileFace = (int) Math.random() + 1;
        if (PileFace == 0) {
            System.out.println(joueur + " joue en premier !");
            joueurActuel = joueur;
        } else {
            System.out.println(machine + " joue en première !");
            joueurActuel = machine;
        }

        // Initialisation de la liste de tas
        ArrayList<Integer> jeu = new ArrayList<Integer>();
        jeu.add(7);

        int ligne;
        int nb;
        boolean running = true;
        boolean gagner;
        while (running) {
            System.out.println("Au tour de " + joueurActuel + " de jouer !");
            afficher(jeu);

            // Si c'est au joueur de jouer...
            if (joueurActuel == joueur) {
                do {
                    ligne = SimpleInput.getInt("Sur quel tas voulez-vous agir (0 à 6) : ");
                } while(ligne < 0 || ligne >= jeu.size() || jeu.get(ligne) <= 2);

                do {
                    nb = SimpleInput.getInt("Combien d'allumettes voulez-vous retirer du tas : ");
                } while(2 * nb == jeu.get(ligne) || nb <= 0 || nb >= jeu.get(ligne));

                enlever(jeu, ligne, nb);

                // Sinon c'est à la machine de jouer
            } else {

                // On test si la position est gagnante ; Si c'est gagnant la machine joue automatiquement
                gagner = jouerGagnant(jeu);

                if (!gagner) { // La position n'est pas gagnante pour la machine ; Elle joue aléatoirement

                    do {
                        ligne = (int) (Math.random() * jeu.size()); // ligne aléatoire
                    } while(jeu.get(ligne) <= 2);

                    do {
                        nb = (int) (Math.random() * jeu.get(ligne)); // nombre d'allumette aléatoire
                    } while(nb == 0 || 2 * nb == jeu.get(ligne));

                    enlever(jeu, ligne, nb);

                }
            }

            // On test si une décomposition est possible pour le prochain tour
            // Si non, le jeu est terminé et le joueur actuel gagne
            if (estPossible(jeu) == false) {
                System.out.println("Bravo " + joueurActuel + " tu as gagné la partie !");
                afficher(jeu);
                running = false;

                // Sinon on change de joueur pour le tour suivant
            } else {
                // Changement de joueur
                if (joueurActuel == joueur) {
                    joueurActuel = machine;
                } else {
                    joueurActuel = joueur;
                }
            }
        }
    }

    /**
     * This method returns true if and only if AFTER normalization of the game, there is a tie between the game and a situation known as
     * in the table of losing situations.
     *
     * @param jeu game board
     * @return true if the situation is known to be a loser, false otherwise.
     */
    boolean estConnuePerdante ( ArrayList<Integer> jeu ) {
        boolean connue = false;
        if (jeu == null) {
            System.err.println("jouerGagnant(): le paramètre jeu est null");

        } else {
            if (posPerdantes.contains(jeu)){
                connue = true;
            }
        }
        return connue;
    }

    /**
     * Brief tests of the estConnuePerdante() method
     */
    void testEstConnuePerdante() {
        System.out.println();
        System.out.println("*** testEstConnuePerdante() ***");

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        jeu1.add(11);
        ArrayList<Integer> jeu2 = new ArrayList<Integer>();
        jeu2.add(11);
        jeu2.add(10);

        System.out.println("Test des cas normaux");
        testCasEstConnuePerdante(jeu1, true);
        testCasEstConnuePerdante(jeu2, false);
    }

    /**
     * Test a case of the estConnuePerdante() method
     * @param jeu the game board
     * @param res the expected result
     */
    void testCasEstConnuePerdante (ArrayList<Integer> jeu, boolean res) {
        posPerdantes.clear();
        // Affichage du test
        System.out.print("estConnuePerdante (" + jeu.toString() + ") : ");
        // Si la positon est connue, resExec est vrai

        ArrayList<Integer> positionPerdante = new ArrayList<Integer>();
        positionPerdante.add(10);
        positionPerdante.add(11);
        posPerdantes.add(positionPerdante);

        boolean resExec = estConnuePerdante(jeu);
        // Affichage du résultat
        System.out.print(jeu.toString() + " " + resExec + " : ");
        if (res == resExec) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }

    /**
     * This method returns true if and only if AFTER normalizing the game, there is a tie between the game and a situation known as
     * winner in the table of winning situations posWinner.
     * @param jeu normalized game configuration
     * @return true if this configuration is known false otherwise
     */
    boolean estConnueGagnante ( ArrayList<Integer> jeu ) {
        boolean connue = false;
        if (jeu == null) {
            System.err.println("jouerGagnant(): le paramètre jeu est null");

        } else {
            if (posGagnantes.contains(jeu)){
                connue = true;
            }
        }
        return connue;
    }

    /**
     * Tests of the estConnueGagnante() method
     */
    void testEstConnueGagnante() {
        System.out.println();
        System.out.println("*** testEstConnueGagnante() ***");

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(3);
        jeu1.add(5);
        ArrayList<Integer> jeu2 = new ArrayList<Integer>();
        jeu2.add(5);
        jeu2.add(6);

        System.out.println("Test des cas normaux");
        testCasEstConnueGagnante(jeu1, true);
        testCasEstConnueGagnante(jeu2, false);
    }

    /**
     * Test a case of the estConnueGagnant() method
     * @param jeu the game board
     * @param res the expected result
     */
    void testCasEstConnueGagnante (ArrayList<Integer> jeu, boolean res) {
        posGagnantes.clear();
        // Affichage du test
        System.out.print("estConnueGagnante (" + jeu.toString() + ") : ");
        // Si la positon est connue, resExec est vrai

        ArrayList<Integer> positionGagnante = new ArrayList<Integer>();
        positionGagnante.add(3);
        positionGagnante.add(5);
        posGagnantes.add(positionGagnante);

        boolean resExec = estConnueGagnante(jeu);
        // Affichage du résultat
        System.out.print(jeu.toString() + " " + resExec + " : ");
        if (res == resExec) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }

    /**
     * Normalise the game configuration passed in parameter
     * @param jeu current game configuration
     * @return game normalise
     */
    ArrayList<Integer> normaliser(ArrayList<Integer> jeu) {
        ArrayList<Integer> jeuNormalise = new ArrayList<>();

        // Boucle normalisant l'arraylist jeuNormalise
        for (int i = 0; i < jeu.size(); i++) {
            if (jeu.get(i) > 2) {
                jeuNormalise.add(jeu.get(i));
            }
        }

        // On procède au tri de jeuNormalise
        int min;
        int temp;
        for (int i = 0 ; i < jeuNormalise.size() ; i++) {
            min = i;
            for (int j = i ; j < jeuNormalise.size() ; j++) {
                if (jeuNormalise.get(j) < jeuNormalise.get(min) ) {
                    min = j;
                }
            }
            // Echange des valeurs
            temp = jeuNormalise.get(i);
            jeuNormalise.set(i, jeuNormalise.get(min));
            jeuNormalise.set(min, temp);
        }
        return jeuNormalise;
    }

    /**
     * Test of the normaliser() method
     */
    void testNormaliser() {
        System.out.println();
        System.out.println("*** testNormaliser() ***");

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        jeu1.add(11);
        jeu1.add(2);
        jeu1.add(1);
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(10);
        res1.add(11);

        ArrayList<Integer> jeu2 = new ArrayList<Integer>();
        jeu2.add(10);
        jeu2.add(11);
        jeu2.add(2);
        jeu2.add(3);
        jeu2.add(1);
        jeu2.add(5);
        ArrayList<Integer> res2 = new ArrayList<Integer>();
        res2.add(3);
        res2.add(5);
        res2.add(10);
        res2.add(11);

        System.out.println("Test des cas normaux");
        testCasNormaliser(jeu1, res1);
        testCasNormaliser(jeu2, res2);
        System.out.println();
    }

    /**
     * Test of a case of the normaliser() method
     * @param jeu board
     * @param res expected result
     */
    void testCasNormaliser(ArrayList<Integer> jeu, ArrayList<Integer> res) {
        // Affichage du test
        System.out.print("normaliser (" + jeu.toString() + ") : ");
        // Execution de la méthode
        ArrayList<Integer> resExec = normaliser(jeu);
        // Affichage du résultat
        System.out.print(resExec.toString() + " : ");
        if (res.equals(resExec)) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }

    /**
     * Simplifies the game by removing losing configurations
     * @param jeu board
     * @return simplified game
     */
    ArrayList<Integer> simplifier(ArrayList<Integer> jeu) {
        ArrayList<Integer> jeuSimplifier = new ArrayList<>();
        ArrayList<Integer> aSupprimer = new ArrayList<>();
        boolean[] estSupprime = new boolean[jeu.size()];

        for (int i = 0 ; i < jeu.size() ; i++) {

            // Cette arraylist va contenir un à un tous les tas de la collection jeu
            ArrayList<Integer> tasParTas = new ArrayList<>();
            tasParTas.add( jeu.get(i) );

            if ( ! posPerdantes.contains( tasParTas ) ) {
                jeuSimplifier.add( jeu.get(i) );
            }
        }

        // On procède à la simplification de la Version 4 en comparant chaque élément entre eux

        for (int i = 0; i < jeuSimplifier.size(); i++) {
            if ( !estSupprime[i] && jeuSimplifier.get(i) <= 50) { // Ignorer les tas déjà marqués pour suppression
                for (int j = i + 1; j < jeuSimplifier.size(); j++) {
                    if (jeuSimplifier.get(j) <= 50 && !estSupprime[j] && type[jeuSimplifier.get(i)] == type[jeuSimplifier.get(j)]) {
                        estSupprime[i] = true;
                        estSupprime[j] = true;
                    }
                }
            }
        }

        ArrayList<Integer> resultat = new ArrayList<>();
        for (int i = 0; i < jeuSimplifier.size(); i++) {
            if (!estSupprime[i]) {
                resultat.add(jeuSimplifier.get(i));
            }
        }

        return resultat;
    }

    /**
     * Test of the simplifier() method
     */
    void testSimplifier() {
        System.out.println();
        System.out.println("*** testSimplifier() ***");

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(3);
        jeu1.add(5);
        jeu1.add(2);
        jeu1.add(1);
        jeu1.add(4);
        jeu1.add(7);
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(3);
        res1.add(5);

        System.out.println("Test des cas normaux");
        testCasSimplifier(jeu1, res1);
        System.out.println();
    }

    /**
     * Test case for the simplifier() method
     * @param jeu board
     * @param res expected result
     */
    void testCasSimplifier(ArrayList<Integer> jeu, ArrayList<Integer> res) {
        posPerdantes.clear();
        // Affichage du test
        System.out.print("simplifier (" + jeu.toString() + ") : ");

        ArrayList<Integer> positionPerdante = new ArrayList<Integer>();
        positionPerdante.add(4);
        positionPerdante.add(7);
        posPerdantes.add(positionPerdante);

        // Execution de la méthode
        ArrayList<Integer> resExec = new ArrayList<>(simplifier(jeu));
        // Affichage du résultat
        System.out.print(resExec.toString() + " : ");
        if (res.equals(resExec)) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
        posPerdantes.clear();
    }

    /**
     * Create a game display in the terminal
     * @param jeu board with heaps of matches
     */
    void afficher(ArrayList<Integer> jeu) {

        // Affichage du jeu
        System.out.println("Plateau de jeu : ");
        for (int i = 0 ; i < jeu.size() ; i++) {
            System.out.print("    ");
            for (int j = 0 ; j < jeu.get(i) ; j++) {
                System.out.print("|");
            }
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Play the winning move if it exists
     *
     * @param jeu board
     * @return true if there is a winning move, false otherwise
     */
    boolean jouerGagnant(ArrayList<Integer> jeu) {
        boolean gagnant = false;

        if (jeu == null) {
            System.err.println("jouerGagnant(): le paramètre jeu est null");
        } else {
            ArrayList<Integer> essai = new ArrayList<Integer>();

            // Une toute première décomposition est effectuée à partir de jeu.
            // Cette première décomposition du jeu est enregistrée dans essai.
            // ligne est le numéro de la case du tableau ArrayList (qui commence à zéro) qui
            // mémorise le tas (nbre d'allumettes) qui a été décomposé
            int ligne = premier(jeu, essai);

            // Mise en oeuvre de la règle numéro2
            // Une situation (ou position) est dite gagnante pour la machine, s’il existe AU MOINS UNE décomposition
            // (c-à-d UNE action qui consiste à décomposer un tas en 2 tas inégaux) perdante pour l’adversaire. C'est
            // évidemment cette décomposition perdante qui sera choisie par la machine.
            while (ligne != -1 && !gagnant) {
                // estPerdante est récursif
                if (estPerdante(essai)) {
                    // estPerdante (pour l'adversaire) à true ===> Bingo essai est la décomposition choisie par la machine qui est alors
                    // certaine de gagner !!
                    jeu.clear();
                    gagnant = true;

                    // essai est recopié dans jeu car essai est la nouvelle situation de jeu après que la machine ait joué (gagnant)
                    for (int i = 0; i < essai.size(); i++) {
                        jeu.add(essai.get(i));
                    }
                } else {
                    // estPerdante à false ===> la machine essaye une autre décomposition en faisant appel à "suivant".
                    // Si, après exécution de suivant, ligne est à (-1) alors il n'y a plus de décomposition possible à partir de jeu (et on sort du while).
                    // En d'autres mots : la machine n'a PAS trouvé à partir de jeu UNE décomposition gagnante.
                    ligne = suivant(jeu, essai, ligne);
                }
            }
        }
        return gagnant;
    }

    /**
     * RECURSIVE method which indicates whether the configuration (of the current game or trial game) is losing.
     * This method is used by the machine to determine whether the opponent can lose (at 100%).
     *
     * @param jeu current game board (the state of the game at a certain point during the game)
     * @return true if the (game) configuration is losing, false otherwise
     */
    boolean estPerdante(ArrayList<Integer> jeu) {

        boolean ret = true; // par défaut la configuration est perdante

        if (jeu == null) {
            System.err.println("estPerdante(): le paramètre jeu est null");
        }

        else {
            // si il n'y a plus que des tas de 1 ou 2 allumettes dans le plateau de jeu
            // alors la situation est forcément perdante (ret=true) = FIN de la récursivité
            if ( !estPossible(jeu) ) {
                ret = true;
            }

            else {
                // On normalise le jeu...
                ArrayList<Integer> jeuNormalise = new ArrayList<>();
                jeuNormalise = normaliser(jeu);

                //... puis on le simplifie pour éviter encore d'autre calcul redondant
                jeuNormalise = simplifier(jeuNormalise);

                // On vérifie si la configuration est déjà connue
                if (estConnuePerdante(jeuNormalise) == true) {
                    ret = true;

                } else if ( estConnueGagnante(jeuNormalise) == true ) {
                    ret = false;

                } else {
                    // création d'un jeu d'essais qui va examiner toutes les décompositions
                    // possibles à partir de jeu
                    ArrayList<Integer> essai = new ArrayList<Integer>(); // size = 0 !

                    // toute première décomposition : enlever 1 allumette au premier tas qui possède
                    // au moins 3 allumettes, ligne = -1 signifie qu'il n'y a plus de tas d'au moins 3 allumettes
                    int ligne = premier(jeu, essai);

                    while ( (ligne != -1) && ret) {

                        cpt++; // compteur du nombre de tour de boucle pour la méthode efficacité

                        // mise en oeuvre de la règle numéro1
                        // Une situation (ou position) est dite perdante si et seulement si TOUTES ses décompositions possibles
                        // (c-à-d TOUTES les actions qui consistent à décomposer un tas en 2 tas inégaux) sont TOUTES gagnantes
                        // (pour l’adversaire).
                        // L'appel à "estPerdante" est RECURSIF.

                        // Si "estPerdante(essai)" est true c'est équivalent à "estGagnante" est false, la décomposition
                        // essai n'est donc pas gagnante, on sort du while et on renvoie false.

                        if (estPerdante(essai) == true) {

                            // Si UNE SEULE décomposition (à partir du jeu) est perdante (pour l'adversaire) alors le jeu n'EST PAS perdant.
                            // On renverra donc false : la situation (jeu) n'est PAS perdante.

                            // Ajout dans posPerdante
                            if (! posGagnantes.contains(jeuNormalise)) {
                                posGagnantes.add(new ArrayList<>(jeuNormalise));
                            }

                            ret = false;

                        } else {
                            // Ajout dans posPerdante
                            // génère la configuration d'essai suivante (c'est-à-dire UNE décomposition possible)
                            // à partir du jeu, si ligne = -1 il n'y a plus de décomposition possible
                            ligne = suivant(jeu, essai, ligne);
                        }
                    }
                }
                if (ret) {
                    if (jeuNormalise.size() > 0) {
                        posPerdantes.add(new ArrayList<>(jeuNormalise));
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Indicates whether the configuration is a winner.
     * Method that simply calls ‘isLosing’.
     *
     * @param jeu board
     * @return true if the configuration is winning, false otherwise
     */
    boolean estGagnante(ArrayList<Integer> jeu) {
        boolean ret = false;
        if (jeu == null) {
            System.err.println("estGagnante(): le paramètre jeu est null");
        } else {
            ret = !estPerdante(jeu);
        }
        return ret;
    }


    /**
     * import java.sql.SQLOutput;
     * Method for testing the effectiveness of estGagnant()
     */
    void testEstGagnanteEfficacite() {

        System.out.println("*** résultats des tests de l'efficacité de estGagnante() : ***");
        posPerdantes.clear();
        posGagnantes.clear();
        // paramètres de estGagnante()
        ArrayList<Integer> jeu = new ArrayList<Integer>();

        // Variables pour mesurer le temps d'exécution
        long t1, t2, diffT;

        // Initialisation du nombre d'allumette
        int n = 3;
        jeu.add(n);

        for (int i = 1 ; i <= 68 ; i++) {
            cpt = 0;

            // Initialisation
            jeu = new ArrayList<Integer>();
            jeu.add(n);

            t1 = System.nanoTime();
            boolean ret = estGagnante(jeu);
            t2 = System.nanoTime();

            // Calcul de la difference de temps
            diffT = t2 - t1;
            System.out.println("n = " + n);
            System.out.println("Temps = " + diffT /1000+ " microsecondes");

            // Affichage de cpt
            System.out.println("cpt : " + cpt);
            System.out.println("estGagnant(jeu) = " + ret);

            posPerdantes.clear();
            posGagnantes.clear();

            n++;
        }
    }


    /**
     * Brief tests of the jouerGagnant() method
     */
    void testJouerGagnant() {
        System.out.println();
        System.out.println("*** testJouerGagnant() ***");

        System.out.println("Test des cas normaux");
        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(6);
        ArrayList<Integer> resJeu1 = new ArrayList<Integer>();
        resJeu1.add(4);
        resJeu1.add(2);

        testCasJouerGagnant(jeu1, resJeu1, true);

    }

    /**
     * Test of a case of the jouerGagnant() method
     *
     * @param jeu the game board
     * @param resJeu the game board after playing Winner
     * @param res the result expected by playWinner
     */
    void testCasJouerGagnant(ArrayList<Integer> jeu, ArrayList<Integer> resJeu, boolean res) {
        // Arrange
        System.out.print("jouerGagnant (" + jeu.toString() + ") : ");

        // Act
        boolean resExec = jouerGagnant(jeu);

        // Assert
        System.out.print(jeu.toString() + " " + resExec + " : ");
        boolean egaliteJeux = jeu.equals(resJeu);
        if (  egaliteJeux && (res == resExec) ) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }

    /**
     * Divide the matches in a line into two piles (1 line = 1 pile).
     * The new pile is placed at the end of the board.
     * The pile that is divided decreases by the number of matches removed.
     *
     * @param jeu array of matches per line
     * @param ligne heap for which matches must be separated
     * @param nb number of matches REMOVED from the heap (line) on separation
     */
    void enlever ( ArrayList<Integer> jeu, int ligne, int nb ) {
        // traitement des erreurs
        if (jeu == null) {
            System.err.println("enlever() : le paramètre jeu est null");
        } else if (ligne >= jeu.size()) {
            System.err.println("enlever() : le numéro de ligne est trop grand");
        } else if (nb >= jeu.get(ligne)) {
            System.err.println("enlever() : le nb d'allumettes à retirer est trop grand");
        } else if (nb <= 0) {
            System.err.println("enlever() : le nb d'allumettes à retirer est trop petit");
        } else if (2 * nb == jeu.get(ligne)) {
            System.err.println("enlever() : le nb d'allumettes à retirer est la moitié");
        } else {
            // nouveau tas ajouté au jeu (nécessairement en fin de tableau)
            // ce nouveau tas contient le nbre d'allumettes retirées (nb) du tas à séparer
            jeu.add(nb);
            // le tas restant possède "nb" allumettes en moins
            jeu.set ( ligne, (jeu.get(ligne) - nb) );
        }
    }

    /**
     * Tests whether it is possible to separate one of the heaps
     *
     * @param jeu game board
     * @return true if there is at least one heap of 3 matches or more, false otherwise
     */
    boolean estPossible(ArrayList<Integer> jeu) {
        boolean ret = false;
        if (jeu == null) {
            System.err.println("estPossible(): le paramètre jeu est null");
        } else {
            int i = 0;
            while (i < jeu.size() && !ret) {
                if (jeu.get(i) > 2) {
                    ret = true;
                }
                i = i + 1;
            }
        }
        return ret;
    }

    /**
     * Create an initial test configuration from the game
     *
     * @param jeu board
     * @param jeuEssai new game configuration
     * @return the number of the heap divided in two or (-1) if there is no heap of at least 3 matches
     */
    int premier(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai) {

        int numTas = -1; // pas de tas à séparer par défaut
        int i;

        if (jeu == null) {
            System.err.println("premier(): le paramètre jeu est null");
        } else if (!estPossible((jeu)) ){
            System.err.println("premier(): aucun tas n'est divisible");
        } else if (jeuEssai == null) {
            System.err.println("premier(): le paramètre jeuEssai est null");
        } else {
            // avant la copie du jeu dans jeuEssai il y a un reset de jeuEssai
            jeuEssai.clear(); // size = 0
            i = 0;

            // recopie case par case de jeu dans jeuEssai
            // jeuEssai est le même que le jeu avant la première configuration d'essai
            while (i < jeu.size()) {
                jeuEssai.add(jeu.get(i));
                i = i + 1;
            }

            i = 0;
            // rechercher un tas d'allumettes d'au moins 3 allumettes dans le jeu
            // sinon numTas = -1
            boolean trouve = false;
            while ( (i < jeu.size()) && !trouve) {

                // si on trouve un tas d'au moins 3 allumettes
                if ( jeuEssai.get(i) >= 3 ) {
                    trouve = true;
                    numTas = i;
                }

                i = i + 1;
            }

            // sépare le tas (case numTas) en un nouveau tas d'UNE SEULE allumette qui vient se placer en fin du tableau
            // le tas en case numTas a diminué d'une allumette (retrait d'une allumette)
            // jeuEssai est le plateau de jeu qui fait apparaître cette séparation
            if ( numTas != -1 ) enlever ( jeuEssai, numTas, 1 );
        }

        return numTas;
    }

    /**
     * Brief tests of the premier() method
     */
    void testPremier() {
        System.out.println();
        System.out.println("*** testPremier()");

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        jeu1.add(11);
        int ligne1 = 0;
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(9);
        res1.add(11);
        res1.add(1);
        testCasPremier(jeu1, ligne1, res1);
    }

    /**
     * Test a case of the testPremier method
     * @param jeu the game board
     * @param ligne the number of the heap split first
     * @param res the game board after a first split
     */
    void testCasPremier(ArrayList<Integer> jeu, int ligne, ArrayList<Integer> res) {
        // Arrange
        System.out.print("premier (" + jeu.toString() + ") : ");
        ArrayList<Integer> jeuEssai = new ArrayList<Integer>();
        // Act
        int noLigne = premier(jeu, jeuEssai);
        // Assert
        System.out.println("\nnoLigne = " + noLigne + " jeuEssai = " + jeuEssai.toString());
        boolean egaliteJeux = jeuEssai.equals(res);
        if ( egaliteJeux && noLigne == ligne ) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }

    /**
     * Generates the following test configuration (i.e. ONE possible decomposition)
     *
     * @param jeu game board
     * @param jeuEssai trial configuration of the game after separation
     * @param ligne the number of the heap which was the last to be split
     * @return the number of the heap divided in two for the new configuration, -1 if no further decomposition is possible
     */
    int suivant(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai, int ligne) {

        // System.out.println("suivant(" + jeu.toString() + ", " +jeuEssai.toString() +
        // ", " + ligne + ") = ");

        int numTas = -1; // par défaut il n'y a plus de décomposition possible

        int i = 0;
        // traitement des erreurs
        if (jeu == null) {
            System.err.println("suivant(): le paramètre jeu est null");
        } else if (jeuEssai == null) {
            System.err.println("suivant() : le paramètre jeuEssai est null");
        } else if (ligne >= jeu.size()) {
            System.err.println("suivant(): le paramètre ligne est trop grand");
        }

        else {

            int nbAllumEnLigne = jeuEssai.get(ligne);
            int nbAllDernCase = jeuEssai.get(jeuEssai.size() - 1);

            // si sur la même ligne (passée en paramètre) on peut encore retirer des allumettes,
            // c-à-d si l'écart entre le nombre d'allumettes sur cette ligne et
            // le nombre d'allumettes en fin de tableau est > 2, alors on retire encore
            // 1 allumette sur cette ligne et on ajoute 1 allumette en dernière case
            if ( (nbAllumEnLigne - nbAllDernCase) > 2 ) {
                jeuEssai.set ( ligne, (nbAllumEnLigne - 1) );
                jeuEssai.set ( jeuEssai.size() - 1, (nbAllDernCase + 1) );
                numTas = ligne;
            }

            // sinon il faut examiner le tas (ligne) suivant du jeu pour éventuellement le décomposer
            // on recrée une nouvelle configuration d'essai identique au plateau de jeu
            else {
                // copie du jeu dans JeuEssai
                jeuEssai.clear();
                for (i = 0; i < jeu.size(); i++) {
                    jeuEssai.add(jeu.get(i));
                }

                boolean separation = false;
                i = ligne + 1; // tas suivant
                // si il y a encore un tas et qu'il contient au moins 3 allumettes
                // alors on effectue une première séparation en enlevant 1 allumette
                while ( i < jeuEssai.size() && !separation ) {
                    // le tas doit faire minimum 3 allumettes
                    if ( jeu.get(i) > 2 ) {
                        separation = true;
                        // on commence par enlever 1 allumette à ce tas
                        enlever(jeuEssai, i, 1);
                        numTas = i;
                    } else {
                        i = i + 1;
                    }
                }
            }
        }

        return numTas;
    }

    /**
     * Brief tests of the suivant() method
     */
    void testSuivant() {
        System.out.println();
        System.out.println("*** testSuivant() ****");

        int ligne1 = 0;
        int resLigne1 = 0;
        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        ArrayList<Integer> jeuEssai1 = new ArrayList<Integer>();
        jeuEssai1.add(9);
        jeuEssai1.add(1);
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(8);
        res1.add(2);
        testCasSuivant(jeu1, jeuEssai1, ligne1, res1, resLigne1);

        int ligne2 = 0;
        int resLigne2 = -1;
        ArrayList<Integer> jeu2 = new ArrayList<Integer>();
        jeu2.add(10);
        ArrayList<Integer> jeuEssai2 = new ArrayList<Integer>();
        jeuEssai2.add(6);
        jeuEssai2.add(4);
        ArrayList<Integer> res2 = new ArrayList<Integer>();
        res2.add(10);
        testCasSuivant(jeu2, jeuEssai2, ligne2, res2, resLigne2);

        int ligne3 = 1;
        int resLigne3 = 1;
        ArrayList<Integer> jeu3 = new ArrayList<Integer>();
        jeu3.add(4);
        jeu3.add(6);
        jeu3.add(3);
        ArrayList<Integer> jeuEssai3 = new ArrayList<Integer>();
        jeuEssai3.add(4);
        jeuEssai3.add(5);
        jeuEssai3.add(3);
        jeuEssai3.add(1);
        ArrayList<Integer> res3 = new ArrayList<Integer>();
        res3.add(4);
        res3.add(4);
        res3.add(3);
        res3.add(2);
        testCasSuivant(jeu3, jeuEssai3, ligne3, res3, resLigne3);

    }

    /**
     * Test a case of the following method
     *
     * @param jeu the game board
     * @param jeuEssai the game board obtained after separating a heap
     * @param ligne is the number of the heap that was last split
     * @param resJeu is the TestGame expected after splitting
     * @param resLigne is the expected number of the heap that is split
     */
    void testCasSuivant(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai, int ligne, ArrayList<Integer> resJeu, int resLigne) {
        // Arrange
        System.out.print("suivant (" + jeu.toString() + ", " + jeuEssai.toString() + ", " + ligne + ") : ");
        // Act
        int noLigne = suivant(jeu, jeuEssai, ligne);
        // Assert
        System.out.println("\nnoLigne = " + noLigne + " jeuEssai = " + jeuEssai.toString());
        boolean egaliteJeux = jeuEssai.equals(resJeu);
        if ( egaliteJeux && noLigne == resLigne ) {
            System.out.println("OK\n");
        } else {
            System.err.println("ERREUR\n");
        }
    }
}
