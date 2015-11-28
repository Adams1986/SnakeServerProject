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
     * @param table
     * @return SqlStatement
     */
    public String getSqlRecord(String table) {

        return "select * from " + table + " WHERE id = ? AND status <> 'deleted'";
    }

    public String getSqlRecordWithHighScore(String table) {

        return "select * from " + table + " WHERE id = ? AND status <> 'deleted'";
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
        return "Insert into users (first_name, last_name, email, username, password, type) " +
                "values (?, ?, ?, ?, ?, ?)";
    }

    public String createSqlGame() {
        return "Insert into games (host, opponent, status, name, host_controls, map_size) " +
                "values (?, ?, ?, ?, ?, ?)";
    }

    public String createSqlScore() {
        return "Insert into scores (user_id, game_id, score, opponent_id) " +
                "values (?, ?, ?, ?)";
    }

    public String deleteSql(String table) {
        return "UPDATE " + table + " SET status = ? WHERE id = ?";
    }

    public String getSQLAllGamesByUserID() {
        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host INNER JOIN users opponents ON opponents.id = opponent WHERE host = ? OR opponent = ?;";
    }

    public String getSQLGamesByStatusAndUserID(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username, winners.username AS winner_username FROM games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent LEFT OUTER JOIN users winners ON winners.id = winner WHERE games.status = ? AND (host = ? OR opponent = ?);";
        //return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent WHERE games.status = ? AND (host = ? OR opponent = ?);";
    }

    public String getSQLOpenGames() {
        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent WHERE games.status = 'open'";
    }

    public String getSQLOpenGamesByOtherUsers(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM Games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent WHERE games.status = 'open' AND host != ?";
    }

    public String getSQLGamesInvitedByUserID() {
        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host INNER JOIN users opponents ON opponents.id = opponent WHERE games.status = 'pending' and opponent = ?";
    }

    public String getSQLGamesHostedByUserID(){
        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent WHERE games.status != 'finished' AND host = ?";
    }

    public String authenticatedSql() {
        return "Select * from users where username = ? AND status <> 'deleted'";
    }

    public String getSqlHighScore() {
        return "select users.*, sum(scores.score) as TotalScore from users " +
                "join scores where users.id = scores.user_id group by users.username order by TotalScore desc";
    }

    public String getScoresByUserID() {
        return " select * from scores where user_id = ?";
    }

    public String getHighScore() {
        return "select games.id as game_id, games.created, games.opponent, games.name as game_name, scores.id as score_id, scores.user_id as user_id, max(scores.score) as highscore, users.first_name, users.last_name, users.username from scores, users, games where scores.user_id = users.id and scores.game_id = games.id group by user_id order by highscore desc";
    }

    public String getSQLHighScores(){

        return "SELECT username, score, game_id, name, host_controls, opponent_controls FROM scores INNER JOIN games ON game_id = games.id INNER JOIN users ON user_id = users.id ORDER BY score DESC LIMIT 25;";
    }

    //Used for returning a specific users finished games with scores
    public String getSQLAllFinishedGamesByUserID() {
        return "select games.id, games.name, users.username as opponent_name, users.first_name as opponent_first_name, users.last_name as opponent_last_name, users.id as opponent_id, scores.score, games.winner from scores, games, users where scores.user_id = ? and games.id = scores.game_id and scores.opponent_id = users.id";
    }

    public String getSQLPendingAndOpenGamesByStatusAndUserID(){

        return "SELECT games.*, hosts.username AS host_username, opponents.username AS opponent_username FROM games INNER JOIN users hosts ON hosts.id = host LEFT OUTER JOIN users opponents ON opponents.id = opponent WHERE host = ? AND (games.status = 'open' OR games.status = 'pending');";
    }

}