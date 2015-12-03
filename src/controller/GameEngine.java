package controller;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import model.Game;
import model.Gamer;

/**
 * Class to determine who wins the game and all functions that are relevant in that sense.
 * Created by: Kasper Tidemann, Henrik Thorn and Jesper Bruun Hansen with a special mention for SIMON ADAMS who worked
 * on fixing collision issues.
 */
public class GameEngine {

    /*instance variables instead of local variables. Cannot pass local variables as params into a method and use again
     with updated values. Changes in method call stays in method call.
      */
    private int hostX;
    private int hostY;
    private int opponentX;
    private int opponentY;


    public Map playGame(Game game) {

        Gamer host = game.getHost();
        Gamer opponent = game.getOpponent();
        Map gamers = new HashMap();
        String hostControls = host.getControls();
        String opponentControls = opponent.getControls();

        //determining start position for host and opponent, depending on map size.
        hostX = (game.getMapSize()-1)/2;
        hostY = (game.getMapSize()+3)/2;
        opponentX = (game.getMapSize()+3)/2;
        opponentY = (game.getMapSize()-1)/2;

        boolean isHostTurn;

        /*Algorithm for determining who gets the head start in the game. E.g. if host has 80 and opponent 800
        host will have about a 9-1 or 1/10 chance of getting the turn. Adding one point to both so a new user gets a
        turn once in a while. Also avoids issues with two new users with zero points (Random would not be happy about
        that).
         */
        int hostTotal = host.getTotalScore() + 1;
        int opponentTotal = opponent.getTotalScore() + 1;

        //generate a set of numbers up to total points of host and opponent
        int turnDictator = new Random().nextInt(hostTotal + opponentTotal);

        /*if host is the most awesome, he gets to act first every time random number (turnDictator)
        is less than his total score
         */
        if (hostTotal > opponentTotal) {

            isHostTurn = turnDictator < hostTotal;
        }
        //if opponent is better, he gets the first move if turnDictator is less than his total
        else {

            isHostTurn = turnDictator < opponentTotal;
        }
        // Split each controls string into a character array:
        char[] hostControlCharacters = hostControls.toCharArray();
        char[] opponentControlCharacters = opponentControls.toCharArray();

        // The game variables for the host:
        boolean hostDidCrash = false;
        int hostScore = 0;
        int hostKills = 0;

        // The game variables for the opponent:
        boolean opponentDidCrash = false;
        int opponentScore = 0;
        int opponentKills = 0;

        // Playing field options:
        int boundary = game.getMapSize();

        int hostMovesNumber = hostControls.length();
        int opponentMovesNumber = opponentControls.length();
        int totalMovesNumber = hostMovesNumber+opponentMovesNumber;

        // Arrays of host and opponent moves:
        LinkedList<Point> hostMoves = new LinkedList<>();
        LinkedList<Point> opponentMoves = new LinkedList<>();

        //adding start positions to game
        hostMoves.add(new Point(hostX, hostY));
        opponentMoves.add(new Point(opponentX, opponentY));

        int hostCounter = 0;
        int opponentCounter = 0;

        //game-loop. Runs loop the total of both gamers control length
        for (int i = 0; i < totalMovesNumber; i++) {

            //checks whose turn it ise
            if (isHostTurn) {

                /*limits iterations through the host's controls to his total moves. Both important for points and exceptions.
                !hostCrash
                 */
                if (hostCounter < hostMovesNumber && !hostDidCrash) {

                    //get w,s,a,d
                    char moveHost = hostControlCharacters[hostCounter];
                    //convert to a new point
                    Point newHostPoint = gameMoveHost(moveHost);

                    //checking hosts x coordinates are inside boundary
                    if (newHostPoint.x > boundary || newHostPoint.x < 1) {

                        hostDidCrash = true;
                    }
                    //checking hosts y coordinates are inside boundary
                    else if (newHostPoint.y > boundary || newHostPoint.y < 1){

                        hostDidCrash = true;
                    }
                    else if (hostMoves.contains(newHostPoint)){

                        hostDidCrash = true;
                    }
                    //check boundary. If opponent snake is not there, add point to list
                    else if (!opponentMoves.contains(newHostPoint) || opponentDidCrash) {

                        hostMoves.add(newHostPoint);
                        hostScore++;
                    }
                    //else host crashes into other gamer
                    else {
                        /*checking if opponent has crashed. So if opponentMoves contains the new point & he hasn't crashed
                        only then does host crash
                         */
                        if (!opponentDidCrash) {
                            opponentKills++;
                            hostDidCrash = true;
                        }
                    }
                    //increment counter and set turn to opponent
                    hostCounter++;
                }
            }
            //checking turn.
            else {
                //Identical for opponent as the above for host
                if (opponentCounter < opponentMovesNumber && !opponentDidCrash) {

                    char moveOpponent = opponentControlCharacters[opponentCounter];
                    Point newOpponentPoint = gameMoveOpponent(moveOpponent);

                    if (newOpponentPoint.x > boundary || newOpponentPoint.x < 1) {

                        opponentDidCrash = true;
                    }
                    else if (newOpponentPoint.y > boundary || newOpponentPoint.y < 1){

                        opponentDidCrash = true;
                    }
                    else if(opponentMoves.contains(newOpponentPoint)){

                        opponentDidCrash = true;
                    }
                    //add point if not true
                    else if (!hostMoves.contains(newOpponentPoint) || hostDidCrash) {

                        opponentMoves.add(newOpponentPoint);
                        opponentScore++;
                    }
                    else {

                        if (!hostDidCrash) {
                            hostKills++;
                            opponentDidCrash = true;
                        }
                    }
                    opponentCounter++;
                }
            }
            //change turn if no one crashed
            if (!opponentDidCrash && !hostDidCrash){

                isHostTurn = !isHostTurn;
            }
            //if host has died make sure it is never host turn again
            else if (hostDidCrash){

                isHostTurn = false;
            }
            else
                isHostTurn = true;
        }
        //end of game-loop

        //adding multipliers - otherwise huge advantage to players always playing large maps.
        //add multiplier for small maps
        if (boundary == 9){

            hostScore = hostScore * 5;
            opponentScore = opponentScore * 5;
        }
        //add multiplier for medium maps
        else if (boundary == 15){

            hostScore = hostScore * 3/2;
            opponentScore = opponentScore * 3/2;
        }

        // Set Score and Kill for the Gamer object for the host user.
        host.setScore(hostScore);
        host.setKills(hostKills);

        // Set Score and Kill for the Gamer object for the opponent user.
        opponent.setScore(opponentScore);
        opponent.setKills(opponentKills);

        // Set Winner for gamer object for the winning user.
        // If the score is the same no winner is set and it is a draw.
        if (hostScore > opponentScore) {
            host.setWinner(true);
        }
        else if (opponentScore > hostScore){
            opponent.setWinner(true);
        }

        host.setControls(hostControls.substring(0, hostMoves.size()-1));
        opponent.setControls(opponentControls.substring(0, opponentMoves.size()-1));

        gamers.put('h', host);
        gamers.put('o', opponent);

        // Return the gamers back to the logic layer, who will now have access to see score, winner etc.
        return gamers;
    }

    /**
     * Method used to move the host snake.
     * @param move
     * @returns a new Point with updated x,y coordinates
     */
    private Point gameMoveHost(char move) {

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
        return new Point(hostX, hostY);
    }

    /**
     * Method for moving opponent snake
     * @param move
     * @returns a new Point with updated x,y coordinates
     */
    private Point gameMoveOpponent(char move) {

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
        return new Point(opponentX, opponentY);
    }
}


