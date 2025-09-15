
import java.util.*;

public class Main {
    /**
     * Use as a general-purpose user-defined exception for when something
     * should not be expected but somehow happens anyway.
     */
    public static class WhatTheHeckException extends Exception {
        public WhatTheHeckException(String message) {
            super(message);
        }
    }

    // TODO: adhere to
    // https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html

    /**
     * Initiates the game of Blackjack.
     * <p>
     * This program follows rules from the <a href="https://en.wikipedia.org/wiki/Blackjack">
     * Wikipedia article on Blackjack</a>, especially those that are commonly
     * practised.
     * <p>
     * This program allows a single player to play this game against a dealer.
     * Multiple hands may be played at the same time.
     *
     * @param args  some arguments that get ignored anyway
     * @throws Cards.DeckEmptyException idk
     * @throws WhatTheHeckException     idk
     */
    public static void main(String[] args)
            throws Cards.DeckEmptyException, WhatTheHeckException {
        // I have no idea how Blackjack works except that you want a 21
        // So here's the Wikipedia article: https://en.wikipedia.org/wiki/Blackjack

        Blackjack.Game game = new Blackjack.Game();

        // Create a GameDebugger instance
        Blackjack.GameDebugger debugger = new Blackjack.GameDebugger()
                .setCheats(new Blackjack.Cheat[]{Blackjack.Cheat.ALL_ACES})
                .setDeckCardFaces(new Cards.Face[]{
                })
                .setNumCardsPerHand(new int[]{2, 2, 3, 3});

        game.start(debugger);
    }
}
