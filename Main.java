
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main {

    // Exceptions
    public static class DeckEmptyException extends Exception {

        public DeckEmptyException(String message) {
            super(message);
        }
    }

    // Define the user-defined datatypes used to represent each card
    // Closest to a RECORD I can find in Java
    public static class Card {

        Face face;
        Suit suit;

        // Constructor!
        Card(Face face, Suit suit) {
            this.face = face;
            this.suit = suit;
        }

        @Override
        public String toString() {
            return this.face.toString() + " of " + this.suit.toString();
        }

        public String toShortString() {
            return this.suit.toShortString() + " " + this.face.toShortString();
        }
    }

    public enum Face {
        // Each enum value acts like a function
        // We're calling the constructor, Face()
        ACE("Ace", " A"),
        TWO("2", " 2"),
        THREE("3", " 3"),
        FOUR("4", " 4"),
        FIVE("5", " 5"),
        SIX("6", " 6"),
        SEVEN("7", " 7"),
        EIGHT("8", " 8"),
        NINE("9", " 9"),
        TEN("10", "10"),
        JACK("Jack", " J"),
        QUEEN("Queen", " Q"),
        KING("King", " K");

        // We want a String version of each value
        private final String nameString;
        private final String shortNameString;

        Face(String nameString, String shortNameString) {
            this.nameString = nameString;
            this.shortNameString = shortNameString;
        }

        @Override
        public String toString() {
            return this.nameString;
        }

        public String toShortString() {
            return this.shortNameString;
        }
    }

    public enum Suit {
        // See definition for Face for how this works
        SPADES("Spades", "♠"),
        CLUBS("Clubs", "♣"),
        HEARTS("Hearts", "♡"),
        DIAMONDS("Diamonds", "♢");

        private final String nameString;
        private final String shortNameString;

        Suit(String nameString, String shortNameString) {
            this.nameString = nameString;
            this.shortNameString = shortNameString;
        }

        @Override
        public String toString() {
            return this.nameString;
        }

        public String toShortString() {
            return this.shortNameString;
        }
    }

    public static class Deck {

        public Card[] cards;
        private int top; // Like a stack!

        Deck() {
            // Create a standard deck of 52 cards
            this.cards = new Card[52];

            // Initialise this deck
            int i = 0;
            // Loop through the enum
            // Looks like I can't do the int for-loop method like in VB
            for (Suit suit : Suit.values()) {
                for (Face face : Face.values()) {
                    this.cards[i] = new Card(face, suit); // Define a new card
                    i++;
                }
            }

            this.top = 0;
        }

        // Methods
        public void shuffle() {
            Random r = new Random();
            // Shuffle cards using the Fisher-Yates shuffle
            for (int i = this.cards.length - 1; i >= 1; i--) {
                // Random integer in range 0 to i
                int j = r.nextInt(i + 1);
                // Swap card at i with j
                Card temp = this.cards[i];
                this.cards[i] = this.cards[j];
                this.cards[j] = temp;
            }
        }

        public Card[] drawCards(int n) throws DeckEmptyException {
            // Draw a card from the top of the deck. Does not get replaced.
            // I want to use an array (for Visual Basic reasons), so we just replace the card with null
            Card[] drawnCards = new Card[n];

            for (int i = 0; i < n; i++) {
                // Make sure the deck isn't empty
                if (this.top == this.cards.length - 1) {
                    throw new DeckEmptyException("Cannot draw card: deck is empty!");
                }

                drawnCards[i] = this.cards[this.top];
                this.cards[this.top] = null;

                this.top += 1; // The deck looks smaller now...
            }
            return drawnCards;
        }

        public void printAll(boolean useShortString) {
            for (Card card : this.cards) {
                System.out.println(useShortString ? card.toShortString() : card.toString());
            }
        }
    }

    // Used for special statuses for the hand
    public enum HandStatus {
        STAND("[STND]"),
        DOUBLE_DOWN("[DBLD]"),
        SURRENDER("[SURR]"),
        BLACKJACK("[ 21 ]"),
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

        public ArrayList<Card> cards;
        public HandStatus status;

        Hand() {
            this.cards = new ArrayList<>();
            this.status = null;
        }

        public void addCards(Card[] newCards) {
            this.cards.addAll(Arrays.asList(newCards));
        }

        public void show() {
            StringBuilder[] sbs = new StringBuilder[4];
            for (int i = 0; i < sbs.length; i++) {
                sbs[i] = new StringBuilder();
            }
            for (Card card : this.cards) {
                sbs[0].append("┌────┐ ");
                sbs[1].append("│").append(card.toShortString()).append("│ ");
                sbs[2].append("│    │ ");
                sbs[3].append("└────┘ ");
            }
            for (StringBuilder sb : sbs) {
                System.out.println(sb.toString());
            }
        }

        // TODO: make a 'cache' for hands' strings so toString() won't be called everytime
        //       unless there was actually a change in the cards' content
        public String[] toStrings() {
            // Used for showing hands vertically
            // We'll be using linear equations!
            String[] res = new String[(2 * this.cards.size()) + 3];

            for (int i = 1; i <= this.cards.size(); i++) {
                // Each card takes up two lines, so we need some linear equations
                // The top of card is shown at 2i - 2
                // The face value is shown at 2i - 1 (because index starts at 0)
                res[(2 * i) - 2] = "╭────╮";
                res[(2 * i) - 1] = "│" + this.cards.get(i - 1).toShortString() + "│";
            }

            // Show the bottom of the last card
            // More equations!
            int index = (2 * (this.cards.size()));
            res[index] = "│    │";
            res[index + 1] = "╰────╯";

            if (this.status != null) {
                res[index + 2] = this.status.toString();
            } else {
                res[index + 2] = "      ";
            }

            return res;
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

        private Deck deck;
        private ArrayList<Hand> playerHands;
        private Hand dealerHand;

        private enum Option {
            HIT("hit"),
            STAND("stand"),
            DOUBLE_DOWN("double down"),
            SPLIT("split"),
            SURRENDER("surrender");

            final String value;

            private Option(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return this.value;
            }
        }

        // Constructor initialises the game
        Game() {
            this.deck = new Deck();
            deck.shuffle(); // cue fancy shuffle techniques

            // The player starts with a single hand
            this.playerHands = new ArrayList<>();
            this.playerHands.add(new Hand());

            // Initialise the dealer's hand
            this.dealerHand = new Hand();
        }

        // Rigs the game for debug purposes
        // Returns true if initial two cards may be added
        private boolean debugHands(GameDebugger debugger) throws DeckEmptyException {
            boolean mayInitiateHand = true;

            if (Arrays.asList(debugger.cheats).contains(Cheat.ALL_ACES)) {
                // Turn all cards into aces
                for (int i = 0; i < this.deck.cards.length; i++) {
                    this.deck.cards[i].face = Face.ACE;
                }
            }

            // Re-initiate hands
            if (debugger.numCardsPerHand != null) {
                this.playerHands.clear();
                Card[] cardsBuffer;

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
                    this.playerHands, (a, b) -> a.cards.size() - b.cards.size()
            ).cards.size();

            StringBuilder[] sbs = new StringBuilder[(maxLength * 2) + 3];
            String[][] playerHandStringses = new String[this.playerHands.size()][maxLength];

            // For efficiency, populate playerHandStringses
            // so we don't go O(n^2) on the .toStrings() — just O(n) is alr
            for (int i = 0; i < this.playerHands.size(); i++) {
                String[] playerHandStrings = this.playerHands.get(i).toStrings();
                playerHandStringses[i] = playerHandStrings;
            }

            // Build the string row by row
            for (int i = 0; i < sbs.length; i++) {
                // Initiate the StringBuilder row
                sbs[i] = new StringBuilder();

                // And get that row from each hand
                for (int j = 0; j < this.playerHands.size(); j++) {
                    String playerHandStringsRow;

                    // I don't want no NullPointerExceptions
                    if (i < playerHandStringses[j].length && playerHandStringses[j][i] != null) {
                        playerHandStringsRow = playerHandStringses[j][i];
                    } else {
                        playerHandStringsRow = "      ";
                    }

                    sbs[i].append(playerHandStringsRow + " ");
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
            ArrayList<Option> options = new ArrayList<>();

            // Default options
            options.addAll(List.of(
                    Option.HIT, Option.STAND
            ));

            // If there are only two cards
            if (playerHand.cards.size() == 2) {
                options.add(Option.DOUBLE_DOWN);

                // If the player can split
                // i.e. both starting cards have the same value
                //      and there are less than 5 hands in play
                Set<Face> VALUE_OF_TEN = Set.of(
                        Face.ACE, Face.JACK, Face.QUEEN, Face.KING
                );
                if ((playerHand.cards.get(0).face == playerHand.cards.get(1).face // same face
                        // both have a value of ten
                        || (VALUE_OF_TEN.contains(playerHand.cards.get(0).face)
                        && VALUE_OF_TEN.contains(playerHand.cards.get(1).face)))
                        // there are less than 5 hands in play (max 5 hands in play)
                        && playerHands.size() < 5) {
                    options.add(Option.SPLIT);
                }

                options.add(Option.SURRENDER);
            }

            StringBuilder optionsSB = new StringBuilder();

            for (int i = 0; i < options.size(); i++) {
                optionsSB.append("(" + (i + 1) + ") " + options.get(i) + "\n");
            }

            // Ask the player what they want to do
            Scanner scanner = new Scanner(System.in);
            int chosenOption = -1;

            while (chosenOption < 1 || chosenOption > options.size()) {
                System.out.println(optionsSB.toString());
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

        // Play the game out
        public void start(GameDebugger debugger) throws DeckEmptyException {
            boolean mayInitiateHand = this.debugHands(debugger);

            if (mayInitiateHand) {
                // Draw the initial two cards and add to hand
                Card[] cardBuffer;
                cardBuffer = deck.drawCards(2);
                this.playerHands.getFirst().addCards(cardBuffer);
            }

            //this.playerHands.get(0).show();
            this.showPlayerHands();
            System.out.println(this.chooseOption(0));

            // TODO: get rid of unused code, add actual game functionality
        }

        // Non-debug version of start()
        public void start() throws DeckEmptyException {
            // Use the default GameDebugger
            GameDebugger debugger = new GameDebugger();
            start(debugger);
        }
    }

    public static void main(String[] args) throws DeckEmptyException {
        // I have no idea how Blackjack works except that you want a 21
        // So here's the Wikipedia article: https://en.wikipedia.org/wiki/Blackjack

        Game game = new Game();

        // Create a GameDebugger instance
        GameDebugger debugger = new GameDebugger()
                .setCheats(new Cheat[]{Cheat.ALL_ACES})
                .setNumCardsPerHand(new int[]{2, 3, 4, 5});

        game.start(debugger);
    }
}
