//@@author nattanyz
package dream.fcard.logic.stats;

import java.time.LocalDateTime;

import dream.fcard.logic.storage.Schema;
import dream.fcard.util.json.exceptions.JsonWrongValueException;
import dream.fcard.util.json.jsontypes.JsonObject;
import dream.fcard.util.json.jsontypes.JsonValue;
import dream.fcard.util.stats.DateTimeUtil;

/** A TestSession represents a review session involving a particular deck. */
public class TestSession extends Session {

    /** The Result of this particular session. */
    private String score;

    public TestSession(LocalDateTime start, LocalDateTime end, String score) {
        super(start, end);
        this.score = score;
    }

    /** Gets the Result of this particular session. */
    public String getScore() {
        return this.score;
    }

    /** Sets the Result of this particular session. */
    public void setScore(String score) {
        this.score = score;
    }

    @Override
    public void endSession() {
        super.endSession();
    }

    /**
     * Ends the session, and sets the result to the given value.
     * @param score Score of the session.
     */
    public void endSession(String score) {
        this.endSession();
        this.setScore(score);
    }

    /** Checks if this session has a score. */
    public boolean hasScore() {
        return this.score != null;
    }

    @Override
    public JsonValue toJson() {
        JsonObject obj = new JsonObject();
        try {
            obj.put(Schema.SESSION_START,
                DateTimeUtil.getJsonFromDateTime(sessionStart).getObject());
            obj.put(Schema.SESSION_END,
                DateTimeUtil.getJsonFromDateTime(sessionEnd).getObject());
            obj.put(Schema.SESSION_RESULT, score);
        } catch (JsonWrongValueException e) {
            System.out.println("DATETIME JSON MUST BE AN OBJECT\n" + e.getMessage());
        }
        return new JsonValue(obj);
    }
}
