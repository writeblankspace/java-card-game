import java.util.Random;

public class Cards {
    public static class DeckEmptyException extends RuntimeException {
        public DeckEmptyException(String message) {
            super(message);
        }
    }

    /**
     * A general-purpose card from a standard 52-card deck.
     */
    public static class Card {

        Face face;
        Suit suit;

        // Constructor!
        Card(Face face, Suit suit) {
            this.face = face;
            this.suit = suit;
        }

        /**
         * Just like your average <code>.toString()</code> method.
         * <p>
         * The card is represented in symbols as its suit and face, which is
         * useful for printing it as a 'card' to the console.
         *
         * @return  the string representation of the card
         */
        @Override
        public String toString() {
            return this.suit.toString() + " " + this.face.toString();
        }
    }

    /**
     * The face of a card.
     * <p>
     * The values of each card are based on their values in Blackjack.
     * <p>
     * Note that <code>ACE</code> has a default value of 11.
     * This is a default value and must be overridden by the program so that
     * the ace may also take the value of 1, according to the rules of
     * Blackjack.
     * <p>
     * Their <code>.toString()</code> values are used for printing 'cards'
     * to the console. Hence, the long names of each face are not used.
     */
    public enum Face {
        // Each enum value acts like a function
        // We're calling the constructor, Face()
        ACE(" A", 11),
        TWO(" 2", 2),
        THREE(" 3", 3),
        FOUR(" 4", 4),
        FIVE(" 5", 5),
        SIX(" 6", 6),
        SEVEN(" 7", 7),
        EIGHT(" 8", 8),
        NINE(" 9", 9),
        TEN("10", 10),
        JACK(" J", 10),
        QUEEN(" Q", 10),
        KING(" K", 10);

        // We want a String version of each value
        private final String nameString;
        public final int value;

        Face(String nameString, int value) {
            this.nameString = nameString;
            this.value = value;
        }

        @Override
        public String toString() {
            return this.nameString;
        }
    }

    /**
     * The suit of a card.
     * <p>
     * Their <code>.toString()</code> values are used for printing 'cards'
     * to the console. Hence, each suit is represented by its single-character
     * symbol.
     */
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

    /**
     * A standard deck of 52 cards, by default.
     * <p>
     * It is implemented like a stack (because what is a deck but a stack of
     * cards?), except cards are not replaced into the deck. The integer
     * variable <code>top</code> acts like a pointer.
     */
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

        /**
         * Shuffles the deck using a modern Fisher-Yates shuffle.
         * <p>
         * Assuming there are no logical errors in the code, this shuffle is
         * unbiased. The house isn't always out to get you.
         */
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

        /**
         * Draws a card from the top of the deck.
         *
         * @param n     the number of cards to draw
         * @return      the <code>Card</code> drawn from the deck
         * @throws Cards.DeckEmptyException the deck is empty and thus a card
         *                                  cannot be drawn from it
         */
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

        /**
         * Prints all the cards in the deck one by one.
         * <p>
         * It will generally look ugly and unpolished, but it gets the job done.
         * Only used for debugging and may be removed anytime.
         */
        public void printAll() {
            for (Card card : this.cards) {
                System.out.println(card.toString());
            }
        }
    }
}
