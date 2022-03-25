import java.util.*;
/*
Author:         Quinn Helm
Student Number: 000737479
Date Completed: 24-10-2021 (DD-MM-YYYY)

Statement of Authorship:
I, Quinn Helm, student number 000737479, certify that all code submitted is my own work;
that I have not copied it from any other source. I also certify that I have not allowed my work to be copied by others.
*/
public class YahtzeeStrategy {
    // Before performing large numbers of sims, set this to false.
    // Printing to the screen is relatively slow and will cause your game to under perform.
    //final boolean _DEBUG_ = true;
    final boolean _DEBUG_ = false;

    public static int totalStupidScored = 0;
    public static int totalStupidScratched = 0;

    public void debugWrite( String str ) {
        if ( _DEBUG_ )
            System.out.println( str );
    }

    public HashMap<Yahtzee.Boxes, Integer> weightedOptions = new HashMap<>();

    Yahtzee game = new Yahtzee();

    // The basic structure of a turn is that you must roll the dice,
    // choose to score the hand or to re-roll some/all dice.
    //
    // If scoring you must provide the decision for what box to score.
    //
    // If re-rolling you must provide the decision for which dice to
    // re-roll.
    //
    // You may score or re-roll a second time after the first re-roll.
    //
    // After the second re-roll you must score or scratch (score a 0).


    // Used enumMap instead of boolean[] so I can use enums as indexes.
    // Keep track of which boxes I've already filled.
    Map<Yahtzee.Boxes, Boolean> boxFilled;
    Map<Yahtzee.Boxes, Integer> boxValues;

    boolean[] keep; // flag array of dice to keep
    int[] roll;  // current turn's dice state

    // EXAMPLE GAME PLAY
    // YOU SHOULD HEAVILY EDIT THIS LOGIC TO SUIT YOUR STRATEGY

    // Track what pattern matches are in the roll.
    Map<Yahtzee.Boxes, Boolean> thisRollHas;

    public int play() {
        debugWrite( game.toString() );
        for (int turnNum = 1; turnNum <= 13; turnNum++) {
            debugWrite( "Playing turn " + turnNum + ": ");
            boxFilled = game.getScoreState();
            boxValues = game.getScores();
            keep = new boolean[5];
            roll = game.play();

            turnRoller(turnNum);
        }
        return game.getGameScore() >= 0 ? game.getGameScore() : 0;
    }

    /**
     * Simulates rolling up to three times per turn of yahtzee, making decisions
     * according to my strategic beliefs.
     *
     * Code moved here from the above play() method for the sake of clarity.
     *
     * @param turn the current turn of the active game.
     */
    private void turnRoller(int turn) {
        Yahtzee.Boxes result;
        String output = "";
        for (int i = 0; i < 3; i++) {
            // debugWrite( "Turn " + turn + " Roll 1: " + Arrays.toString( roll ) );
            thisRollHas = game.has();
            if (i < 2) {
                if (yahtzeeScore() || straightScore()) {
                    break;
                } else {
                    coreLogic(turn);
                    roll = game.play(keep);
                }
            } else {
                result = weightedScoring(turn);
                output = result.name();
                boolean altered = scoreOrScratch(turn);
                if (!altered)
                    System.err.println("Invalid game state, can't score, can't scratch.");
                // debugWrite( game.toString() );
            }
        }
        //System.out.println("Turn " + turn + ": " + output + "\t--\t" + game.showDice() + "\t--\t" + game.toString());
        //System.out.println("----------");
    }

    /**
     * Check to determine if the current roll has a yahtzee. If so, score it.
     *
     * Code copied from the above play() method for the purpose of modularization
     *
     * @return true if yahtzee is scored, false otherwise.
     */
    private boolean yahtzeeScore() {
        if (thisRollHas.get(Yahtzee.Boxes.Y)) {
            return game.setScore("Y");
        }
        return false;
    }

    /**
     * Check to determine if the current roll has a large or small straight.
     * Score the large straight if possible, and if not, score the small straight.
     *
     * @return true if ls/ss scored, false otherwise.
     */
    private boolean straightScore() {
        if (thisRollHas.get(Yahtzee.Boxes.LS)) {
            return game.setScore("LS") || game.setScore("SS");
        } else if (thisRollHas.get(Yahtzee.Boxes.SS)) {
            return game.setScore("SS");
        }
        return false;
    }

    /**
     * Check for multiples to keep. If there are no beneficial multiples,
     * attempt to go for a large/small straight if either is open.
     * @param turn the current turn number.
     */
    private void coreLogic(int turn) {
        int[] tempRoll = roll.clone();
        Arrays.sort(tempRoll);

        if (thisRollHas.get(Yahtzee.Boxes.FK) || thisRollHas.get(Yahtzee.Boxes.TK)) {
            for (int i = 0; i < roll.length; i++)
                if (roll[i] == tempRoll[2]) keep[i] = true;
        } else {
            HashMap<Integer, Integer> pairsCheck = new HashMap<>(6);
            int highPair = 0;
            int lowPair = 0;
            for (int die : tempRoll) {
                if (pairsCheck.containsKey(die)) {
                    pairsCheck.put(die, pairsCheck.get(die) + 1);
                    if (lowPair == 0) {
                        lowPair = die;
                    } else if (die > lowPair) {
                        highPair = die;
                    }
                    //System.out.println("Roll contains a pair of " + die + "s. " + pairsCheck.containsKey(die) + " - " + pairsCheck.get(die));
                } else {
                    pairsCheck.put(die, 1);
                }
            }
            //System.out.println("Turn " + turn + " - High Pair: " + highPair + ", Low Pair: " + lowPair);
            if (highPair > 0) {
                boolean high = upperCardCheck(highPair);
                boolean low = upperCardCheck(lowPair);
                if (!boxFilled.get(Yahtzee.Boxes.FH) && ((highPair > 0 && highPair < 5) || (!high))) {
                    //System.out.println("Need to keep both pair.");
                    for (int i = 0; i < roll.length; i++) {
                        if (roll[i] == highPair) keep[i] = true;
                        if (roll[i] == lowPair) keep[i] = true;
                    }
                } else {
                    if (high) {
                        //System.out.println("Need to keep high pair.");
                        for (int i = 0; i < roll.length; i++) {
                            if (roll[i] == highPair) keep[i] = true;
                        }
                    } else {
                        if (low) {
                            //System.out.println("Need to keep low pair.");
                            for (int i = 0; i < roll.length; i++) {
                                if (roll[i] == lowPair) keep[i] = true;
                            }
                        }
                    }
                }
            } else if ((lowPair > 0 && turn > 8) || lowPair >= 3) {
                if (upperCardCheck(lowPair)) {
                    //System.out.println("Need to keep low pair.");
                    for (int i = 0; i < roll.length; i++) {
                        if (roll[i] == lowPair) keep[i] = true;
                    }
                }
            } else if (!boxFilled.get(Yahtzee.Boxes.LS) || !boxFilled.get(Yahtzee.Boxes.SS)) {
                // go for large or small straight if missing, keep necessary rolls required for this
                ArrayList<Integer> checker = new ArrayList<>(4);
                int upper = 6;
                int lower = 1;
                if (!boxFilled.get(Yahtzee.Boxes.LS) && thisRollHas.get(Yahtzee.Boxes.SS)) {
                    if (tempRoll[4] == 6) {
                        if (tempRoll[3] == 5) {
                            upper = 7;
                        }
                    }
                }
                for (int i = 0; i < roll.length; i++) {
                    if (roll[i] > lower && roll[i] < upper && !checker.contains(roll[i])) {
                        keep[i] = true;
                        checker.add(roll[i]);
                    }
                }
            }
            //System.out.println("-------");
        }
    }

    /**
     * Check to determine if the current roll has a four or three of a kind
     * and will score if possible.
     *
     * @return true if scoring occurred, false otherwise.
     */
    private boolean ofAKindScore(int turn) {
        boolean didScore = false;
        if (thisRollHas.get(Yahtzee.Boxes.FH) && !boxFilled.get(Yahtzee.Boxes.FH)) {
            didScore = game.setScore("FH");
            if (didScore) {
                return true;
            }
        }
        if (thisRollHas.get(Yahtzee.Boxes.FK) || thisRollHas.get(Yahtzee.Boxes.TK)) {
            int[] tempRoll = roll.clone();
            Arrays.sort(tempRoll);
            switch (tempRoll[2]) {
                case 1:
                    if (!boxFilled.get(Yahtzee.Boxes.U1)) {
                        didScore = game.setScore("U1");
                    }
                    break;
                case 2:
                    if (!boxFilled.get(Yahtzee.Boxes.U2)) {
                        didScore = game.setScore("U2");
                    }
                    break;
                case 3:
                    if (!boxFilled.get(Yahtzee.Boxes.U3)) {
                        didScore = game.setScore("U3");
                    }
                    break;
                case 4:
                    if (!boxFilled.get(Yahtzee.Boxes.U4)) {
                        didScore = game.setScore("U4");
                    }
                    break;
                case 5:
                    if (!boxFilled.get(Yahtzee.Boxes.U5)) {
                        didScore = game.setScore("U5");
                    }
                    break;
                case 6:
                    if (!boxFilled.get(Yahtzee.Boxes.U6)) {
                        didScore = game.setScore("U6");
                    }
                    break;
                default:
                    break;
            }
            if (!didScore) {
                if (tempRoll[2] > 3 || turn >= 6) {
                    if (thisRollHas.get(Yahtzee.Boxes.FK)) {
                        didScore = game.setScore("FK") || game.setScore("TK");
                    } else if (thisRollHas.get(Yahtzee.Boxes.TK)) {
                        didScore = game.setScore("TK");
                    }
                }
            }
        }

        return didScore;
    }

    /**
     * Checks the upper card to see if a given slot is empty.
     *
     * @param slot slot in the upper card to check.
     * @return true if empty, false otherwise.
     */
    private boolean upperCardCheck(int slot) {
        boolean canScore = false;
        switch (slot) {
            case 1:
                canScore = !boxFilled.get(Yahtzee.Boxes.U1);
                break;
            case 2:
                canScore = !boxFilled.get(Yahtzee.Boxes.U2);
                break;
            case 3:
                canScore = !boxFilled.get(Yahtzee.Boxes.U3);
                break;
            case 4:
                canScore = !boxFilled.get(Yahtzee.Boxes.U4);
                break;
            case 5:
                canScore = !boxFilled.get(Yahtzee.Boxes.U5);
                break;
            case 6:
                canScore = !boxFilled.get(Yahtzee.Boxes.U6);
                break;
            default:
                break;
        }
        return canScore;
    }

    private boolean scoreOrScratch(int turn) {
        // need more specialized logic to raise min score
        int faceTotal = 0;
        for (int die : roll) {
            faceTotal += die;
        }
        if (yahtzeeScore() || straightScore() || ofAKindScore(turn)) {
            return true;
        }
        if ((turn >= 9 || faceTotal >= 20) && !boxFilled.get(Yahtzee.Boxes.C)) {
            if (game.setScore("C")) {
                return true;
            }
        } else {
            int[] sorted = roll.clone();
            Arrays.sort(sorted);
            for (int die : sorted) {
                if (upperScored(die, turn)) {
                    return true;
                }
            }
        }
        if (scoreCheck()) {
            //System.out.println("Stupid Scoring!");
            totalStupidScored++;
            return true;
        } else if (scratchCheck(turn)) {
            totalStupidScratched++;
            //System.out.println("Stupid Scratching!");
            return true;
        }
        return false;
    }

    private boolean upperScored(int slot, int turn) {
        int extra = 0;
        boolean scored = false;

        if (boxValues.get(Yahtzee.Boxes.U6) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U6)/6) > 3) ? 6 : 0;
        }
        if (boxValues.get(Yahtzee.Boxes.U5) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U5)/5) > 3) ? 5 : 0;
        }
        if (boxValues.get(Yahtzee.Boxes.U4) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U4)/4) > 3) ? 4 : 0;
        }
        if (boxValues.get(Yahtzee.Boxes.U3) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U3)/3) > 3) ? 3 : 0;
        }
        if (boxValues.get(Yahtzee.Boxes.U2) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U2)/2) > 3) ? 2 : 0;
        }
        if (boxValues.get(Yahtzee.Boxes.U1) != null) {
            extra += ((boxValues.get(Yahtzee.Boxes.U1)/1) > 3) ? 1 : 0;
        }

        int missing = extra - slot;
        // System.out.println("Upper Scoring, I have " + extra + " points of wiggle room. Trying to score: " + slot);
        if (missing > 0 || turn > 8) {
            switch (slot) {
                case 1:
                    if (!boxFilled.get(Yahtzee.Boxes.U1)) {
                        scored = game.setScore("U1");
                    }
                    break;
                case 2:
                    if (!boxFilled.get(Yahtzee.Boxes.U2)) {
                        scored = game.setScore("U2");
                    }
                    break;
                case 3:
                    if (!boxFilled.get(Yahtzee.Boxes.U3)) {
                        scored = game.setScore("U3");
                    }
                    break;
                case 4:
                    if (!boxFilled.get(Yahtzee.Boxes.U4)) {
                        scored = game.setScore("U4");
                    }
                    break;
                case 5:
                    if (!boxFilled.get(Yahtzee.Boxes.U5)) {
                        scored = game.setScore("U5");
                    }
                    break;
                case 6:
                    if (!boxFilled.get(Yahtzee.Boxes.U6)) {
                        scored = game.setScore("U6");
                    }
                    break;
                default:
                    break;
            }
        }
        return scored;
    }

    private Yahtzee.Boxes weightedScoring(int turn) {
        // initialize all weights to 0 to find the most valuable given a situation
        for (Yahtzee.Boxes box : Yahtzee.Boxes.values()) {
            weightedOptions.put(box, 0);
        }
        int facesTotal = 0;
        for (int die : roll) {
            facesTotal += die;
        }

        if (thisRollHas.get(Yahtzee.Boxes.Y)) {
            // always the most valuable, if available
            weightedOptions.put(Yahtzee.Boxes.Y, 10000);
        }
        if (thisRollHas.get(Yahtzee.Boxes.LS)) {
            if (!boxFilled.get(Yahtzee.Boxes.LS)) {
                weightedOptions.put(Yahtzee.Boxes.LS, 999);
            } else if (!boxFilled.get(Yahtzee.Boxes.SS)) {
                weightedOptions.put(Yahtzee.Boxes.SS, 999);
            }
        } else {
            if (thisRollHas.get(Yahtzee.Boxes.SS) && !boxFilled.get(Yahtzee.Boxes.SS)) {
                weightedOptions.put(Yahtzee.Boxes.SS, 999);
            }
        }
        if (thisRollHas.get(Yahtzee.Boxes.FK)) {
            int[] tempRoll = roll.clone();
            Arrays.sort(tempRoll);
            switch (tempRoll[2]) {
                case 1:
                    if (!boxFilled.get(Yahtzee.Boxes.U1)) {
                        if (turn > 6) {
                            weightedOptions.put(Yahtzee.Boxes.U1, 99);
                        }
                        else {
                            weightedOptions.put(Yahtzee.Boxes.U1, 10);
                        }
                    }
                    break;
                case 2:
                    if (!boxFilled.get(Yahtzee.Boxes.U2)) {
                        weightedOptions.put(Yahtzee.Boxes.U2, 99);
                    }
                    break;
                case 3:
                    if (!boxFilled.get(Yahtzee.Boxes.U3)) {
                        weightedOptions.put(Yahtzee.Boxes.U3, 99);
                    } else if (!boxFilled.get(Yahtzee.Boxes.TK)) {
                        weightedOptions.put(Yahtzee.Boxes.TK, 999);
                    } else {
                        weightedOptions.put(Yahtzee.Boxes.FK, 999);
                    }
                    break;
                case 4:
                    if (!boxFilled.get(Yahtzee.Boxes.U4)) {
                        weightedOptions.put(Yahtzee.Boxes.U4, 99);
                    } else if (!boxFilled.get(Yahtzee.Boxes.FK)) {
                        weightedOptions.put(Yahtzee.Boxes.FK, 999);
                    } else {
                        weightedOptions.put(Yahtzee.Boxes.TK, 999);
                    }
                    break;
                case 5:
                    if (!boxFilled.get(Yahtzee.Boxes.U5)) {
                        weightedOptions.put(Yahtzee.Boxes.U5, 99);
                    } else if (!boxFilled.get(Yahtzee.Boxes.FK)) {
                        weightedOptions.put(Yahtzee.Boxes.FK, 999);
                    } else {
                        weightedOptions.put(Yahtzee.Boxes.TK, 999);
                    }
                    break;
                case 6:
                    if (!boxFilled.get(Yahtzee.Boxes.U6)) {
                        weightedOptions.put(Yahtzee.Boxes.U6, 99);
                    } else if (!boxFilled.get(Yahtzee.Boxes.FK)) {
                        weightedOptions.put(Yahtzee.Boxes.FK, 999);
                    } else {
                        weightedOptions.put(Yahtzee.Boxes.TK, 999);
                    }
                    break;
                default:
                    break;
            }
        } else if (thisRollHas.get(Yahtzee.Boxes.TK)) {
            int[] tempRoll = roll.clone();
            Arrays.sort(tempRoll);
            switch (tempRoll[2]) {
                case 1:
                    if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                        weightedOptions.put(Yahtzee.Boxes.FH, 200);
                    }
                    if (!boxFilled.get(Yahtzee.Boxes.U1)) {
                        if (turn > 6) {
                            weightedOptions.put(Yahtzee.Boxes.U1, 99);
                        }
                        else {
                            weightedOptions.put(Yahtzee.Boxes.U1, 10);
                        }
                    }
                    break;
                case 2:
                    if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                        weightedOptions.put(Yahtzee.Boxes.FH, 200);
                    }
                    if (!boxFilled.get(Yahtzee.Boxes.U2)) {
                        weightedOptions.put(Yahtzee.Boxes.U2, 99);
                    }
                    break;
                case 3:
                    if (!boxFilled.get(Yahtzee.Boxes.U3)) {
                        weightedOptions.put(Yahtzee.Boxes.U3, 99);
                    } else {
                        if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                            weightedOptions.put(Yahtzee.Boxes.FH, 25);
                        }
                        if (!boxFilled.get(Yahtzee.Boxes.TK)) {
                            weightedOptions.put(Yahtzee.Boxes.TK, facesTotal);
                        }
                    }
                    break;
                case 4:
                    if (!boxFilled.get(Yahtzee.Boxes.U4)) {
                        weightedOptions.put(Yahtzee.Boxes.U4, 99);
                    } else {
                        if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                            weightedOptions.put(Yahtzee.Boxes.FH, 25);
                        }
                        if (!boxFilled.get(Yahtzee.Boxes.TK)) {
                            weightedOptions.put(Yahtzee.Boxes.TK, facesTotal);
                        }
                    }
                    break;
                case 5:
                    if (!boxFilled.get(Yahtzee.Boxes.U5)) {
                        weightedOptions.put(Yahtzee.Boxes.U5, 99);
                    } else {
                        if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                            weightedOptions.put(Yahtzee.Boxes.FH, 25);
                        }
                        if (!boxFilled.get(Yahtzee.Boxes.TK)) {
                            weightedOptions.put(Yahtzee.Boxes.TK, facesTotal);
                        }
                    }
                    break;
                case 6:
                    if (!boxFilled.get(Yahtzee.Boxes.U6)) {
                        weightedOptions.put(Yahtzee.Boxes.U6, 99);
                    } else {
                        if (!boxFilled.get(Yahtzee.Boxes.TK)) {
                            weightedOptions.put(Yahtzee.Boxes.TK, 999);
                        }
                        if (!boxFilled.get(Yahtzee.Boxes.FH) && thisRollHas.get(Yahtzee.Boxes.FH)) {
                            weightedOptions.put(Yahtzee.Boxes.FH, 200);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (!boxFilled.get(Yahtzee.Boxes.C)) {
            if (turn >= 9 || facesTotal >= 20) {
                weightedOptions.put(Yahtzee.Boxes.C, 60);
            } else {
                for (int die : roll) {
                    switch (die) {
                        case 1:
                            if (!boxFilled.get(Yahtzee.Boxes.U1) && weightedOptions.get(Yahtzee.Boxes.U1) != facesTotal) {
                                if (turn > 6) {
                                    weightedOptions.put(Yahtzee.Boxes.U1, facesTotal);
                                }
                            }
                            break;
                        case 2:
                            if (!boxFilled.get(Yahtzee.Boxes.U2) && weightedOptions.get(Yahtzee.Boxes.U2) != facesTotal) {
                                if (turn > 6) {
                                    weightedOptions.put(Yahtzee.Boxes.U2, facesTotal);
                                }
                            }
                            break;
                        case 3:
                            if (!boxFilled.get(Yahtzee.Boxes.U3) && weightedOptions.get(Yahtzee.Boxes.U3) != facesTotal) {
                                weightedOptions.put(Yahtzee.Boxes.U3, facesTotal);
                            }
                            break;
                        case 4:
                            if (!boxFilled.get(Yahtzee.Boxes.U4) && weightedOptions.get(Yahtzee.Boxes.U4) != facesTotal) {
                                weightedOptions.put(Yahtzee.Boxes.U4, facesTotal);
                            }
                            break;
                        case 5:
                            if (!boxFilled.get(Yahtzee.Boxes.U5) && weightedOptions.get(Yahtzee.Boxes.U5) != facesTotal) {
                                weightedOptions.put(Yahtzee.Boxes.U5, facesTotal);
                            }
                            break;
                        case 6:
                            if (!boxFilled.get(Yahtzee.Boxes.U6) && weightedOptions.get(Yahtzee.Boxes.U1) != facesTotal) {
                                weightedOptions.put(Yahtzee.Boxes.U6, facesTotal);
                            }
                            break;
                        default:
                            break;
                    }
                }
                weightedOptions.put(Yahtzee.Boxes.C, facesTotal);
            }
        }

        // finding key with max value taken from
        // https://stackoverflow.com/questions/5911174/finding-key-associated-with-max-value-in-a-java-map
        // everything else in this method is my own strategy
        Map.Entry<Yahtzee.Boxes, Integer> valuable = null;
        //System.out.println("Current\t\t\tValuable");
        for (Map.Entry<Yahtzee.Boxes, Integer> option : weightedOptions.entrySet()) {
            if (valuable == null || option.getValue().compareTo(valuable.getValue()) > 0) {
                valuable = option;
            }
            //System.out.println(option.getKey().name() + "\t\t\t\t" + valuable.getKey().name());
        }
        return valuable.getKey();
    }

    /**
     * This method attempts to dumbly score one of the boxes.
     * Copy+pasted from the above play() method in an attempt to modularize the application.
     *
     * @return true if a box was scored, false otherwise.
     */
    private boolean scoreCheck() {
        boolean scored = false;
        for (Yahtzee.Boxes b : Yahtzee.Boxes.values()) {
            switch (b) {
                // yes, at this point I wish I hadn't used strings ...
                // but I can set priority by rearranging things
                case U1:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U1");
                    break;
                case U2:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U2");
                    break;
                case U3:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U3");
                    break;
                case U4:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U4");
                    break;
                case U5:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U5");
                    break;
                case U6:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("U6");
                    break;
                case TK:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("TK");
                    break;
                case FH:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("FH");
                    break;
                case SS:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("SS");
                    break;
                case C:
                    if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore("C");
                    break;
            }
            if (scored) {
                break;
            }
        }
        return scored;
    }

    /**
     * Attempts to scratch a box, by specific order depending on which turn it
     * currently is. If it doesn't scratch according to that logic, it scratches dumbly.
     *
     * Dumb scratch code moved here from above play() method for purpose of modularization.
     *
     * @param turn the current turn of the active game.
     * @return true if scratch occurred, false otherwise.
     */
    private boolean scratchCheck(int turn) {
        if (game.scratchBox(Yahtzee.Boxes.U1) || game.scratchBox(Yahtzee.Boxes.Y) || game.scratchBox(Yahtzee.Boxes.FH)
                || game.scratchBox(Yahtzee.Boxes.FK) || game.scratchBox(Yahtzee.Boxes.LS)
                || game.scratchBox(Yahtzee.Boxes.SS) || game.scratchBox(Yahtzee.Boxes.TK)) {
            return true;
        } else {
            // must scratch, let's do it stupidly
            for (Yahtzee.Boxes b : Yahtzee.Boxes.values()) {
                if (game.scratchBox(b)) {
                    return true;
                }
            }
        }
        return false;
    }
}