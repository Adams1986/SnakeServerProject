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
 * Created by: Kasper Tidemann, Henrik Thorn and Jesper Bruun Hansen
 */
public class GameEngine {

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

        hostX = (game.getMapSize()-1)/2;
        hostY = (game.getMapSize()+3)/2;
        opponentX = (game.getMapSize()+3)/2;
        opponentY = (game.getMapSize()-1)/2;
        System.out.println("hx: "+hostX +"hy: " + hostY+"ox: "+opponentX+"oy: "+opponentY);

        boolean isHostTurn;

        /*Algorithm for determining who gets the head start in the game. E.g. if host has 80 and opponent 800
        host will have about a 9-1 or 1/10 chance of getting the turn. Adding one point to both so a new user gets a
        turn once in a while. Also avoids issues with two new users with zero points (Random would not be happy about
        that).
         */
        int hostTotal = host.getTotalScore() + 1;
        int opponentTotal = opponent.getTotalScore() + 1;

        int dictatingNumber = new Random().nextInt(hostTotal + opponentTotal);

        if (hostTotal > opponentTotal) {

            isHostTurn = dictatingNumber < hostTotal;

        } else {
            isHostTurn = dictatingNumber < opponentTotal;
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
        int boundary = game.getMapSize()-1;

        int totalMovesCount = hostControls.length() + opponentControls.length();

        // Arrays of host and opponent moves:
        LinkedList<Point> hostMoves = new LinkedList<>();
        LinkedList<Point> opponentMoves = new LinkedList<>();

        //adding start positions to game
        hostMoves.add(new Point(hostX, hostY));
        opponentMoves.add(new Point(opponentX, opponentY));

        int hostCounter = 0;
        int opponentCounter = 0;

        for (int i = 0; i < totalMovesCount; i++) {

            //checks whose turn it ise
            if (isHostTurn) {

                //limits points to hostControls lenght
                if (hostCounter < hostControls.length()) {

                    //get w,s,a,d
                    char moveHost = hostControlCharacters[hostCounter];
                    //convert to a new point
                    Point newHostPoint = gameMoveHost(moveHost);

                    //tjek boundary. If opponent snake is not there, add point to list
                    if (!opponentMoves.contains(newHostPoint)) {

                        hostMoves.add(newHostPoint);
                        hostScore++;

                    }
//                    if (newHostPoint.x > boundary || newHostPoint.x < boundary || newHostPoint.y > boundary || newHostPoint.y < boundary) {
                    else if (newHostPoint.x > boundary || newHostPoint.y > boundary) {

                        hostDidCrash = true;
                    }
                    else if (newHostPoint.x < boundary || newHostPoint.y < boundary){

                        hostDidCrash = true;
                    }
                    //else host crashes into other gamer
                    else {

                        opponentKills++;
                        hostDidCrash = true;
                    }

                    //increment counter and set turn to opponent
                    hostCounter++;
                }
            }
            //checking turn. If opponent
            else {

                if (opponentCounter < opponentControls.length()) {

                    char moveOpponent = opponentControlCharacters[opponentCounter];
                    Point newOpponentPoint = gameMoveOpponent(moveOpponent);

                    //add point if not true
                    if (!hostMoves.contains(newOpponentPoint)) {

                        opponentMoves.add(newOpponentPoint);
                        opponentScore++;
                    }
                    else if (newOpponentPoint.x > boundary || newOpponentPoint.y > boundary) {
//                    if (newOpponentPoint.x > boundary || newOpponentPoint.x < boundary || newOpponentPoint.y > boundary || newOpponentPoint.y < boundary) {

                        opponentDidCrash = true;
                    }
                    else if (newOpponentPoint.x < boundary || newOpponentPoint.y < boundary){

                        opponentDidCrash = true;
                    }
                    else {

                        hostKills++;
                        opponentDidCrash = true;
                    }

                    opponentCounter++;
                }
            }
            if (!opponentDidCrash && !hostDidCrash){

                isHostTurn = !isHostTurn;
            }
            else if (hostDidCrash){

                isHostTurn = false;
            }
            else
                isHostTurn = true;
        }

        // Set Score and Kill for the Gamer object for the host user.
        host.setScore(hostScore);
        host.setKills(hostKills);

        // Set Score and Kill for the Gamer object for the opponent user.
        opponent.setScore(opponentScore);
        opponent.setKills(opponentKills);

        // Set Winner for gamer object for the winning user.
        // If the game is draw, both object will be loser.
        if (hostScore > opponentScore) {
            host.setWinner(true);
            opponent.setControls(opponentControls.substring(0, opponentMoves.size()));
        }
        if (hostScore < opponentScore) {
            opponent.setWinner(true);
            host.setControls(hostControls.substring(0, hostMoves.size()));
        }
        gamers.put('h', host);
        gamers.put('o', opponent);

        // Return the gamers back to the logic layer, who will now have access to see score, winner etc.
        return gamers;


    }


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


