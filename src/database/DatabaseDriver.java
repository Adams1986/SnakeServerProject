package database;

import model.Config;

import java.sql.Connection;
import java.sql.DriverManager;
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

    //used for switch in updateGame
    public static final int JOIN = 0;
    public static final int FINISHED = 1;

    /**
     * Connects to the database with the specified Url, User and Password.
     */
    public DatabaseDriver()
    {
        try {

            connection = DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
        }
        catch (SQLException e) {

            e.printStackTrace();
            System.exit(1);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Method used to close to DB connection
     */
    public void close() {

        try{
            connection.close();
        }
        catch(SQLException e) {

            e.printStackTrace();
        }
    }

    /**
     * Querybuilder with two parameters, which, when specified will get a single record from a specific table.
     * @return SqlStatement
     */
    public String getSqlRecord(String table) {

        //Possible threat of SQL injection
        return String.format("select * from %s WHERE id = ? AND status <> 'deleted'", table);
    }

    public String getSqlRecordWithoutCurrentUser(String table) {

        //Possible threat of SQL injection
        return String.format("select * from %s WHERE id = ? AND status <> 'deleted'", table);
    }

    /**
     * Querybuilder with a single parameter, which, when specified will get a table.
     * @return SqlStatement
     */
    public String getSqlRecords(String table) {

        //Possible threat of SQL injection
        return String.format("select * from %s", table);
    }

    /**
     * Querybuilder with seven parameters, which, when specified will update the value of the shown columns in the 'users' table
     * @return SqlStatement
     */
    public String updateSqlUser(){

        return "UPDATE Users SET first_name = ?, last_name = ?, email = ?, password = ?, " +
                "status = ?, type = ? WHERE id = ?";
    }

    /**
     * Querybuilder with seven parameters, which, when specified will update the value of the shown columns in the 'games' table
     * @return SqlStatement
     */
    public String updateSqlGame(int type){
        switch (type){
            case JOIN:
                return "UPDATE Games SET status = ?, opponent = ? WHERE id = ?";
            case FINISHED:
                return "UPDATE Games SET status = ?, winner = ?, host_controls = ?, opponent_controls = ? WHERE id = ?";
        }
        return null;
    }

    public String createSqlUser() {

        return "INSERT INTO USERS (first_name, last_name, email, username, password, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    }

    public String createSqlGame() {

        return "INSERT INTO games (host, opponent, status, name, host_controls, map_size) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    }

    public String createSqlScore() {

        return "INSERT INTO scores (user_id, game_id, score, opponent_id) " +
                "VALUES (?, ?, ?, ?)";
    }

    public String deleteSql(String table) {

        return String.format("UPDATE %s SET status = ? WHERE id = ?", table);
    }

    public String getSQLAllGamesByUserID() {

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "INNER JOIN users opponents ON opponents.id = opponent " +
                "WHERE host = ? OR opponent = ?;";
    }

    //Using left outer join for when games are "open". Inner joins not useful as null values will create an empty set. And open games have a lot of null values
    public String getSQLGamesByStatusAndUserID(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username, winners.username AS winner_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "LEFT OUTER JOIN users opponents ON opponents.id = opponent " +
                "LEFT OUTER JOIN users winners ON winners.id = winner " +
                "WHERE games.status = ? " +
                "AND (host = ? OR opponent = ?);";
    }

    public String getSQLOpenGames() {

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "LEFT OUTER JOIN users opponents ON opponents.id = opponent " +
                "WHERE games.status = 'open'";
    }

    public String getSQLOpenGamesByOtherUsers(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM Games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "LEFT OUTER JOIN users opponents ON opponents.id = opponent " +
                "WHERE games.status = 'open' " +
                "AND host != ?";
    }

    public String getSQLGamesInvitedByUserID() {

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "INNER JOIN users opponents ON opponents.id = opponent " +
                "WHERE games.status = 'pending' " +
                "AND opponent = ?";
    }

    public String getSQLGamesHostedByUserID(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "LEFT OUTER JOIN users opponents ON opponents.id = opponent " +
                "WHERE games.status != 'deleted' " +
                "AND host = ?";
    }

    public String authenticatedSql() {

        return "SELECT users.*, SUM(scores.score) as TotalScore " +
                "FROM users " +
                "JOIN scores " +
                "WHERE users.id = scores.user_id " +
                "AND username = ? " +
                "AND status <> 'deleted'";
    }

    public String getSqlHighScore() {

        return "SELECT users.*, SUM(scores.score) as TotalScore " +
                "FROM users " +
                "JOIN scores where users.id = scores.user_id " +
                "GROUP BY users.username " +
                "ORDER BY TotalScore desc";
    }

    public String getScoresByUserID() {

        return " SELECT * " +
                "FROM scores " +
                "WHERE user_id = ?";
    }

    public String getHighScore() {

        return "SELECT games.id as game_id, games.created, games.opponent, games.name as game_name, scores.id as score_id, scores.user_id as user_id, MAX(scores.score) as highscore, users.first_name, users.last_name, users.username " +
                "FROM scores, users, games " +
                "WHERE scores.user_id = users.id and scores.game_id = games.id " +
                "GROUP BY user_id " +
                "ORDER BY highscore DESC";
    }

    public String getSQLHighScores(){

        return "SELECT username, score, game_id, map_size, name, host_controls, opponent_controls " +
                "FROM scores " +
                "INNER JOIN games ON game_id = games.id " +
                "INNER JOIN users ON user_id = users.id " +
                "ORDER BY score " +
                "DESC LIMIT 15;";
    }

    //Used for returning a specific users finished games with scores
    public String getSQLAllFinishedGamesByUserID() {

        return "SELECT games.id, games.name, users.username as opponent_name, users.first_name as opponent_first_name, users.last_name as opponent_last_name, users.id as opponent_id, scores.score, games.winner " +
                "FROM scores, games, users " +
                "WHERE scores.user_id = ? " +
                "AND games.id = scores.game_id " +
                "AND scores.opponent_id = users.id";
    }

    public String getSQLPendingAndOpenGamesByStatusAndUserID(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username " +
                "FROM games " +
                "INNER JOIN users hosts ON hosts.id = host " +
                "LEFT OUTER JOIN users opponents ON opponents.id = opponent " +
                "WHERE host = ? " +
                "AND (games.status = 'open' " +
                "OR games.status = 'pending');";
    }

    public String getSqlRecordsUsers() {

        return "SELECT * " +
                "FROM users " +
                "WHERE id != ? " +
                "AND status = 'active' " +
                "AND type = ?;";
    }

    public String getSqlRecordsUsersForAdmin() {

        return "SELECT * " +
                "FROM users " +
                "WHERE status = 'active';";
    }
}