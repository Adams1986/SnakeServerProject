package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import database.DatabaseWrapper;
import model.*;


/**
 * This class contains all methods that interact between the TUI / API and the data-layer in the Model package of the application.
 *
 * @author Henrik Thorn
 */
public class Logic {


    static DatabaseWrapper db = new DatabaseWrapper();

    /**
     * Get all users
     *
     * @return ArrayList of users
     */
    public static ArrayList<User> getEncryptedUsers(int userId, int userType) {

        // Define ArrayList to be used to add users and return them.
        return db.getUsers(userId, userType);
    }

    public static ArrayList<User> getUsers() {

        // Define ArrayList to be used to add users and return them.
        return db.getUsers(-1, -1);
    }

    /**
     * Create user
     *
     * @param user
     * @return true if success, false if failure
     */
    public static int createUser(User user) {

        //Email checker
        Pattern pattern = Pattern.compile("^[_a-zA-Z0-9\\.]{2,}+@[_a-zA-Z0-9\\.]{2,}\\.[_a-zA-Z0-9]{2,4}");
        Matcher matcher = pattern.matcher(user.getEmail());

        if (matcher.matches()) {

            //password checker
            pattern = Pattern.compile("^[a-zA-Z0-9æøåÆØÅ]{7,14}");
            matcher = pattern.matcher(user.getPassword());

            if (matcher.matches()) {
                user.setPassword(Security.hashing(user.getPassword()));

                if (db.createUser(user))
                    return 0;
                else
                    return 1;
            }
            else
                return 2;
        }
        else
            return 3;
    }

    /**
     * Get specific user
     *
     * @param userId
     * @return User object
     */
    public static User getUser(int userId) {
        return db.getUser(userId);
    }

    /**
     * Delete user
     *
     * @param userId
     * @return rows affected en database
     */
    public static int deleteUser(int userId) {
        return db.deleteEntry(userId, DatabaseWrapper.DELETE_USER);
    }

    /**
     * Authenticates user
     * The method uses 1 parameters: user which it authenticates as the correct credentials of an existing user.
     * Error/Success code (0 = admin trying to play) (1 = user doesn't exists), (1 = password is wrong), (2 = successful login)
     *
     * @param user
     * @return hashMap with user type, error/succes code, userid
     */
    public static int authenticateUser(User user) {

        User temp = db.getUserByUsername(user.getUsername());
        int code = 0;

        if (temp == null) {
            // User does not exists.
            code = 1;
            user.setType(-1);
        }
        else {
            if (user.getPassword().equals(temp.getPassword())) {

                if (temp.getType() != 0) {

                    code = 2;
                    user.setId(temp.getId());
                    user.setFirstName(temp.getFirstName());
                    user.setLastName(temp.getLastName());
                    user.setEmail(temp.getEmail());
                    user.setTotalScore(temp.getTotalScore());

                    //resetting password info, so it won't be sent back to user
                    user.setPassword("");
                    // Return 2 if temp exists and password is correct. Success.
                }
            }
            else {
                //Return 1 if temp exists but password is wrong.
                code = 1;
            }
            user.setType(temp.getType());
        }
        return code;
    }


    /**
     * Get specific game
     *
     * @param gameId
     * @return Game object
     */
    public static Game getGame(int gameId) {
        return db.getGame(gameId);
    }

    /**
     * Getting a speific list of games by type and userId
     *
     * @param type
     * @param userId
     * @return ArrayList<Game>
     */
    public static ArrayList<Game> getGames(int type, int userId) {
        ArrayList<Game> games = null;

        switch (type) {
            case DatabaseWrapper.GAMES_BY_ID:
                //Used for showing a user's games
                games = db.getGames(DatabaseWrapper.GAMES_BY_ID, userId);
                break;
            case DatabaseWrapper.PENDING_BY_ID:
                //Used for showing all pending games the user has as host or opponent
                games = db.getGames(DatabaseWrapper.PENDING_BY_ID, userId);
                break;
            case DatabaseWrapper.PENDING_INVITED_BY_ID:
                //Used for showing all pending games the user has been invited to play
                games = db.getGames(DatabaseWrapper.PENDING_INVITED_BY_ID, userId);
                break;
            case DatabaseWrapper.PENDING_HOSTED_BY_ID:
                //Used for showing the open games created by the user
                games = db.getGames(DatabaseWrapper.PENDING_HOSTED_BY_ID, userId);
                break;
            case DatabaseWrapper.OPEN_BY_ID:
                //Used for showing the open games created by the user
                games = db.getGames(DatabaseWrapper.OPEN_BY_ID, userId);
                break;
            case DatabaseWrapper.COMPLETED_BY_ID:
                //Shows all completed games for the user
                games = db.getGames(DatabaseWrapper.COMPLETED_BY_ID, userId);
                break;
            case DatabaseWrapper.OPEN_GAMES:
                //Used for showing all open games, when a user wants to join a game
                //Is getting set to 0 in API class because this method doesn't return games by user ID
                games = db.getGames(DatabaseWrapper.OPEN_GAMES, userId);
                break;
            case DatabaseWrapper.ALL_GAMES:
                //Used for showing all open games, when a user wants to join a game
                //Is getting set to 0 in TUI class because this method doesn't return games by user ID
                games = db.getGames(DatabaseWrapper.ALL_GAMES, userId);
                break;
            case DatabaseWrapper.PENDING_AND_OPEN_GAMES_BY_ID:
                //Used for showing both open and pending games by user id.
                games = db.getGames(DatabaseWrapper.PENDING_AND_OPEN_GAMES_BY_ID, userId);
                break;
            case DatabaseWrapper.OPEN_GAMES_BY_OTHER_USERS:
                //Used for showing open games from other users. User logged on and playing can select from this list
                //and join the game
                games = db.getGames(DatabaseWrapper.OPEN_GAMES_BY_OTHER_USERS, userId);
                break;
        }
        return games;
    }

    /**
     * Create a game
     *
     * @return returns inriched game object
     */
    public static boolean createGame(Game game) {

        if (game.getHost() != null) {
            if (db.createGame(game)) {
                if (game.getOpponent() != null) {
                    game.setStatus("pending");
                } else {
                    game.setStatus("open");
                }
                return true;
            }
        }
        return false;
    }

    public static boolean joinGame(Game game) {

        if (db.updateGame(game, DatabaseWrapper.JOIN_GAME) == 1)
            return true;
        else
            return false;
    }


    /**
     * Starting the game
     *
     * @param requestGame
     * @return Object Game
     */
    public static Game startGame(Game requestGame) {

        //gameid, opponentcontrolls
        Game game = db.getGame(requestGame.getGameId());
        ArrayList<Gamer> gamersTotalScore = db.getScore();

        for (Gamer g : gamersTotalScore) {

            if (g.getId() == game.getHost().getId()){

                game.getHost().setTotalScore(g.getTotalScore());
            }
            else if (g.getId() == game.getOpponent().getId()) {

                game.getOpponent().setTotalScore(g.getTotalScore());
            }
        }

        if(game.getOpponent() == null)
        {
            game.setOpponent(requestGame.getOpponent());
        }
        else{
            game.getOpponent().setControls(requestGame.getOpponent().getControls());

        }
        GameEngine gameEngine = new GameEngine();
        Map gamers = gameEngine.playGame(game);

        game = endGame(gamers, game);

        return game;
    }


    //endgame() Called when game is over and pushes score data to the database for future use.
    public static Game endGame(Map gamers, Game game) {

        if (((Gamer) gamers.get('h')).isWinner()) {
            game.setWinner(game.getHost());
        } else if (((Gamer) gamers.get('o')).isWinner()) {
            game.setWinner(game.getOpponent());
        }

        game.setStatus("Finished");
        db.updateGame(game, DatabaseWrapper.FINISH_GAME);
        db.createScore(game);

        return game;
    }

    /**
     * Delete game
     *
     * @param gameId
     * @return rows affected
     */
    public static int deleteGame(int gameId) {
        return db.deleteEntry(gameId, DatabaseWrapper.DELETE_GAME);
    }

    /**
     * Get all highscores from the game
     *
     * @return ArrayList of highscores
     */
    public static ArrayList<Score> getHighscore() {
        //TODO: Get all highscores
        //ArrayList<Score> highScores = db.getHighscore();

        return db.getHighscore();
    }

    public static ArrayList<Score> getHighScores(){

        return db.getHighScores();
    }


    public static ArrayList<Score> getScoresByUserID(int userId) {
        return db.getScoresByUserID(userId);
    }

}
