import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * Grundy game with AI for the machine
 * This program only contains methods to test jouerGagnant()
 * This version is brute without any improvement
 * @version 1.0
 * @author N.LESCOP - M.GOUELO
 */
class GrundyRecBruteEff {

    /** Counter to measure the efficiency of the estGagnante() method. */
    long cpt;

    /**
     * Main method of the program
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
            testEstGagnanteEfficacite();

        } else if (test == 'n' || test == 'N') {
            JoueurVsMachine();
        }
    }

    /**
     * Main game loop
     */
    void JoueurVsMachine() {
        System.out.println(" ---------- JEU GRUNDY ----------");

        // Création joueur
        String joueur;
        String machine = "Griddy";
        String joueurActuel; // Qui enregistre qui doit jouer pendant le tour actuel

        do {
            joueur = SimpleInput.getString("Quel est votre pseudo (min 3 caractères) : ");
        } while(joueur.length() < 3);

        System.out.println("On détermine aléatoirement qui est ce qui commence...");
        int PileFace = (int) Math.random();
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
                        ligne = (int) Math.random() * jeu.size(); // ligne aléatoire
                    } while(jeu.get(ligne) <= 2);

                    do {
                        nb = (int) Math.random() * jeu.get(ligne); // nombre d'allumette aléatoire
                    } while(2 * nb == jeu.get(ligne));

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
     * Displays the game board in the terminal.
     * @param jeu game board with matchstick piles.
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
     * Plays the winning move if it exists.
     *
     * @param jeu game board
     * @return true if there is a winning move, false otherwise.
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
     * RECURSIVE method that indicates whether the configuration (of the current game or test game) is losing.
     * This method is used by the machine to determine whether the opponent can lose (with 100% certainty).
     *
     * @param jeu the current game board (the state of the game at a given point during the game)
     * @return true if the configuration (of the game) is losing, false otherwise.
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
                        ret = false;

                    } else {
                        // génère la configuration d'essai suivante (c'est-à-dire UNE décomposition possible)
                        // à partir du jeu, si ligne = -1 il n'y a plus de décomposition possible
                        ligne = suivant(jeu, essai, ligne);
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Indicates whether the configuration is winning.
     * This method simply calls "estPerdante".
     *
     * @param jeu game board
     * @return true if the configuration is winning, false otherwise.
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
     * Method testing the efficiency of estGagnante().
     */
    void testEstGagnanteEfficacite() {

        System.out.println("*** résultats des tests de l'efficacité de estGagnante() : ***");

        // paramètres de estGagnante()
        ArrayList<Integer> jeu = new ArrayList<Integer>();

        // Variables pour mesurer le temps d'exécution
        long t1, t2, diffT;

        // Initialisation du nombre d'allumette
        int n = 3;
        jeu.add(n);

        for (int i = 1 ; i <= 22 ; i++) {
            cpt = 0;

            // Initialisation
            jeu = new ArrayList<Integer>();
            jeu.add(n);

            t1 = System.nanoTime();
            estGagnante(jeu);
            t2 = System.nanoTime();

            // Calcul de la différence de temps
            diffT = t2 - t1;
            System.out.println("n = " + n);
            System.out.println("Temps = " + diffT /1000+ " us");

            // Affichage de cpt
            System.out.println("cpt : " + cpt);
            System.out.println();

            n++;
        }
    }


    /**
     Basic tests for the jouerGagnant() method.
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
     * Tests a specific case of the jouerGagnant() method.
     *
     * @param jeu the game board
     * @param resJeu the game board after playing a winning move
     * @param res the expected result of jouerGagnant
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
     * Splits the matchsticks in a pile into two piles.
     * The new pile is necessarily placed at the end of the list.
     * The pile that is split decreases by the number of matchsticks removed.
     *
     * @param jeu the list of matchstick piles
     * @param ligne the pile to be split
     * @param nb the number of matchsticks REMOVED from the pile (ligne) during the split
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
     * Tests if it is possible to split one of the piles.
     *
     * @param jeu the game board
     * @return true if there is at least one pile with 3 or more matchsticks, false otherwise
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
     * Creates the very first test configuration from the game board.
     *
     * @param jeu the game board
     * @param jeuEssai the new test configuration of the game board
     * @return the number of the pile split into two, or -1 if no pile has at least 3 matchsticks
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
     * Tests the premier() method in a succinct manner.
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
     * Tests a specific case of the premier() method.
     *
     * @param jeu the game board
     * @param ligne the number of the first pile to be split
     * @param res the game board after the first split
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
     * Generates the next test configuration (i.e., one possible split).
     *
     * @param jeu the game board
     * @param jeuEssai the test configuration of the game after splitting
     * @param ligne the number of the last pile that was split
     * @return the number of the pile split into two for the new configuration, or -1 if no more splits are possible
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
     * Tests the suivant() method in a succinct manner.
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
     * Tests a specific case of the suivant() method.
     *
     * @param jeu the game board
     * @param jeuEssai the game board obtained after splitting a pile
     * @param ligne the number of the last pile that was split
     * @param resJeu the expected test configuration after splitting
     * @param resLigne the expected number of the pile that was split
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