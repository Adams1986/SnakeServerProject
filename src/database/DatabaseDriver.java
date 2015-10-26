package database;

import model.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Team Depardieu
 *This class connects to the database - It also includes different prepared statements
 */
public class DatabaseDriver {
    /**
     * Specifies the connection to the server - Url, User and password needs to be adjusted to the individual database.
     */
    private static String sqlUrl = "jdbc:mysql://"+Config.getHost()+":"+Config.getPort()+"/"+Config.getDbname();
    private static String sqlUser = Config.getUsername();
    private static String sqlPassword = Config.getPassword();

    private Connection connection = null;

    /**
     * Connects to the database with the specified Url, User and Password.
     */
    public DatabaseDriver()
    {
        try
        {
            connection = DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Connection getConnection() {
        return connection;
    }

//    public static void checkConnection() {
//
//        try {
//            connection = DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
//
//            if (connection.isValid(1000)) {
//                System.out.println("You are connected");
//
//            } else {
//                System.out.println("Connection lost");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//    }
//
//
//
//    /**
//     * Checks if the database exists or not
//     * @return bool
//     * @throws SQLException
//     */
//
//    public static boolean DbExist() throws SQLException {
//
//        ResultSet resultSet = connection.getMetaData().getCatalogs();
//        while (resultSet.next()) {
//            String databaseName = resultSet.getString(1);
//
//            if (databaseName.equals(dbName)) {
//                return true;
//
//            } else {
//                doQuery("Indsæt SQL dump");
//            }
//        }
//
//        return false;
//    }

    /**
     * Method used to close to DB connection
     */

    public void close()
    {
        try{
            connection.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Querybuilder with two parameters, which, when specified will get a single record from a specific table.
     * @param table
     * @return SqlStatement
     */

    public String getSqlRecord(String table) {

        return "select * from " + table + " WHERE id = ?";
    }

    /**
     * Querybuilder with a single parameter, which, when specified will get a table.
     * @param table
     * @return SqlStatement
     */

    public String getSqlRecords(String table) {

        return "select * from " + table;
    }

    /**
     * Querybuilder with seven parameters, which, when specified will update the value of the shown columns in the 'users' table
     * @return SqlStatement
     */
    public String updateSqlUser(){
        return "UPDATE Users SET first_name = ?, last_name = ?, email = ?, password = ?, " +
                "status = ?, type = ? WHERE id = ?";
    }
    //
    /**
     * Querybuilder with seven parameters, which, when specified will update the value of the shown columns in the 'games' table
     * @return SqlStatement
     */
    public String updateSqlGame(){
        return "UPDATE Games SET name = ?, status = ?, winner = ?, host_controls = ?, " +
                "opponent_controls = ? WHERE id = ?";
    }

    public String createSqlUser() {
        return "Insert into users (first_name, last_name, email, username, password, status, type) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
    }

    public String createSqlGame() {
        return "Insert into games (host, opponent, name, status, host_controls) " +
                "values (?, ?, ?, ?, ?)";
    }

    public String createSqlScore() {
        return "Insert into scores (user_id, game_id, score, opponent_id) " +
                "values (?, ?, ?, ?)";
    }

    public String deleteSqlUser() {
        return "UPDATE Users SET status = ? WHERE id = ?";
    }

    public String deleteSqlGame() {
        return "UPDATE Games SET status = ? WHERE id = ?";
    }

    public String getSqlAllGamesByUserID() {
        return "select * from games where status <> 'deleted' (host = ? OR opponent = ?)";
    }

    public String getSqlPendingGamesByUserID() {
        return "select * from games WHERE status = 'pending' and (host = ? OR opponent = ?)";
    }

    public String getSqlCompletedGamesByUserID() {

        return "select * from games where status = 'completed' and (host = ? OR opponent = ?)";
    }

    public String getSqlGameInvitesByUserID() {
        return "select * from games WHERE status = 'pending' and opponent = ?";
    }

    public String authenticatedSql() {
        return "Select * from users where username = ?";
    }

    public String getSqlHighScore() {
        return "select users.*, sum(scores.score) as TotalScore from users " +
                "join scores where users.id = scores.user_id group by users.username order by TotalScore desc";
    }

    public String  getScoresByUserID() {
        return " select * from scores where user_id = ?";
    }
    public String getHighScore() {
        return "select games.id as game_id, games.created, games.opponent, games.name as game_name, scores.id as score_id, scores.user_id as user_id, max(scores.score) as highscore, users.first_name, users.last_name, users.username from scores, users, games where scores.user_id = users.id and scores.game_id = games.id group by user_id order by highscore desc";
    }

    public String getGamesByUserID() {
        return "select scores.*, games.name, users.username as oppponent_name from scores, games, users where scores.user_id = '1' and games.id = scores.game_id and scores.opponent_id = users.id";
    }




}