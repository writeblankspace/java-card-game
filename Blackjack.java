import java.util.*;

public class Blackjack {
    /**
     * The different statuses that a hand in Blackjack may take.
     * <p>
     * Their <code>.toString()</code> values are used for printing 'hands'
     * to the console, and use exactly 6 characters for alignment.
     */
    private enum HandStatus {
        STAND("[STND]"),
        DOUBLE_DOWN("[DBLD]"),
        SURRENDER("[SURR]"),
        BUST("[BUST]"),
        TWENTY_ONE("[ 21 ]"),
        BLACKJACK("[ BJ ]"),
        SPLIT("-SPLT-");

        private final String shorthand;

        HandStatus(String shorthand) {
            this.shorthand = shorthand;
        }

        @Override
        public String toString() {
            return this.shorthand;
        }
    }

    /**
     * A single hand in a game of Blackjack.
     * <p>
     * A hand may contain any number of cards, but due to how Blackjack works
     * it may only contain 2 to 21 cards, assuming the deck is bottomless and
     * contains at least 21 aces.
     * <p>
     * The status of this hand may be set to any from <code>HandStatus</code>.
     * The currentTurn when the status was updated must be recorded in
     * <code>turnStatusUpdated</code>, to ensure the program works as expected.
     * Thus, the use of <code>updateStatus()</code> is recommended.
     */
    private static class Hand {

        private ArrayList<Cards.Card> cards;
        private HandStatus status;
        private int turnStatusUpdated;

        Hand() {
            this.cards = new ArrayList<>();
            this.status = null;
            this.turnStatusUpdated = -1;
        }

        public void addCards(Cards.Card[] newCards) {
            this.cards.addAll(Arrays.asList(newCards));
        }

        /**
         * Calculates the value of this hand and returns it.
         * <p>
         * It ensures that aces are valued correctly according to the rules
         * of Blackjack.
         *
         * @return  the value of this hand
         */
        public int getValue() {
            int res = 0;
            int numAces = 0;

            // Set res to the sum of the values of all cards except aces
            for (Cards.Card card : this.cards) {
                if (card.getFace() == Cards.Face.ACE) {
                    numAces++;
                } else {
                    res += card.getFace().value;
                }
            }

            // All aces are valued at 11 by default, unless this causes a bust (i.e. res > 21)
            // in which case they are valued at 1
            for (int i = 0; i < numAces; i++) {
                if (res + 11 <= 21) {
                    res += 11;
                } else {
                    res += 1;
                }
            }

            return res;
        }

        /**
         * Updates the status of this hand, especially when the hand is valued
         * at 21 or higher.
         * <p>
         * Only updates the status of the hand if the status is
         * <code>null</code> or <code>HandStatus.SPLIT</code>, as a hand with
         * any other statuses wouldn't have gotten any new cards since its
         * status was updated.
         *
         * @param turn  the current currentTurn in the game
         * @return      the new status of this hand
         */
        public HandStatus updateStatus(int turn) {
            // All statuses except for SPLIT mean that the hand takes no more cards
            // So leave the status be if it is null or SPLIT
            // And if it's a DOUBLE_DOWN, we need to check for TWENTY_ONEs and BUSTs
            if (this.canBePlayed() || this.status == HandStatus.DOUBLE_DOWN) {
                int handValue = this.getValue();

                if (handValue == 21) {
                    // It could be a Blackjack!
                    if (this.cards.size() == 2) {
                        // A 10-valued card and an ace from a split isn't considered a blackjack
                        if (this.cards.stream().anyMatch(x -> x.getFace() == Cards.Face.ACE)
                                && this.status == HandStatus.SPLIT) {
                            this.status = HandStatus.TWENTY_ONE;
                        } else {
                            this.status = HandStatus.BLACKJACK;
                        }
                    } else {
                        this.status = HandStatus.TWENTY_ONE;
                    }
                } else if (handValue > 21) {
                    // Whoops, busted
                    this.status = HandStatus.BUST;
                } else if (this.turnStatusUpdated != turn
                        && this.status == HandStatus.SPLIT) {
                    // The currentTurn is over and the hand is below 21
                    // And the status wasn't updated as a split this currentTurn
                    this.status = null;
                }

                this.turnStatusUpdated = turn;
            }
            return this.status;
        }

        /**
         * Updates the status of this hand to the <code>newStatus</code>, then
         * runs <code>updateStatus(int currentTurn)</code>.
         *
         * @param turn      the current currentTurn in the game
         * @param newStatus the status to set this hand to
         * @return          the new status of this hand
         */
        public HandStatus updateStatus(int turn, HandStatus newStatus) {
            this.status = newStatus;
            this.turnStatusUpdated = turn;

            return this.updateStatus(turn);
        }

        /**
         * @return  whether the hand can be played
         */
        public boolean canBePlayed() {
            return (this.status == null || this.status == HandStatus.SPLIT);
        }

        // TODO: make a 'cache' for hands' strings so toString() won't be called everytime
        //       unless there was actually a change in the cards' content

        /**
         * Returns a <code>String[]</code> used for printing the contents of this
         * hand vertically on the console. Each element in this returned
         * Array represents a single line to be printed.
         *
         * @return  lines that can be printed to show this hand's contents
         */
        public String[] toStrings() {
            // We'll be using linear equations!
            String[] res = new String[(2 * this.cards.size()) + 4];

            for (int i = 1; i <= this.cards.size(); i++) {
                // Each card takes up two lines, so we need some linear equations
                // The top of card is shown at 2i - 2
                // The face value is shown at 2i - 1 (because index starts at 0)
                res[(2 * i) - 2] = "╭────╮";
                res[(2 * i) - 1] = "│" + this.cards.get(i - 1).toString() + "│";
            }

            // Show the bottom of the last card
            // More equations!
            int index = (2 * (this.cards.size()));
            res[index] = "│    │";
            res[index + 1] = "╰────╯";

            if (this.status == HandStatus.SPLIT) {
                res[index + 2] = " * " + String.format("%1$2s",
                        Integer.toString(this.getValue())) + " ";
            } else if (this.status != null) {
                res[index + 2] = this.status.toString();
            } else {
                res[index + 2] = "   " + String.format("%1$2s",
                        Integer.toString(this.getValue())) + " ";
            }

            return res;
        }
    }

    /**
     * Cheats used for <code>GameDebugger</code>.
     * <ul>
     *     <li><code>ALL_ACES</code>: all cards on the deck are replaced by
     *         aces. May be overridden by
     *         <code>GameDebugger.deckCardFaces</code>.</li>
     * </ul>
     */
    public enum Cheat {
        ALL_ACES,
    }

    /**
     * A toolbox with many fancy cheats used for debugging. Allows the
     * programmer to alter parts of the game, such as the number of cards in
     * each hand or what cards are at the top of the deck.
     */
    public static class GameDebugger {
        int[] numCardsPerHand;
        HandStatus[] statusesForEachHand;
        Cheat[] cheats;
        Cards.Face[] deckCardFaces;

        GameDebugger() {
            // Set default values
            this.numCardsPerHand = null;
            this.statusesForEachHand = new HandStatus[]{};
            this.cheats = new Cheat[]{};
            this.deckCardFaces = new Cards.Face[]{};
        }

        /**
         * Sets the number of cards in each hand at the beginning of the game.
         * Note that the hands' statuses are not updated when the game starts,
         * so most hands with more than two cards may end up busting before the
         * first currentTurn.
         * <p>
         * For example, setting <code>numCardsPerHand</code> to
         * <code>{2, 3, 4, 5}</code> lets the player start with 4 hands with 2,
         * 3, 4 and 5 cards, respectively.
         * <p>
         * As this is a debugging tool, nothing is stopping the programmer from
         * setting the number of cards in a hand to 1. This is not recommended
         * as the program expects the number of cards to always be 2 or more.
         *
         * @param numCardsPerHand   the number of cards in each hand
         * @return                  this GameDebugger
         */
        GameDebugger setNumCardsPerHand(int[] numCardsPerHand) {
            this.numCardsPerHand = numCardsPerHand;
            return this;
        }

        /**
         * Sets the statuses of each hand at the beginning of the game.
         * <p>
         * The statuses are applied starting with the first hand onwards. The
         * <code>statusesForEachHand</code> does not need to have the same
         * length as <code>numCardsPerHand</code>.
         *
         * @param statusesForEachHand   the statuses applied to each hand
         * @return                      this GameDebugger
         */
        GameDebugger setStatusesForEachHand(HandStatus[] statusesForEachHand) {
            this.statusesForEachHand = statusesForEachHand;
            return this;
        }

        /**
         * Sets the cheats used for the game.
         * <p>
         * These cheats cause the game to not act like a standard game of
         * Blackjack, and thus could make the game really annoying.
         *
         * @param cheats    cheats to be used for the game
         * @return          this GameDebugger
         */
        GameDebugger setCheats(Cheat[] cheats) {
            this.cheats = cheats;
            return this;
        }

        /**
         * Sets the faces of the cards at the top of the game's deck.
         * <p>
         * Useful for debugging very specific scenarios, such as a Blackjack
         * or two aces on the initial hand.
         *
         * @param cardFaces faces of the cards to be used
         * @return          this GameDebugger
         */
        GameDebugger setDeckCardFaces(Cards.Face[] cardFaces) {
            this.deckCardFaces = cardFaces;
            return this;
        }

    }

    /**
     * A single game of Blackjack.
     * <p>
     * Each game is made of several turns where the player makes a decision
     * for each hand. The player may start with more than one hand. Up to seven
     * hands may be played in a single game.
     * <p>
     * The behaviour of this game may be altered using
     * <code>GameDebugger</code>.
     */
    public static class Game {

        private Cards.Deck deck;
        private ArrayList<Hand> playerHands;
        private Hand dealerHand;
        private int currentPlayerHandIndex;
        private int currentTurn;
        private final GameDebugger debugger;

        private enum Option {
            HIT("hit"),
            STAND("stand"),
            DOUBLE_DOWN("double down"),
            SPLIT("split"),
            SURRENDER("surrender");

            final String value;

            Option(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return this.value;
            }
        }

        /**
         * Initialises variables for this game.
         *
         * @param debugger  the <code>GameDebugger</code> for altering the
         *                  behaviour of this game
         */
        Game(GameDebugger debugger) {
            // Set the debugger for the game
            this.debugger = debugger;

            // Create a new, unshuffled, super-sparkly deck
            this.deck = new Cards.Deck();
            deck.shuffle(); // cue fancy shuffle techniques

            // By default, the player starts with a single hand
            this.playerHands = new ArrayList<>();

            // Initialise the dealer's hand
            this.dealerHand = new Hand();
        }

        /**
         * Initialises variables for this game.
         * <p>
         * Uses a default <code>GameDebugger</code>.
         */
        Game() {
            this(new GameDebugger());
        }

        /**
         * Creates a new hand and initialises it by drawing two cards.
         * <p>
         * To be used when a new hand is created and it does not come from a
         * split (for example, at the start of the game when the player is
         * asked for how many hands to play).
         */
        private void initializeNewHand() {
            this.playerHands.add(new Hand());
            Cards.Card[] cardBuffer;
            cardBuffer = deck.drawCards(2);
            this.playerHands.getLast().addCards(cardBuffer);
        }

        /**
         * Makes use of the <code>GameDebugger</code> to alter the behaviour
         * of the game.
         *
         * @return          a boolean that tells whether hands should be
         *                  initialized after this method is run
         */
        private boolean initializeWithDebugger() {
            boolean mayInitializeHands = true;

            if (Arrays.asList(this.debugger.cheats).contains(Cheat.ALL_ACES)) {
                // Turn all cards into aces
                for (int i = 0; i < this.deck.cards.length; i++) {
                    this.deck.cards[i].setFace(Cards.Face.ACE);
                }
            }

            for (int i = 0; i < this.debugger.deckCardFaces.length; i++) {
                this.deck.cards[i].setFace(this.debugger.deckCardFaces[i]);
            }

            // Re-initialise hands
            if (this.debugger.numCardsPerHand != null) {
                this.playerHands.clear();
                Cards.Card[] cardsBuffer;

                for (int i = 0; i < this.debugger.numCardsPerHand.length; i++) {
                    this.playerHands.add(new Hand());
                    cardsBuffer = deck.drawCards(this.debugger.numCardsPerHand[i]);
                    this.playerHands.get(i).addCards(cardsBuffer);

                    if (i < this.debugger.statusesForEachHand.length) {
                        this.playerHands.get(i).status = this.debugger.statusesForEachHand[i];
                    }
                }

                mayInitializeHands = false;
            }
            return mayInitializeHands;
        }

        /**
         * Shows the player's and dealer's hands on the console.
         * <p>
         * The statuses and values of each hand are also shown, as well as which
         * hand is currently being played.
         */
        private void showHands() {
            // Get the longest Hand from playerHands
            int maxLength = Collections.max(
                    this.playerHands, Comparator.comparingInt(a -> a.cards.size())
            ).cards.size();
            // Also compare with the dealerHand
            if (maxLength < dealerHand.cards.size()) {
                maxLength = dealerHand.cards.size();
            }

            boolean dealerHandExists = !dealerHand.cards.isEmpty();
            int numHands = this.playerHands.size() + (dealerHandExists ? 1 : 0);

            StringBuilder[] sbs = new StringBuilder[(maxLength * 2) + 4];
            String[][] handStringses =
                    new String[numHands][maxLength];

            sbs[0] = new StringBuilder();

            // For player hands
            for (int i = 0; i < this.playerHands.size(); i++) {
                // For efficiency, populate handStringses
                // so we don't go O(n^2) on the .toStrings() — just O(n) is alr
                String[] playerHandStrings = this.playerHands.get(i).toStrings();
                handStringses[i] = playerHandStrings;

                // Also make the first StringBuilder row show titles for each hand
                if (i == this.currentPlayerHandIndex) {
                    sbs[0].append(" ").append("PLAY").append("  ");
                } else {
                    sbs[0].append("       ");
                }
            }
            // For the dealer hand
            if (dealerHandExists) {
                handStringses[handStringses.length - 1] = dealerHand.toStrings();
                sbs[0].append(" ").append("DEAL").append("  ");
            }


            // Build the string row by row
            for (int i = 1; i < sbs.length; i++) {
                // Initiate the StringBuilder row
                sbs[i] = new StringBuilder();

                // And get that row from each hand
                for (int j = 0; j < numHands; j++) {
                    String handStringsRow;

                    // I don't want no NullPointerExceptions
                    if (i < handStringses[j].length && handStringses[j][i - 1] != null) {
                        handStringsRow = handStringses[j][i - 1];
                    } else {
                        handStringsRow = "      ";
                    }

                    sbs[i].append(handStringsRow).append(" ");
                }
            }

            for (StringBuilder sb : sbs) {
                System.out.println(sb.toString());
            }

            System.out.println();

        }

        /**
         * Allows the player to make a decision (for example, to hit or stand)
         * via console input.
         * <p>
         * This method also handles which options can be chosen based on the
         * state of the game (for example, a player can only split if the two
         * initial cards in the hand have the same value)
         *
         * @return  the option chosen by the player
         */
        private Option chooseOption() {
            Hand playerHand = this.playerHands.get(this.currentPlayerHandIndex);
            ArrayList<Option> options = new ArrayList<>();

            // Hitting split aces is not allowed
            if (!(playerHand.cards.get(0).getFace() == Cards.Face.ACE
                    && playerHand.status == HandStatus.SPLIT)) {
                options.add(Option.HIT);
            }

            // Literally the only option that you can unconditionally do
            options.add(Option.STAND);

            // If there are only two cards
            if (playerHand.cards.size() == 2) {
                options.add(Option.DOUBLE_DOWN);

                // If the player can split
                // i.e. both starting cards have the same value
                //      and there are less than 7 hands in play
                if (playerHand.cards.get(0).getFace().value
                        == playerHand.cards.get(1).getFace().value
                        && playerHands.size() < 7) {
                    options.add(Option.SPLIT);
                }

                // Surrendering after a split is not allowed
                if (playerHand.status != HandStatus.SPLIT) {
                    options.add(Option.SURRENDER);
                }
            }

            StringBuilder optionsSB = new StringBuilder();

            for (int i = 0; i < options.size(); i++) {
                optionsSB.append("(").append(i + 1).append(") ").append(options.get(i)).append("\n");
            }

            // Ask the player what they want to do
            Scanner scanner = new Scanner(System.in);
            int chosenOption = -1;

            while (chosenOption < 1 || chosenOption > options.size()) {
                System.out.println(optionsSB);
                System.out.print("Enter an option number: ");

                // Don't get InputMismatchExceptions
                if (scanner.hasNextInt()) {
                    chosenOption = scanner.nextInt();
                    // Invalid input
                    if (chosenOption < 1 || chosenOption > options.size()) {
                        System.out.println("> ERROR: Invalid option. Please " +
                                "try again.\n");
                    }
                } else {
                    System.out.println("> ERROR: Invalid option. Please try " +
                            "again.\n");
                    scanner.next();
                }
            }

            return options.get(chosenOption - 1);
        }

        /**
         * Plays the option chosen by the player (for example, drawing a card
         * from the deck and adding it to the player's hand if the player
         * chose to hit).
         * <p>
         * This method is responsible for updating the status of the played
         * hand.
         *
         * @param option    the option chosen by the player
         * @return          the new status of the hand played
         */
        private HandStatus playOption(Option option) {
            // See "PLAYER OPTIONS" in rules.txt for how this works

            Cards.Card[] cardsBuffer;
            Hand playerHand = this.playerHands.get(this.currentPlayerHandIndex);

            switch (option) {

                case Option.HIT:
                    // Draw a card
                    cardsBuffer = deck.drawCards(1);
                    playerHand.addCards(cardsBuffer);
                    // Update this hand's status
                    playerHand.updateStatus(this.currentTurn);
                    break;

                case Option.STAND:
                    // Stop drawing cards
                    playerHand.updateStatus(this.currentTurn, HandStatus.STAND);
                    break;

                case Option.DOUBLE_DOWN:
                    // (Double the bet) and draw a card
                    cardsBuffer = deck.drawCards(1);
                    playerHand.addCards(cardsBuffer);
                    // Stop drawing cards
                    playerHand.updateStatus(this.currentTurn, HandStatus.DOUBLE_DOWN);
                    break;

                case Option.SPLIT:
                    // Create a new hand and transfer second card in current hand to it
                    this.playerHands.add(new Hand());
                    Hand newPlayerHand = this.playerHands.getLast();
                    Cards.Card cardBuffer = playerHand.cards.getLast();
                    newPlayerHand.cards.add(cardBuffer);
                    playerHand.cards.removeLast();
                    // Draw a card for each hand
                    cardsBuffer = this.deck.drawCards(2);
                    playerHand.cards.add(cardsBuffer[0]);
                    newPlayerHand.cards.add(cardsBuffer[1]);
                    // Set the status
                    playerHand.updateStatus(this.currentTurn, HandStatus.SPLIT);
                    newPlayerHand.updateStatus(this.currentTurn, HandStatus.SPLIT);
                    break;

                case Option.SURRENDER:
                    // (Get half the bet back) and stop drawing cards
                    playerHand.updateStatus(this.currentTurn, HandStatus.SURRENDER);
                    break;

                default:
                    // This is an unexpected result
                    throw new Main.WhatTheHeckException("Option does not match any Option????");
            }

            return playerHand.status;
        }

        /**
         * Starts this game.
         */
        public void start() {
            boolean mayInitializeHands = this.initializeWithDebugger();

            System.out.println("Shuffling...");

            if (mayInitializeHands) {
                // Ask the player for how many hands to start with
                Scanner scanner = new Scanner(System.in);
                int numHands = 1;

                do {
                    System.out.print("Enter the number of hands to start with (1 to 7): ");
                    if (scanner.hasNextInt()) {
                        numHands = scanner.nextInt();
                        // Invalid input
                        if (numHands < 1 || numHands > 7) {
                            System.out.println("> ERROR: Invalid number of " +
                                    "hands. Please try again.\n");
                        }
                    } else {
                        System.out.println("> ERROR: Invalid number of hands." +
                                " Please try again.\n");
                        scanner.next();
                    }
                } while (numHands < 1 || numHands > 7);

                // Initialise the hands
                this.currentPlayerHandIndex = 0;

                for (int i = 0; i < numHands; i++) {
                    this.initializeNewHand();
                }
            }

            System.out.println();

            // Check for any Blackjacks at the beginning of the game
            for (Hand hand : this.playerHands) {
                hand.updateStatus(0);
            }

            // Each run of the loop is a single turn
            this.currentTurn = 0;
            this.currentPlayerHandIndex = 0;
            Hand hand;
            boolean canContinueGame;

            do {
                canContinueGame = false;

                for (int i = 0; i < this.playerHands.size(); i++) {
                    hand = this.playerHands.get(i);
                    this.currentPlayerHandIndex = i;

                    // Skip hand if it can't be played
                    if (hand.canBePlayed()) {
                        this.showHands();
                        Option option = this.chooseOption();
                        this.playOption(option);

                        System.out.println();

                        if (hand.canBePlayed()) {
                            // This hand is still playable
                            canContinueGame = true;
                        }
                    }
                }

                this.currentTurn += 1;

            } while (canContinueGame);

            // TODO: show results of each hand and let the dealer play

            System.out.println("Resolving dealer's hand...");

            this.currentPlayerHandIndex = 1;

            Cards.Card[] cardsBuffer;
            while (dealerHand.getValue() < 17) {
                cardsBuffer = this.deck.drawCards(1);
                dealerHand.addCards(cardsBuffer);
            }

            dealerHand.updateStatus(-1); // No need for a turn number

            if (dealerHand.status == HandStatus.BUST) {
                // All players who haven't busted win
            }

            this.showHands();

            System.out.println("Game over");
        }

    }

}
