import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    /**
     * Use as a general-purpose user-defined exception for when something
     * should not be expected but somehow happens anyway.
     */
    public static class WhatTheHeckException extends RuntimeException {
        public WhatTheHeckException(String message) {
            super(message);
        }
    }

    // TODO: set attributes and methods to private unless otherwise necessary

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
     */
    public static void main(String[] args) {
        // I have no idea how Blackjack works except that you want a 21
        // So here's the Wikipedia article: https://en.wikipedia.org/wiki/Blackjack


        // Ask player if they would like to read the rules
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.print("Would you like to read the rules before playing? (y/N): ");

        // Ignore invalid output and move on
        if (scanner.hasNextLine()) {
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                // Show rules
                System.out.println();
                try (BufferedReader br = new BufferedReader(new FileReader("rules.txt"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("\nNow that you know the rules, let's start the game!");
            }
        }
        System.out.println();

        // Create a GameDebugger instance
        Blackjack.GameDebugger debugger = new Blackjack.GameDebugger()
                //.setCheats(new Blackjack.Cheat[]{Blackjack.Cheat.ALL_ACES})
                //.setDeckCardFaces(new Cards.Face[]{})
                .setNumCardsPerHand(new int[]{2, 2, 3, 3})
                ;

        Blackjack.Game game = new Blackjack.Game(debugger);
        game.start();
    }
}
