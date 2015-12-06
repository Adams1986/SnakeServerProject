package api;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonSyntaxException;
import controller.DataParser;
import controller.JWTProvider;
import controller.Logic;
import controller.Security;
import database.DatabaseWrapper;
import io.jsonwebtoken.*;
import model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/api")
public class Api {

    @POST //"POST-request" er ny data vi kan indtaste for at logge ind.
    @Path("/login/")
    @Produces("application/json")
    public Response login(String data) {

        int statusCode;
        HashMap<String, String> dataMap = new HashMap<>();
        String jwtToken = "";

        try {
            //Decrypt user
            User user = DataParser.getDecryptedUser(data);
            user.setPassword(Security.hashing(user.getPassword()));

            int code = Logic.authenticateUser(user);

            switch (code) {
                case 0:
                    statusCode = 400;
                    dataMap.put("message", "Nice try Admin. Get back to work!");
                    break;
                case 1:
                    statusCode = 400;
                    dataMap.put("message", "Wrong username or password");
                    break;

                case 2:
                    statusCode = 200;
                    dataMap.put("message", "Login successful");
                    dataMap.put("data", DataParser.getEncryptedDto(user));
                    //if exists, create a new token for user.
                    String subject = user.getEmail()+"---"+user.getId();
                    jwtToken = JWTProvider.createToken(subject);
                    break;

                default:
                    statusCode = 500;
                    dataMap.put("message", "Unknown error. Please contact Henrik Thorn at: henrik@itkartellet.dk");
                    break;
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            statusCode = 400;
            dataMap.put("message", "Error in JSON");
        }

        return Response
                .status(statusCode)
                .entity(DataParser.hashMapToJson(dataMap))
                .header("authorization", jwtToken)
                .header("Access-Control-Allow-Headers", "*")
                .build();

    }

    @GET //"GET-request"
    @Path("/users/") //USER-path - identifice det inden for metoden
    @Produces("application/json")
    public Response getAllUsers(@HeaderParam("authorization") String authorization, @HeaderParam("headerData")String headerData) {

        int statusCode;

        ArrayList<User> users = null;

        JWTProvider.validateToken(authorization);

        try {
            statusCode = 200;
            users = Logic.getEncryptedUsers(Integer.parseInt(headerData), 1);

            return Response
                    .status(statusCode)
                    .entity(DataParser.getEncryptedUserList(users))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        }
        catch (InvalidClaimException | SignatureException | MalformedJwtException e) {
            e.printStackTrace();

            return Response
                    .status(401)
                    .entity("{\"message\":\"failure\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }


    @DELETE //DELETE-request fjernelse af data (bruger): Slet bruger
    @Path("/users/")
    @Produces("application/json")
    public Response deleteUser(@HeaderParam("authorization") String authorization, @HeaderParam("headerData")String headerData) {

        String message = JWTProvider.validateToken(authorization);

        int status;

        HashMap<String, String> dataMap = null;

            int deleteUser = Logic.deleteUser(Integer.parseInt(headerData));

            dataMap = new HashMap<>();

            if (deleteUser == 1) {
                status = 200;
                dataMap.put("message", "User was deleted");

            } else {
                status = 400;
                dataMap.put("message", "Failed. User was not deleted");
            }

        return Response
                .status(status)
                .entity(DataParser.hashMapToJson(dataMap))
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
            user = DataParser.getDecryptedUser(data);
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
                .entity(DataParser.hashMapToJson(dataMap))
                .header("Access-Control-Allow-Headers", "*")
                .build();
    }

    @GET //"GET-request"
    @Path("/users/id/")
    @Produces("application/json")
    // JSON: {"userId": [userid]}
    public Response getUser(@HeaderParam("authorization") String authorization, @HeaderParam("headerData")String headerData) {

        JWTProvider.validateToken(authorization);

        int statusCode;
        String sendingToClient;
        User user = null;


            user = Logic.getUser(Integer.parseInt(headerData));
            //udprint/hent/identificer af data omkring spillere
            if (user != null) {
                statusCode = 200;
                sendingToClient = DataParser.getEncryptedDto(user);
            } else {
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
    public Response createGame(@HeaderParam("authorization") String authorization, String json) {

        String validation = JWTProvider.validateToken(authorization);

        int statusCode;
        String sendingToClient;

        try {
            boolean createdGame = Logic.createGame(DataParser.getDecryptedGame(json));

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
    public Response joinGame(@HeaderParam("authorization") String authorization, String json) {

        JWTProvider.validateToken(authorization);

        String sendingToClient;
        int statusCode;

        try {
            Game game = DataParser.getDecryptedGame(json);

            if (Logic.joinGame(game)) {
                statusCode = 201;
                sendingToClient = "{\"message\":\"Game was joined\"}";
            }
            else {
                statusCode = 400;
                sendingToClient = "{\"message\":\"You can't join this game. Game is closed\"}";
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
    public Response startGame(@HeaderParam("authorization") String authorization, String json) {

        JWTProvider.validateToken(authorization);

        int statusCode;
        String sendingToClient;

        try {
            Game game = Logic.startGame(DataParser.getDecryptedGame(json));

            if (game != null) {
                statusCode = 201;
                sendingToClient = "{\"message\":\"Game was played. See results in the replayer\"}";
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
    @Path("/games/")
    @Produces("application/json")
    public Response deleteGame(@HeaderParam("authorization") String authorization, @HeaderParam("headerData")String headerData) {

        String validation = JWTProvider.validateToken(authorization);

        //returns response with status 401 - Unauthorized if token has expired. New token is needed to access deletion of games
        if (!validation.equals("{\"message\": \"Valid token\"}")){

            return Response
                    .status(401)
                    .entity(validation)
                    .header("Access-Control-Allow-Headers", "*")
                    .build();
        }

        int deleteGame = Logic.deleteGame(Integer.parseInt(headerData));
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
    @Path("/game/")
    @Produces("application/json")
    public Response getGame(@HeaderParam("authorization") String authorization, @HeaderParam("headerData") String headerData) {

        JWTProvider.validateToken(authorization);

        Game game = Logic.getGame(Integer.parseInt(headerData));

        return Response
                .status(200)
                .entity(DataParser.getEncryptedDto(game))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @GET //"GET-request"
    @Path("/scores/")
    @Produces("application/json")
    public Response getHighscore(@HeaderParam("authorization") String authorization) {

        JWTProvider.validateToken(authorization);

        return Response
                .status(200)
                .entity(DataParser.getEncryptedScoreList(Logic.getHighscore()))
                .header("Access-Control-Allow-Origin", "*")
                .build();

    }

    @GET
    @Path("/highscores")
    @Produces("application/json")
    public Response getHighScores(@HeaderParam("authorization") String authorization){

        JWTProvider.validateToken(authorization);

        ArrayList<Score> highScores = Logic.getHighScores();

        return Response
                .status(200)
                .entity(DataParser.getEncryptedScoreList(highScores))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /*
    Getting games by userid
     */
    @GET //"GET-request"
    @Path("/games/userid/")
    @Produces("application/json")
    public Response getGamesByUserID(@HeaderParam("authorization") String authorization) {

        JWTProvider.validateToken(authorization);

        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.GAMES_BY_ID, Integer.parseInt(authorization));

        return Response
                .status(200)
                .entity(DataParser.getEncryptedGameList(games))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /*
    Getting games by game status and user id
     */
    @GET //"GET-request"
    @Path("/games/")
    @Produces("application/json")
    public Response getGamesByStatusAndUserID(@HeaderParam("authorization") String authorization, @HeaderParam("headerData") String headerData) {

        JWTProvider.validateToken(authorization);

        ArrayList<Game> games = null;
        int userId = DataParser.parseHeaderToInteger(headerData);
        String gameStatus = DataParser.parseHeaderToString(headerData);


        switch (gameStatus) {
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
                .status(200)
                .entity(DataParser.getEncryptedGameList(games))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }


    /*
    Getting all scores by user id
    Used for showing all finished games and scores for the user
     */
    @GET //"GET-request"
    @Path("/scores/userid/")
    @Produces("application/json")
    public Response getScoresByUserID(@HeaderParam("authorization") String authorization) {

        JWTProvider.validateToken(authorization);

        ArrayList<Score> score = Logic.getScoresByUserID(Integer.parseInt(authorization));

        return Response
                .status(200)
                .entity(DataParser.getEncryptedScoreList(score))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}