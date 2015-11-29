package controller;

import com.google.gson.Gson;
import model.Config;
import model.Gamer;
import model.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ADI on 29-11-2015.
 */
public class DataParser {

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

        //return the decrypted value as a user object, using the gson method fromJson
        return gson.fromJson(jsonUser, User.class);

    }

    public static String getEncryptedListOfDto(ArrayList<Gamer> list){

        HashMap<String, String> encryptedList = new HashMap<>();
        encryptedList.put("data", Security.encrypt(new Gson().toJson(list), Config.getEncryptionkey()));

        return new Gson().toJson(encryptedList);
    }
}
