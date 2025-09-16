import java.util.*;

public class Blackjack {
    // Used for special statuses for the hand

    /**
     * The different statuses that a hand in Blackjack may take.
     * <p>
     * Their <code>.toString()</code> values are used for printing 'hands'
     * to the console, and use exactly 6 characters for alignment.
     */
    public enum HandStatus {
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
    public static class Hand {

        public ArrayList<Cards.Card> cards;
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
                if (card.face == Cards.Face.ACE) {
                    numAces++;
                } else {
                    res += card.face.value;
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

        // Checks if the hand is a 21 or a bust and updates the hand's status accordingly

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
         */
        public void updateStatus(int turn) {
            // All statuses except for SPLIT mean that the hand takes no more cards
            // So leave the status be if it is null or SPLIT
            if (this.status == null || this.status == HandStatus.SPLIT) {
                int handValue = this.getValue();

                if (handValue == 21) {
                    // It could be a Blackjack!
                    if (this.cards.size() == 2) {
                        // A 10-valued card and an ace from a split isn't considered a blackjack
                        if (this.cards.stream().anyMatch(x -> x.face == Cards.Face.ACE)
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
                } else if (!(this.turnStatusUpdated == turn && this.status == HandStatus.SPLIT)) {
                    // The currentTurn is over and the hand is below 21
                    // And the status wasn't updated as a split this currentTurn
                    this.status = null;
                }

                this.turnStatusUpdated = turn;
            }
        }

        /**
         * Updates the status of this hand to the <code>newStatus</code>, then
         * runs <code>updateStatus(int currentTurn)</code>.
         *
         * @param turn      the current currentTurn in the game
         * @param newStatus the status to set this hand to
         */
        public void updateStatus(int turn, HandStatus newStatus) {
            this.status = newStatus;
            this.turnStatusUpdated = turn;

            this.updateStatus(turn);
        }

        /**
         * @return  the current status of the hand
         */
        public HandStatus getStatus() {
            return this.status;
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
                res[index + 2] = " * " + String.format("%1$2s", this.getValue()) + " ";
            } else if (this.status != null) {
                res[index + 2] = this.status.toString();
            } else {
                res[index + 2] = "   " + String.format("%1$2s", this.getValue()) + " ";
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

    // Gameplay for a single-player game against a dealer
    // After much consulting of the Wikipedia page

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
        private int currentPlayerHandIndex;
        private Hand dealerHand;
        private int currentTurn;
        private GameDebugger debugger;

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
         * Initialises this game.
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

            // TODO: allow the player to start with more than one hand

            // The player starts with a single hand
            this.playerHands = new ArrayList<>();
            this.playerHands.add(new Hand());
            this.currentPlayerHandIndex = 0;

            // Initialise the dealer's hand
            this.dealerHand = new Hand();

            // Turns start at 0
            this.currentTurn = 0;
        }

        /**
         * Initialises this game.
         */
        Game() {
            this(new GameDebugger());
        }

        /**
         * Initialises a hand by drawing two cards and adding it to the hand.
         * <p>
         * To be used when a new hand is created and it does not come from a
         * split (for example, at the start of the game when the player is
         * asked for how many hands to play).
         *
         * @param playerHandIndex   the index of the player's hand to initialise
         */
        private void initializeHand(int playerHandIndex) {
            Cards.Card[] cardBuffer;
            cardBuffer = deck.drawCards(2);
            this.playerHands.get(playerHandIndex).addCards(cardBuffer);
        }

        /**
         * Makes use of the <code>GameDebugger</code> to alter the behaviour
         * of the game.
         *
         * @return          a boolean that tells whether hands should be
         *                  initialized (have two cards added to them) after
         *                  this method is run
         */
        private boolean initializeWithDebugger() {
            boolean mayInitializeHand = true;

            if (Arrays.asList(this.debugger.cheats).contains(Cheat.ALL_ACES)) {
                // Turn all cards into aces
                for (int i = 0; i < this.deck.cards.length; i++) {
                    this.deck.cards[i].face = Cards.Face.ACE;
                }
            }

            for (int i = 0; i < this.debugger.deckCardFaces.length; i++) {
                this.deck.cards[i].face = this.debugger.deckCardFaces[i];
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

                mayInitializeHand = false;
            }
            return mayInitializeHand;
        }

        /**
         * Shows the player's hands on the console.
         * <p>
         * The statuses and values of each hand are also shown, as well as which
         * hand is currently being played.
         */
        private void showPlayerHands() {
            // Get the longest Hand from playerHands
            int maxLength = Collections.max(
                    this.playerHands, Comparator.comparingInt(a -> a.cards.size())
            ).cards.size();

            StringBuilder[] sbs = new StringBuilder[(maxLength * 2) + 4];
            String[][] playerHandStringses = new String[this.playerHands.size()][maxLength];

            sbs[0] = new StringBuilder();

            for (int i = 0; i < this.playerHands.size(); i++) {
                // For efficiency, populate playerHandStringses
                // so we don't go O(n^2) on the .toStrings() — just O(n) is alr
                String[] playerHandStrings = this.playerHands.get(i).toStrings();
                playerHandStringses[i] = playerHandStrings;

                // Also make the first StringBuilder row show titles for each hand
                if (i == this.currentPlayerHandIndex) {
                    sbs[0].append(" ").append("PLAY").append("  ");
                } else {
                    sbs[0].append(" ").append(i + 1).append("     ");
                }
            }

            // Build the string row by row
            for (int i = 1; i < sbs.length; i++) {
                // Initiate the StringBuilder row
                sbs[i] = new StringBuilder();

                // And get that row from each hand
                for (int j = 0; j < this.playerHands.size(); j++) {
                    String playerHandStringsRow;

                    // I don't want no NullPointerExceptions
                    if (i < playerHandStringses[j].length && playerHandStringses[j][i - 1] != null) {
                        playerHandStringsRow = playerHandStringses[j][i - 1];
                    } else {
                        playerHandStringsRow = "      ";
                    }

                    sbs[i].append(playerHandStringsRow).append(" ");
                }
            }

            for (StringBuilder sb : sbs) {
                System.out.println(sb.toString());
            }

            System.out.println();
        }

        /**
         * Shows the dealer's hand on the console.
         */
        private void showDealerHand() {
            //this.dealerHand.show();
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
            if (!(playerHand.cards.get(0).face == Cards.Face.ACE
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
                if (playerHand.cards.get(0).face.value == playerHand.cards.get(1).face.value
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
                        System.out.println("ERROR: Invalid option. Please try again.\n");
                    }
                } else {
                    System.out.println("ERROR: Invalid option. Please try again.\n");
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
         */
        private void playOption(Option option) {
            // HIT: draw a new card
            // STAND: stop drawing cards
            // DOUBLE_DOWN: double the bet and draw a card; stop drawing cards
            // SPLIT: split the two cards into two hands; draw a card for each hand
            // SURRENDER: get half the bet back and stop drawing cards
            Cards.Card[] cardsBuffer;
            Hand playerHand = this.playerHands.get(this.currentPlayerHandIndex);

            switch (option) {

                case Option.HIT:
                    // Draw a new card
                    cardsBuffer = deck.drawCards(1);
                    playerHand.addCards(cardsBuffer);
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
                    throw new Main.WhatTheHeckException("Option does not match any Option????");
            }

        }

        // TODO: start() should just be added to Game() instead

        /**
         * Starts this game.
         */
        public void start() {
            boolean mayInitializeHand = this.initializeWithDebugger();

            if (mayInitializeHand) {
                // Draw the initial two cards and add to hand
                this.initializeHand(0);
            }

            // TODO: move on from this weird demo
            // Currently, this demo plays a single move and shows the result.

            // The player may want to read the rules before playing.
            // The player may choose how many initial hands to have.
            // We must checkBlackjack() as soon as the game starts and after every playOption()
            // Then move on to the next hand after playOption(), until the end of the currentTurn.
            // This continues until all hands are done (any Option except Option.SPLIT).
            // At which point the dealer plays, and winners are decided.

            this.showPlayerHands();
            Option option = this.chooseOption();
            this.playOption(option);
            this.showPlayerHands();

            // TODO: add actual game functionality
        }

    }

}
