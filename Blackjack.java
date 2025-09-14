import java.util.*;

public class Blackjack {
    // Used for special statuses for the hand
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

    public static class Hand {

        public ArrayList<Cards.Card> cards;
        public HandStatus status;

        Hand() {
            this.cards = new ArrayList<>();
            this.status = null;
        }

        public void addCards(Cards.Card[] newCards) {
            this.cards.addAll(Arrays.asList(newCards));
        }

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

        // TODO: make a 'cache' for hands' strings so toString() won't be called everytime
        //       unless there was actually a change in the cards' content
        // Used for showing hands vertically
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

        public void show() {
            for (String string : this.toStrings()) {
                System.out.println(string);
            }
        }
    }

    public enum Cheat {
        ALL_ACES,
        BLACKJACK
    }

    // Tools for debugging a game
    public static class GameDebugger {
        int[] numCardsPerHand;
        HandStatus[] statusesForEachHand;
        Cheat[] cheats;
        int blackjackCardIndex;

        GameDebugger() {
            // Set default values
            this.numCardsPerHand = null;
            this.statusesForEachHand = new HandStatus[]{};
            this.cheats = new Cheat[]{};
            this.blackjackCardIndex = -1;
        }

        GameDebugger setNumCardsPerHand(int[] numCardsPerHand) {
            this.numCardsPerHand = numCardsPerHand;
            return this;
        }

        GameDebugger setStatusesForEachHand(HandStatus[] statusesForEachHand) {
            this.statusesForEachHand = statusesForEachHand;
            return this;
        }

        GameDebugger setCheats(Cheat[] cheats) {
            this.cheats = cheats;
            return this;
        }

        GameDebugger setBlackjackCardIndex(int blackjackCardIndex) {
            this.blackjackCardIndex = blackjackCardIndex;
            return this;
        }

    }

    // Gameplay for a single-player game against a dealer
    // After much consulting of the Wikipedia page
    public static class Game {

        private Cards.Deck deck;
        private ArrayList<Hand> playerHands;
        private int currentPlayerHandIndex;
        private Hand dealerHand;

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

        // Constructor initialises the game
        Game() {
            this.deck = new Cards.Deck();
            deck.shuffle(); // cue fancy shuffle techniques

            // The player starts with a single hand
            this.playerHands = new ArrayList<>();
            this.playerHands.add(new Hand());
            this.currentPlayerHandIndex = 0;

            // Initialise the dealer's hand
            this.dealerHand = new Hand();
        }

        // Rigs the game for debug purposes
        // Returns true if initial two cards may be added
        private boolean debugHands(GameDebugger debugger) throws Cards.DeckEmptyException {
            boolean mayInitiateHand = true;

            if (Arrays.asList(debugger.cheats).contains(Cheat.ALL_ACES)) {
                // Turn all cards into aces
                for (int i = 0; i < this.deck.cards.length; i++) {
                    this.deck.cards[i].face = Cards.Face.ACE;
                }
            }

            // Re-initiate hands
            if (debugger.numCardsPerHand != null) {
                this.playerHands.clear();
                Cards.Card[] cardsBuffer;

                for (int i = 0; i < debugger.numCardsPerHand.length; i++) {
                    this.playerHands.add(new Hand());
                    cardsBuffer = deck.drawCards(debugger.numCardsPerHand[i]);
                    this.playerHands.get(i).addCards(cardsBuffer);

                    if (i < debugger.statusesForEachHand.length) {
                        this.playerHands.get(i).status = debugger.statusesForEachHand[i];
                    }
                }

                mayInitiateHand = false;
            }
            return mayInitiateHand;
        }

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
        }

        private void showDealerHand() {
            this.dealerHand.show();
        }

        private Option chooseOption(int playerHandIndex) {
            Hand playerHand = this.playerHands.get(playerHandIndex);

            // Default options
            ArrayList<Option> options = new ArrayList<>(List.of(
                    Option.HIT, Option.STAND
            ));

            // If there are only two cards
            if (playerHand.cards.size() == 2) {
                options.add(Option.DOUBLE_DOWN);

                // If the player can split
                // i.e. both starting cards have the same value
                //      and there are less than 5 hands in play
                if (playerHand.cards.get(0).face.value == playerHand.cards.get(1).face.value
                        && playerHands.size() < 5) {
                    options.add(Option.SPLIT);
                }

                options.add(Option.SURRENDER);
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

        // TODO: implement this
        // Checks if the hand is a 21 or a bust
        private boolean updateStatus(int playerHandIndex) {
            return false;
        }

        // Play the chosen option
        private void playOption(int playerHandIndex, Option option) throws Cards.DeckEmptyException, Main.WhatTheHeckException {
            // HIT: draw a new card
            // STAND: stop drawing cards
            // DOUBLE_DOWN: double the bet and draw a card; stop drawing cards
            // SPLIT: split the two cards into two hands; draw a card for each hand
            // SURRENDER: get half the bet back and stop drawing cards
            Cards.Card[] cardsBuffer;
            Hand playerHand = this.playerHands.get(playerHandIndex);

            switch (option) {
                case Option.HIT:
                    // Draw a new card
                    cardsBuffer = deck.drawCards(1);
                    playerHand.addCards(cardsBuffer);
                    break;
                case Option.STAND:
                    // Stop drawing cards
                    playerHand.status = HandStatus.STAND;
                    break;
                case Option.DOUBLE_DOWN:
                    // (Double the bet) and draw a card
                    cardsBuffer = deck.drawCards(1);
                    playerHand.addCards(cardsBuffer);
                    // Stop drawing cards
                    playerHand.status = HandStatus.DOUBLE_DOWN;
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
                    playerHand.status = HandStatus.SPLIT;
                    newPlayerHand.status = HandStatus.SPLIT;
                    break;
                case Option.SURRENDER:
                    // (Get half the bet back) and stop drawing cards
                    playerHand.status = HandStatus.SURRENDER;
                    break;
                default:
                    throw new Main.WhatTheHeckException("Option does not match any Option????");
            }

        }

        // Play the game out
        public void start(GameDebugger debugger) throws Cards.DeckEmptyException, Main.WhatTheHeckException {
            boolean mayInitiateHand = this.debugHands(debugger);

            if (mayInitiateHand) {
                // Draw the initial two cards and add to hand
                Cards.Card[] cardBuffer;
                cardBuffer = deck.drawCards(2);
                this.playerHands.getFirst().addCards(cardBuffer);
            }

            // TODO: move on from this weird demo
            // Currently, this demo plays a single move and shows the result.
            // We must checkBlackjack() as soon as the game starts and after every playOption()
            // Then move on to the next hand after playOption(), until the end of the turn.
            // This continues until all hands are done (any Option except Option.SPLIT).
            // At which point the dealer plays, and winners are decided.

            this.showPlayerHands();
            Option option = this.chooseOption(0);
            this.playOption(0, option);
            this.showPlayerHands();

            // TODO: add actual game functionality
        }

        // Non-debug version of start()
        public void start() throws Cards.DeckEmptyException, Main.WhatTheHeckException {
            // Use the default GameDebugger
            GameDebugger debugger = new GameDebugger();
            start(debugger);
        }
    }

}
