package controller;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import model.Game;
import model.Gamer;

/**
 * Class to determine who wins the game and all functions that are relevant in that sense.
 * Created by: Kasper Tidemann, Henrik Thorn and Jesper Bruun Hansen
 */
public class GameEngine {

    private int hostX = -1;
    private int hostY = 1;
    private int opponentX = 1;
    private int opponentY = -1;


    public Map playGame(Game game){
        Gamer host = game.getHost();
        Gamer opponent = game.getOpponent();
        Map gamers = new HashMap();
        String hostControls = host.getControls();
        String opponentControls = opponent.getControls();

        String turn;

        /*Algorithm for determining who gets the head start in the game. E.g. if host has 80 and opponent 800
        host will have about a 9-1 or 1/10 chance of getting the turn
         */
        int hostTotal = host.getTotalScore()+1;
        int opponentTotal = opponent.getTotalScore()+1;
        boolean higherScore = hostTotal > opponentTotal;
        int noOfOutComes = hostTotal > opponentTotal ? (hostTotal + opponentTotal) / opponentTotal : (opponentTotal + hostTotal) / hostTotal;
        int dictatingNumber = new Random().nextInt(noOfOutComes);

        if (higherScore) {
            turn = dictatingNumber < (hostTotal / opponentTotal) ? "host" : "opponent";
        } else {
            turn = dictatingNumber < (opponentTotal / hostTotal) ? "opponent" : "host";
        }

        // Split each controls string into a character array:
        char[] hostControlCharacters = hostControls.toCharArray();
        char[] opponentControlCharacters = opponentControls.toCharArray();

        // The game variables for the host:
        boolean hostDidCrash = false;
        int hostScore = 0;
        int hostKills = 0;
        //x set to -1 and y to 1 to match up with client game engine

        // The game variables for the opponent:
        boolean opponentDidCrash = false;
        int opponentScore = 0;
        //x set to 1 and y to -1 to match up with client game engine
        int opponentKills = 0;

        // Playing field options:
        int boundary = game.getMapSize();

        // Total amount of moves:
        int movesCount = hostControls.length() < opponentControls.length() ? opponentControls.length() : hostControls.length();

        // Arrays of host and opponent moves:
        int[][] hostMoves = new int[movesCount][];
        int[][] opponentMoves = new int[movesCount][];

        // Loop through the host (leader) moves:
        for (int i = 0; i < hostControls.length(); i++) {
            char move = hostControlCharacters[i];

            Map coordinates = gameMove(move);

            hostMoves[i] = new int[]{(int) coordinates.get('x'), (int) coordinates.get('y')};
        }

        // Loop through the follower (opponent) moves:
        for (int i = 0; i < opponentControls.length(); i++) {
            char move = opponentControlCharacters[i];

            Map coordinates = gameMoveOpponent(move);

            opponentMoves[i] = new int[]{(int) coordinates.get('x'), (int) coordinates.get('y')};
        }

        // Loop through both moves to find collisions:
        for (int i = 0; i < movesCount; i++) {
            int[] hostMove = hostMoves[i];
            int[] opponentMove = opponentMoves[i];

            // Kills on two scales in the grid and see who was there first:
            if (hostMove != null && opponentMove != null && hostMove[0] == opponentMove[0] && hostMove[1] == opponentMove[1]) {
                if (turn.equals("host")) {
                    hostKills++;

                    opponentDidCrash = true;
                    //changing controls to the place where opponent dies to eliminate confusion
                    game.getOpponent().setControls(game.getOpponent().getControls().substring(0, i));
                } else {
                    opponentKills++;

                    hostDidCrash = true;
                    //changing controls to the place where host dies to eliminate confusion
                    game.getHost().setControls(game.getHost().getControls().substring(0, i));
                }

            }

            // Host move:
            if (hostMove != null) {

                // Check if the host move results in a wall crash:
                if (hostMove[0] > boundary || hostMove[1] > boundary) {
                    hostDidCrash = true;
                }

                // Check if the host move is not null and if the host did not previously crash:
                if (!hostDidCrash) {
                    hostScore++;
                }

            }

            // Opponent move:
            if (opponentMove != null) {

                // Check if the opponent move results in a wall crash:
                if (opponentMove[0] > boundary || opponentMove[1] > boundary) {
                    opponentDidCrash = true;
                }

                // Check if the opponent move is not null and if the opponent did not previously crash:
                if (!opponentDidCrash) {
                    opponentScore++;
                }

            }

        }
        // Set Score and Kill for the Gamer object for the host user.
        host.setScore(hostScore);
        host.setKills(hostKills);

        // Set Score and Kill for the Gamer object for the opponent user.
        opponent.setScore(opponentScore);
        opponent.setKills(opponentKills);

        // Set Winner for gamer object for the winning user.
        // If the game is draw, both object will be loser.
        if(hostScore > opponentScore) {
            host.setWinner(true);
        }
        if(hostScore < opponentScore){
            opponent.setWinner(true);
        }
        gamers.put('h', host);
        gamers.put('o', opponent);

        // Return the gamers back to the logic layer, who will now have access to see score, winner etc.
        return gamers;

    }

    private Map gameMove(char move) {

        //Create a HashMap in order to store the coordinates.
        Map newCoordinates = new HashMap();

        //Calculate the new X,Y coordinates based on the char from the user.
        if (move == 'a') {
            hostX--;
        } else if (move == 'd') {
            hostX++;
        } else if (move == 'w') {
            hostY++;
        } else if (move == 's') {
            hostY--;
        }

        //Put the newly calculated coordinates into the HashMap in order to return it.
        newCoordinates.put('x', hostX);
        newCoordinates.put('y', hostY);

        //Return the coordinates back in order to use it.
        return newCoordinates;
    }
    private Map gameMoveOpponent(char move) {

        //Create a HashMap in order to store the coordinates.
        Map newCoordinates = new HashMap();

        //Calculate the new X,Y coordinates based on the char from the user.
        if (move == 'a') {
            opponentX--;
        } else if (move == 'd') {
            opponentX++;
        } else if (move == 'w') {
            opponentY++;
        } else if (move == 's') {
            opponentY--;
        }

        //Put the newly calculated coordinates into the HashMap in order to return it.
        newCoordinates.put('x', opponentX);
        newCoordinates.put('y', opponentY);

        //Return the coordinates back in order to use it.
        return newCoordinates;
    }
}
