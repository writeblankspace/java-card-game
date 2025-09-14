import java.util.Random;

public class Cards {
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
            return this.suit.toString() + " " + this.face.toString();
        }
    }

    public enum Face {
        // Each enum value acts like a function
        // We're calling the constructor, Face()
        ACE(" A"),
        TWO(" 2"),
        THREE(" 3"),
        FOUR(" 4"),
        FIVE(" 5"),
        SIX(" 6"),
        SEVEN(" 7"),
        EIGHT(" 8"),
        NINE(" 9"),
        TEN("10"),
        JACK(" J"),
        QUEEN(" Q"),
        KING(" K");

        // We want a String version of each value
        private final String nameString;

        Face(String nameString) {
            this.nameString = nameString;
        }

        @Override
        public String toString() {
            return this.nameString;
        }
    }

    public enum Suit {
        // See definition for Face for how this works
        SPADES("♠"),
        CLUBS("♣"),
        HEARTS("♥"),
        DIAMONDS("♦");

        private final String nameString;

        Suit(String nameString) {
            this.nameString = nameString;
        }

        @Override
        public String toString() {
            return this.nameString;
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

        public Card[] drawCards(int n) throws Cards.DeckEmptyException {
            // Draw a card from the top of the deck. Does not get replaced.
            // I want to use an array (for Visual Basic reasons), so we just replace the card with null
            Card[] drawnCards = new Card[n];

            for (int i = 0; i < n; i++) {
                // Make sure the deck isn't empty
                if (this.top == this.cards.length - 1) {
                    throw new Cards.DeckEmptyException("Cannot draw card: deck is empty!");
                }

                drawnCards[i] = this.cards[this.top];
                this.cards[this.top] = null;

                this.top += 1; // The deck looks smaller now...
            }
            return drawnCards;
        }

        public void printAll() {
            for (Card card : this.cards) {
                System.out.println(card.toString());
            }
        }
    }
}
