package dream.fcard.gui.controllers.windows;

import java.io.IOException;

import dream.fcard.logic.stats.Session;
import dream.fcard.logic.stats.SessionList;
import dream.fcard.logic.stats.UserStats;
import dream.fcard.logic.stats.UserStatsHolder;
import dream.fcard.model.Deck;
import dream.fcard.util.stats.SessionListUtil;
import dream.fcard.util.stats.StatsDisplayUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

/**
 * Window to display user's statistics.
 */
public class StatisticsWindow extends ScrollPane {
    @FXML
    private Label totalSessions;
    @FXML
    private Label sessionsThisWeek;
    @FXML
    private Label totalDuration;
    @FXML
    private Label averageDuration;
    @FXML
    private ScrollPane sessionsScrollPane;
    @FXML
    private TableView<Session> sessionsTableView;
    @FXML
    private ScrollPane deckTableScrollPane;
    @FXML
    private TableView<Deck> deckTableView;

    private UserStats userStats;

    /** Creates a new instance of StatisticsWindow. */
    public StatisticsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class
                .getResource("/view/Windows/StatisticsWindow.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            //TODO: replace with logger
            e.printStackTrace();
        }

        this.userStats = UserStatsHolder.getUserStats();
        displaySummaryStats();

        this.sessionsTableView = StatsDisplayUtil.getUserSessionsTableView();
        this.sessionsScrollPane.setContent(sessionsTableView);

        this.deckTableView = StatsDisplayUtil.getDeckTableView();
        this.deckTableScrollPane.setContent(deckTableView);
        allowDeckStatisticsWindowToBeOpened();
    }

    /** Retrieves and displays numerical stats, like the total number of login sessions. */
    private void displaySummaryStats() {
        int numSessions = userStats.getSessionList().getNumberOfSessions();
        this.totalSessions.setText("Total login sessions: " + numSessions
            + (numSessions == 1 ? " session" : " sessions"));

        SessionList sublistForThisWeek = SessionListUtil.getSublistForThisWeek(
            userStats.getSessionList());
        int numSessionsThisWeek = sublistForThisWeek.getNumberOfSessions();
        this.sessionsThisWeek.setText("Total login sessions this week: " + numSessionsThisWeek
            + (numSessionsThisWeek == 1 ? " session" : " sessions"));

        String duration = userStats.getSessionList().getTotalDurationAsString();
        this.totalDuration.setText("Total login duration: " + duration);

        String averageDuration = userStats.getSessionList().getAverageDurationAsString();
        this.averageDuration.setText("Average duration per login: " + averageDuration);
    }

    /** Allows the relevant DeckStatisticsWindow to be opened when a row of the deckTableView is double-clicked. */
    private void allowDeckStatisticsWindowToBeOpened() {
        //@@author nattanyz-reused
        // solution adapted from https://stackoverflow.com/questions/30191264/javafx-tableview-
        // how-to-get-the-row-i-clicked
        this.deckTableView.setRowFactory(tv -> {
            TableRow<Deck> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2) {
                    Deck selectedDeck = row.getItem();
                    StatsDisplayUtil.openDeckStatisticsWindow(selectedDeck);
                }
            });
            return row;
        });
        //@@author
    }
}
