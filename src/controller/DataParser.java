package controller;

import com.google.gson.Gson;
import controller.Security;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ADI on 29-11-2015.
 */
public class DataParser {

    public static String parseHeaderToString(String dataToBeParsed){

        String [] values = dataToBeParsed.split("---");

        return values[0];
    }

    public static int parseHeaderToInteger(String dataToBeParsed){

        String [] values = dataToBeParsed.split("---");

        return Integer.valueOf(values[1]);
    }

    public static String decryptMessage(String dataToBeEncrypted){

        return Security.decrypt(dataToBeEncrypted, Config.getEncryptionkey());
    }

    //TODO: find suitable place. Maybe make a parser class like on client
    public static String getEncryptedDto(Object o){

        HashMap<String, String> encryptedDto = new HashMap<>();
        encryptedDto.put("data", Security.encrypt(new Gson().toJson(o), Config.getEncryptionkey()));

        return new Gson().toJson(encryptedDto);
    }

    public static User getDecryptedUser(String jsonData){

        //creating the gson-parser
        Gson gson = new Gson();

        //use parser to parse json data into a hash map
        HashMap<String, String> jsonHashMap = gson.fromJson(jsonData, HashMap.class);

        //get the value from the key "data" in the hash map
        String encryptedUser = jsonHashMap.get("data");

        //decrypt the value inside "data" key, using the Security.decrypt method
        String jsonUser = Security.decrypt(encryptedUser, Config.getEncryptionkey());
        System.out.println(jsonUser);
        //return the decrypted value as a user object, using the gson method fromJson
        return gson.fromJson(jsonUser, User.class);

    }

    public static String getEncryptedUserList(ArrayList<User> list){

        HashMap<String, String> encryptedList = new HashMap<>();
        encryptedList.put("data", Security.encrypt(new Gson().toJson(list), Config.getEncryptionkey()));

        return new Gson().toJson(encryptedList);
    }

    public static String getEncryptedGameList(ArrayList<Game> list){

        HashMap<String, String> encryptedList = new HashMap<>();
        encryptedList.put("data", Security.encrypt(new Gson().toJson(list), Config.getEncryptionkey()));

        return new Gson().toJson(encryptedList);
    }

    public static String getEncryptedScoreList(ArrayList<Score> list){

        HashMap<String, String> encryptedList = new HashMap<>();
        encryptedList.put("data", Security.encrypt(new Gson().toJson(list), Config.getEncryptionkey()));

        return new Gson().toJson(encryptedList);
    }

    public static String getEncryptedGame(Game game) {

//        HashMap<String, String> encryptedDto = new HashMap<>();
//        String encryptedGame = Security.encrypt(new Gson().toJson(game), Config.getEncryptionkey());
//        encryptedDto.put("data", encryptedGame);
//
//        return new Gson().toJson(encryptedDto);
        return new Gson().toJson(game);
    }

    public static Game getDecryptedGame(String jsonData){

        Gson gson = new Gson();
        HashMap<String, String> jsonHashMap = gson.fromJson(jsonData, HashMap.class);
        String encryptedUser = jsonHashMap.get("data");
        String jsonUser = Security.decrypt(encryptedUser, Config.getEncryptionkey());

        return gson.fromJson(jsonUser, Game.class);

    }

    public static String hashMapToJson(HashMap<String, String> dataMap) {

        return new Gson().toJson(dataMap);
    }
}
