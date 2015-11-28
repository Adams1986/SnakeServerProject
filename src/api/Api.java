package api;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import controller.Logic;
import controller.Security;
import database.DatabaseWrapper;
import model.Game;
import model.Score;
import model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/api")
public class Api {

    //TODO: rewrite this! does not need the 'huge' switch. Default is enough
    @POST //"POST-request" er ny data vi kan indtaste for at logge ind.
    @Path("/login/")
    @Produces("application/json")
    public Response login(String data) {

        int statusCode;
        HashMap<String, String> dataMap = new HashMap<>();

        try {
            //Decrypt user
            User user = Logic.getDecryptedUser(data);
            user.setPassword(Security.hashing(user.getPassword()));

            HashMap<String, Integer> hashMap = Logic.authenticateUser(user.getUsername(), user.getPassword());

            switch (hashMap.get("code")) {
                case 0:
                    statusCode = 400;
                    dataMap.put("message", "Get back to work!");
                    break;
                case 1:
                    statusCode = 400;
                    dataMap.put("message", "Wrong username or password");
                    break;

                case 2:
                    statusCode = 200;
                    dataMap.put("message", "Login successful");
                    dataMap.put("data", hashMap.get("userid") + "");
                    break;

                default:
                    statusCode = 500;
                    dataMap.put("message", "Unknown error. Please contact Henrik Thorn at: henrik@itkartellet.dk");
                    break;
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {

            statusCode = 400;
            dataMap.put("message", "Error in JSON");
        }

        return Response
                .status(statusCode)
                .entity(new Gson().toJson(dataMap))
                .header("Access-Control-Allow-Headers", "*")
                .build();

    }

    @GET //"GET-request"
    @Path("/users/{userid}") //USER-path - identifice det inden for metoden
    @Produces("application/json")
    public Response getAllUsers(@PathParam("userid") int userId) {

        //TODO change maybe?
        //ArrayList<User> users = Logic.getUsers();

        return Response
                .status(200)
                .entity(Logic.getEncryptedUsers(userId))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }


    @DELETE //DELETE-request fjernelse af data (bruger): Slet bruger
    @Path("/users/{userid}")
    @Produces("application/json")
    public Response deleteUser(@PathParam("userid") int userId) {

        int deleteUser = Logic.deleteUser(userId);
        int status;
        HashMap<String, String> dataMap = new HashMap<>();

        if (deleteUser == 1) {
            status = 200;
            dataMap.put("message", "User was deleted");

        } else {
            status = 400;
            dataMap.put("message", "Failed. User was not deleted");
        }
        return Response
                .status(status)
                .entity(new Gson().toJson(dataMap))
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @POST //POST-request: Ny data der skal til serveren; En ny bruger oprettes
    @Path("/users/")
    @Produces("application/json")
    public Response createUser(String data) {

        User user;
        HashMap<String, String> dataMap = new HashMap<>();
        int statusCode;

        try {
            user = Logic.getDecryptedUser(data);
            //set type to 1 so will always create a user and not an admin
            user.setType(1);

            int createdUser = Logic.createUser(user);

            switch (createdUser) {

                case 0:
                    statusCode = 200;
                    dataMap.put("message", "User was created");
                    break;

                case 1:
                    statusCode = 400;
                    dataMap.put("message", "Username or email already exists");
                    break;

                case 2:
                    statusCode = 400;
                    dataMap.put("message", "Password is not valid. Must be 7 to 14 characters long, " +
                            "consisting only of numbers and letters.");
                    break;

                case 3:
                    statusCode = 400;
                    dataMap.put("message", "Invalid email. Must contain @ and .com/.dk/.net or equivalent");
                    break;

                default:
                    statusCode = 400;
                    dataMap.put("message", "Something went wrong");
                    break;
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {
            statusCode = 400;
            dataMap.put("message", "Error in JSON");
        }

        return Response
                .status(statusCode)
                .entity(new Gson().toJson(dataMap))
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @GET //"GET-request"
    @Path("/users/id/{userId}")
    @Produces("application/json")
    // JSON: {"userId": [userid]}
    public Response getUser(@PathParam("userId") int userId) {

        int statusCode;
        String sendingToClient;
        User user = Logic.getUser(userId);
        //udprint/hent/identificer af data omkring spillere
        if (user != null) {
            statusCode = 200;
            sendingToClient = Logic.getEncryptedDto(user);
        }
        else {
            statusCode = 400;
            sendingToClient = "{\"message\":\"User was not found\"}";
        }
        return Response
                .status(statusCode)
                .entity(sendingToClient)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST //POST-request: Nyt data; nyt spil oprettes
    @Path("/games/")
    @Produces("application/json")
    public Response createGame(String json) {

        int statusCode;
        String sendingToClient;

        try {
            boolean createdGame = Logic.createGame(new Gson().fromJson(json, Game.class));

            if (createdGame) {

                statusCode = 201;
                sendingToClient = "{\"message\":\"Your game was created\"}";
            }
            else {
                statusCode = 400;
                sendingToClient = "{\"message\":\"Something went wrong\"}";
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            statusCode = 400;
            sendingToClient = "{\"message\":\"Error in JSON\"}";
        }
        return Response
                .status(statusCode)
                .entity(sendingToClient)
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @PUT
    @Path("/games/join/")
    @Produces("application/json")
    public Response joinGame(String json) {

        String sendingToClient;
        int statusCode;

        try {
            Game game = new Gson().fromJson(json, Game.class);

            if (Logic.joinGame(game)) {
                statusCode = 201;
                sendingToClient = "{\"message\":\"Game was joined\"}";
            }
            else {
                statusCode = 400;
                sendingToClient = "{\"message\":\"Game closed\"}";
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            statusCode = 400;
            sendingToClient = "{\"message\":\"Error in JSON\"}";
        }

        return Response
                .status(statusCode)
                .entity(sendingToClient)
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }


    @PUT
    @Path("/games/start/")
    @Produces("application/json")
    public Response startGame(String json) {

        int statusCode;
        String sendingToClient;

        try {
            Game game = Logic.startGame(new Gson().fromJson(json, Game.class));

            if (game != null) {
                statusCode = 201;
                sendingToClient = new Gson().toJson(game);
            }
            else {
                statusCode = 400;
                sendingToClient = "{\"message\":\"Something went wrong\"}";
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            statusCode = 400;
            sendingToClient = "{\"message\":\"Error in JSON\"}";
        }
        return Response
                .status(statusCode)
                .entity(sendingToClient)
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @DELETE //DELETE-request fjernelse af data(spillet slettes)
    @Path("/games/{gameid}")
    @Produces("application/json")
    public Response deleteGame(@PathParam("gameid") int gameId) {

        int deleteGame = Logic.deleteGame(gameId);
        int statusCode;
        String sendingToClient;

        if (deleteGame == 1) {
            statusCode = 200;
            sendingToClient = "{\"message\":\"Game was deleted\"}";
        }
        else {
            statusCode = 400;
            sendingToClient = "{\"message\":\"Failed. Game was not deleted\"}";
        }
        return Response
                .status(statusCode)
                .entity(sendingToClient)
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @GET //"GET-request"
    @Path("/game/{gameid}")
    @Produces("application/json")
    public Response getGame(@PathParam("gameid") int gameid) {

        Game game = Logic.getGame(gameid);

        return Response
                .status(200)
                .entity(new Gson().toJson(game))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @GET //"GET-request"
    @Path("/scores/")
    @Produces("application/json")
    public Response getHighscore() {

        return Response
                .status(200)
                .entity(new Gson().toJson(Logic.getHighscore()))
                .header("Access-Control-Allow-Origin", "*")
                .build();

    }

    @GET
    @Path("/highscores")
    @Produces("application/json")
    public Response getHighScores(){

        ArrayList<Score> highScores = Logic.getHighScores();

        return Response
                .status(200)
                .entity(new Gson().toJson(highScores))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /*
    Getting games by userid
     */
    @GET //"GET-request"
    @Path("/games/{userid}/")
    @Produces("application/json")
    public Response getGamesByUserID(@PathParam("userid") int userId) {

        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.GAMES_BY_ID, userId);

        return Response
                .status(200)
                .entity(new Gson().toJson(games))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /*
    Getting games by game status and user id
     */
    @GET //"GET-request"
    @Path("/games/{status}/{userid}")
    @Produces("application/json")
    public Response getGamesByStatusAndUserID(@PathParam("status") String status, @PathParam("userid") int userId) {

        ArrayList<Game> games = null;

        switch (status) {
            case "Pending_games":
                games = Logic.getGames(DatabaseWrapper.PENDING_BY_ID, userId);
                break;
            case "My_open_games":
                games = Logic.getGames(DatabaseWrapper.OPEN_BY_ID, userId);
                break;
            case "Finished_games":
                games = Logic.getGames(DatabaseWrapper.COMPLETED_BY_ID, userId);
                break;
            case "Open_and_pending":
                games = Logic.getGames(DatabaseWrapper.PENDING_AND_OPEN_GAMES_BY_ID, userId);
                break;
            case "Open_challenges":
                games = Logic.getGames(DatabaseWrapper.OPEN_GAMES_BY_OTHER_USERS, userId);
                break;
            case "Invited_games":
                games = Logic.getGames(DatabaseWrapper.PENDING_INVITED_BY_ID, userId);
                break;
            case "Hosted_games":
                games = Logic.getGames(DatabaseWrapper.PENDING_HOSTED_BY_ID, userId);
                break;
            case "All_open_games":
                games = Logic.getGames(DatabaseWrapper.OPEN_GAMES, 0);
                break;
        }

        return Response
                .status(201)
                .entity(new Gson().toJson(games))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }


    /*
    Getting all scores by user id
    Used for showing all finished games and scores for the user
     */
    @GET //"GET-request"
    @Path("/scores/{userid}")
    @Produces("application/json")
    public Response getScoresByUserID(@PathParam("userid") int userid) {

        ArrayList<Score> score = Logic.getScoresByUserID(userid);

        return Response
                .status(201)
                .entity(new Gson().toJson(score))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}