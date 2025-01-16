import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Jeu de Grundy avec IA pour la machine
 * Ce programme ne contient que les méthodes permettant de tester jouerGagnant()
 * Cette version est brute sans aucune amélioration
 * @version 1.4
 * @author N.LESCOP - M.GOUELO
 */
class GrundyRecGplusGequalsP {
    /** Compteur pour mesurer l'efficacité de la méthode estGagnante() */
    long cpt;

    // Liste des positions perdantes
    ArrayList<ArrayList<Integer>> posPerdantes = new ArrayList<ArrayList<Integer>>();

    // Liste des positions gagnantes
    ArrayList<ArrayList<Integer>> posGagnantes = new ArrayList<ArrayList<Integer>>();

    // Liste de types
    int[] type = new int[] {0, 0, 0, 1, 0, 2, 1, 0, 2, 1, 0,
                        2, 1, 3, 2, 1, 3, 2, 4, 3, 0,
                        4, 3, 0, 4, 3, 0, 4, 1, 2, 3,
                        1, 2, 4, 1, 2, 6, 1, 2, 4, 1,
                        5, 4, 1, 5, 4, 1, 5, 4, 1, 0};

    /**
     * Méthode principal() du programme
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
     * Boucle de jeu principale
     */
    void JoueurVsMachine() {
        System.out.println(" ---------- JEU GRUNDY ----------");

        // Création joueur
        String joueur;
        String machine = "Grundy";
        String joueurActuel; // Qui enregistre qui doit jouer pendant le tour actuel

        do {
            joueur = SimpleInput.getString("Quel est votre pseudo : ");
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
                    ligne = SimpleInput.getInt("Sur quel tas voulez-vous agir : ");
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
                        System.out.println(nb);
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
     * Cette methode renvoie vrai si et seulement si APRES normalisation de jeu, il y a égalité entre jeu et une situation connue comme
     * perdante dans le tableau des situations perdantes.
     *
     * @param jeu plateau de jeu
     * @return vrai si la situation est connue comme perdante, faux sinon
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
     * Tests succincts de la méthode estConnuePerdante()
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
     * Test un cas de la méthode estConnuePerdante()
     * @param jeu le plateau de jeu
     * @param res le résultat attendu
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
     * Cette methode renvoie vrai si et seulement si APRES normalisation de jeu, il y a égalité entre jeu et une situation connue comme
     * gagnante dans le tableau des situations gagnante posGagnante.
     * @param jeu configuration de jeu normalisée
     * @return vrai si cette configuration est connue faux sinon
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
     * Tests de la methode estConnueGagnante()
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
     * Test un cas de la méthode estConnueGagnante()
     * @param jeu le plateau de jeu
     * @param res le résultat attendu
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
     * Normalise la configuration du jeu passer en paramètre
     * @param jeu configuration actuelle du jeu
     * @return jeu normaliser
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
     * Test de la méthode normaliser()
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
     * Test d'un cas de la méthode normaliser()
     * @param jeu plateau de jeu
     * @param res résultat attendu
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
     * Simplifie le jeu en retirant les configurations perdantes
     * @param jeu plateau de jeu
     * @return jeu simplifié
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
     * Test de la méthode simplifier()
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
     * Test cas de la méthode simplifier()
     * @param jeu plateau de jeu
     * @param res résultat attendu
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
     * Créer un affichage du jeu dans le terminal
     * @param jeu plateau de jeu avec les tas d'allumettes
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
     * Joue le coup gagnant s'il existe
     *
     * @param jeu plateau de jeu
     * @return vrai s'il y a un coup gagnant, faux sinon
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
     * Méthode RECURSIVE qui indique si la configuration (du jeu actuel ou jeu d'essai) est perdante.
     * Cette méthode est utilisée par la machine pour savoir si l'adversaire peut perdre (à 100%).
     *
     * @param jeu plateau de jeu actuel (l'état du jeu à un certain moment au cours de la partie)
     * @return vrai si la configuration (du jeu) est perdante, faux sinon
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
                    posPerdantes.add(new ArrayList<>(jeuNormalise));
                }
            }
        }
        return ret;
    }

    /**
     * Indique si la configuration est gagnante.
     * Méthode qui appelle simplement "estPerdante".
     *
     * @param jeu plateau de jeu
     * @return vrai si la configuration est gagnante, faux sinon
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


    /**import java.sql.SQLOutput;
     * Méthode testant l'efficacité de estGagnante()
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

        for (int i = 1 ; i <= 78 ; i++) {
            cpt = 0;

            // Initialisation
            jeu = new ArrayList<Integer>();
            jeu.add(n);

            t1 = System.nanoTime();
            estGagnante(jeu);
            t2 = System.nanoTime();

            // Calcul de la difference de temps
            diffT = t2 - t1;
            System.out.println("n = " + n);
            System.out.println("Temps = " + diffT /1000+ " microsecondes");

            // Affichage de cpt
            System.out.println("cpt : " + cpt);

            posPerdantes.clear();
            posGagnantes.clear();

            n++;
        }
    }


    /**
     * Tests succincts de la méthode joueurGagnant()
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
     * Test d'un cas de la méthode jouerGagnant()
     *
     * @param jeu le plateau de jeu
     * @param resJeu le plateau de jeu après avoir joué gagnant
     * @param res le résultat attendu par jouerGagnant
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
     * Divise en deux tas les allumettes d'une ligne de jeu (1 ligne = 1 tas).
     * Le nouveau tas se place nécessairement en fin de tableau.
     * Le tas qui est divisé diminue du nombre d'allumettes enlevées.
     *
     * @param jeu   tableau des allumettes par ligne
     * @param ligne tas pour lequel les allumettes doivent être séparées
     * @param nb    nombre d'allumettes RETIREE du tas (ligne) lors de la séparation
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
     * Teste s'il est possible de séparer un des tas
     *
     * @param jeu      plateau de jeu
     * @return vrai s'il existe au moins un tas de 3 allumettes ou plus, faux sinon
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
     * Crée une toute première configuration d'essai à partir du jeu
     *
     * @param jeu      plateau de jeu
     * @param jeuEssai nouvelle configuration du jeu
     * @return le numéro du tas divisé en deux ou (-1) si il n'y a pas de tas d'au moins 3 allumettes
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
     * Tests succincts de la méthode premier()
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
     * Test un cas de la méthode testPremier
     * @param jeu le plateau de jeu
     * @param ligne le numéro du tas séparé en premier
     * @param res le plateau de jeu après une première séparation
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
     * Génère la configuration d'essai suivante (c'est-à-dire UNE décomposition possible)
     *
     * @param jeu      plateau de jeu
     * @param jeuEssai configuration d'essai du jeu après séparation
     * @param ligne    le numéro du tas qui est le dernier à avoir été séparé
     * @return le numéro du tas divisé en deux pour la nouvelle configuration, -1 si plus aucune décomposition n'est possible
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
     * Tests succincts de la méthode suivant()
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
     * Test un cas de la méthode suivant
     *
     * @param jeu le plateau de jeu
     * @param jeuEssai le plateau de jeu obtenu après avoir séparé un tas
     * @param ligne le numéro du tas qui est le dernier à avoir été séparé
     * @param resJeu est le jeuEssai attendu après séparation
     * @param resLigne est le numéro attendu du tas qui est séparé
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