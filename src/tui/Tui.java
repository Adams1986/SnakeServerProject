package tui;

import java.util.*;

import controller.Logic;
import controller.Security;
import database.DatabaseWrapper;
import model.Game;
import model.Gamer;
import model.User;

public class Tui {

    private static Scanner input = new Scanner(System.in);
    private static boolean adminIsAuthenticated = false;

    public static void serverMenu(){
        boolean serverRunning = true;
        while (serverRunning) {

            Tui.miscOut("\n***Welcome to the Snake server***\n");
            Tui.miscOut("What do you want to do?");
            Tui.miscOut("1) Login as admin");
            Tui.miscOut("2) Stop server");

            switch (input.nextInt()) {
                case 1:
                    login();
                    break;
                case 2:
                    serverRunning = false;

                    break;
                default:
                    Tui.miscOut("Unassigned key.");
            }
        }
    }

    public static void login() {
        miscOut("Please log in.");

        HashMap <String, Integer> hashMap = Logic.authenticateUser(enterUsername(), Security.hashing(enterPassword()));

        int code = hashMap.get("code");
        if (code == 2)
            miscOut("Admin does not exist.");
        else if (code == 1) {
            miscOut("Wrong password.");
        } else if (code == 0) {
            miscOut("Success.");
            adminIsAuthenticated = true;
            userMenu();
        }

    }

    public static void userMenu() {

        while (adminIsAuthenticated) {

            int menu = userMenuScreen();

            switch (menu) {
                case 1:
                    miscOut("Game List: ");
                    ArrayList<Game> gameList = Logic.getGames(DatabaseWrapper.ALL_GAMES, 0);
                    listGames(gameList);
                    break;
                case 2:
                    miscOut("User List: ");
                    ArrayList<Gamer> userList = Logic.getUsers();
                    listUsers(userList);
                    break;
                case 3:
                    miscOut("Create User: ");
                    createUser();
                    break;
                case 4:
                    miscOut("Delete User: ");
                    Logic.deleteUser(deleteUserScreen());
                    break;
                case 5:
                    miscOut("You Logged Out.");
                    adminIsAuthenticated = false;
                    break;
                default:
                    miscOut("Unassigned key.");
                    break;

            }
        }
    }

    public static int userMenuScreen() {

        System.out.println("Please make a choice");
        System.out.println("\n1: List all games");
        System.out.println("2: List all users");
        System.out.println("3: Create user");
        System.out.println("4: Delete user");
        System.out.println("5: Log out");

        return input.nextInt();
    }

    public static void listUsers(ArrayList<Gamer> userList) {

        for (User user : userList) {
            System.out.println("Id: " + user.getId() + "\tUser: " + user.getUsername() + "\tStatus: " + user.getStatus());
        }
    }

    public static void listGames(ArrayList<Game> gameList) {

        for (Game game : gameList) {

            System.out.println("GameId: " + game.getGameId() + "\tHostId: " + game.getHost().getId() + "\tOpponentId: "
                    + game.getOpponent().getId() + "\tWinner: " + game.getWinner().getId());
        }
    }

    public static void createUser() {

        User usr = new User();
        usr.setFirstName(enterFirstName());
        usr.setLastName(enterLastName());
        usr.setEmail(enterEmail());
        usr.setUsername(enterUsername());
        usr.setPassword(enterPassword());
        usr.setType(enterUserType());

        int succesCode = Logic.createUser(usr);

        switch (succesCode){
            case 0:
                System.out.println("User was created");
                break;
            /*setting up for possible extra info for admin. For now assume admin knows standards for creating a user,
            e.g. requirements for password, email etc
             */
            case 1:
            case 2:
            case 3:
                System.out.println("User was not created");
                break;
        }
    }


    public static int deleteUserScreen() {
        listUsers(Logic.getUsers());
        System.out.print("Type id on the user you wish to delete: ");

        return input.nextInt();
    }

    public static String enterUsername() {

        System.out.print("Please enter username: "); // Brugeren bliver spurgt om username

        return input.next();
    }

    public static String enterPassword() {

        System.out.print("Please enter password: "); // Brugeren bliver spurgt om password

        return input.next();
    }

    public static String enterFirstName() {
        System.out.print("Please enter first name: "); // Brugeren bliver spurgt om fornavn

        return input.next();
    }

    public static String enterLastName() {
        System.out.print("Please enter last name: "); // Brugeren bliver spurgt om efternavn

        return input.next();
    }

    public static String enterEmail() {
        System.out.print("Please enter email: "); // Brugeren bliver spurgt om email

        return input.next();
    }

    public static int enterUserType() {
        boolean userTypeApproved = false;
        System.out.print("Please enter user type (0 or 1) \n0) Admin\n1) User\n");
        int userType = input.nextInt();

        do {
            if (userType != 0 && userType != 1)
                System.out.println("Type must be either admin or user. P try again");
            else{
                userTypeApproved = true;
            }


        } while (!userTypeApproved);

        return userType;
    }

    public static void miscOut(String s) {
        System.out.println(s);
    }
}