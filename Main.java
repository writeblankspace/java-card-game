
import java.util.*;

public class Main {
    // Exceptions
    public static class WhatTheHeckException extends Exception {
        public WhatTheHeckException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) throws Cards.DeckEmptyException, WhatTheHeckException {
        // I have no idea how Blackjack works except that you want a 21
        // So here's the Wikipedia article: https://en.wikipedia.org/wiki/Blackjack

        Blackjack.Game game = new Blackjack.Game();

        // Create a GameDebugger instance
        Blackjack.GameDebugger debugger = new Blackjack.GameDebugger()
                .setCheats(new Blackjack.Cheat[]{})
                .setNumCardsPerHand(new int[]{2});

        game.start(debugger);
    }
}
