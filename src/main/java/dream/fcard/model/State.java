package dream.fcard.model;

import java.util.ArrayList;
import java.util.Stack;

import dream.fcard.core.commons.core.LogsCenter;
import dream.fcard.logic.storage.StorageManager;
import dream.fcard.model.exceptions.DeckNotFoundException;
import dream.fcard.model.exceptions.NoDeckHistoryException;
import dream.fcard.model.exceptions.NoUndoHistoryException;

/**
 * State stores data representing the state of the running program
 * It should not execute logic or parsing, simply a data store object.
 */
public class State {
    private ArrayList<Deck> decks;
    private Stack<ArrayList<Deck>> deckHistory;
    private Stack<ArrayList<Deck>> undoHistory;
    private StateEnum currState;
    private Deck currentDeck;

    /**
     * Constructor to create a State object with no Deck objects.
     */
    public State() {
        decks = StorageManager.loadDecks();
        currState = StateEnum.DEFAULT;
        deckHistory = new Stack<>();
        undoHistory = new Stack<>();
    }

    /**
     * Return the current deck in Create mode.
     * @return the deck in Create Mode.
     */
    public Deck getCurrentDeck() {
        return currentDeck;
    }


    /**
     * Returns false if decks is non-empty, true if decks is empty.
     */
    public boolean isEmpty() {
        return decks.size() == 0;
    }

    /**
     * Returns the list of decks.
     */
    public ArrayList<Deck> getDecks() {
        return decks;
    }

    /**
     * Adds a new empty Deck object to decks list.
     */
    public void addDeck(String deckName) {
        /*
        System.out.println("BEFORE ADDING");
        printDecks();
        printDeckHistory();*/

        addCurrDecksToDeckHistory();
        Deck temp = new Deck(deckName);
        decks.add(temp);
        this.currentDeck = temp;

        /*System.out.println("AFTER ADDING");
        printDecks();
        printDeckHistory();*/
    }

    /**
     * Adds a deck object to decks list.
     *
     * @param deck deck object
     */
    public void addDeck(Deck deck) {
        addCurrDecksToDeckHistory();
        decks.add(deck);
        this.currentDeck = deck;
    }

    /**
     * Removes the deck from the decks list, if there is a deck with a matching name.
     * Else, throw exception when no deck with matching name is found.
     */
    public void removeDeck(String name) throws DeckNotFoundException {
        int deckIndex = getDeckIndex(name);
        if (deckIndex == -1) {
            throw new DeckNotFoundException("Deck not found - " + name);
        }
        addCurrDecksToDeckHistory();
        decks.remove(deckIndex);
    }

    /**
     * Returns the deck object that matches in name, if a deck with matching name exists.
     * Else, throw exception when no deck with matching name is found.
     *
     * @return index
     */
    public Deck getDeck(String name) throws DeckNotFoundException {
        int indexOfDeck = getDeckIndex(name);
        if (indexOfDeck == -1) {
            throw new DeckNotFoundException("Deck not found - " + name);
        }
        return decks.get(indexOfDeck);
    }

    /**
     * Returns the index of a deck given the deck name, if a deck with matching name exists.
     * Else, return -1 if no deck with matching name is found.
     * <p>
     * Note: this method is only used internally for State processing.
     * Should not be confused with user seen indexes, since this is 0-based index.
     *
     * @return index
     */
    private int getDeckIndex(String name) {
        for (int i = 0; i < decks.size(); i++) {
            Deck currentDeck = decks.get(i);
            boolean isUserInputMatchDeckName = currentDeck.getDeckName().equals(name);

            if (isUserInputMatchDeckName) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Set current state of the app.
     * @param currState the current state.
     */
    public void setCurrState(StateEnum currState) {
        this.currState = currState;
        LogsCenter.getLogger(State.class).info("Entering state: + this.currState");
    }

    /**
     * Get current state of the app.
     * @return the current state.
     */
    public StateEnum getCurrState() {
        return currState;
    }

    /**
     * Checks whether a deck with the given name exists. To prevent duplicates.
     * @param name
     * @return the index.
     */
    public int hasDeckName(String name) {
        for (int i = 0; i < decks.size(); i++) {
            if (decks.get(i).getDeckName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds the current deck to the deckHistory for Undo command
     */
    public void addCurrDecksToDeckHistory() {
        @SuppressWarnings("unchecked")
        ArrayList<Deck> currDeck = (ArrayList<Deck>) this.decks.clone();

        if (deckHistory.empty() || !currDeck.equals(deckHistory.peek())) {
            deckHistory.push(currDeck);
        }
    }

    /**
     * Adds the current deck to the undoHistory for Redo command
     */
    public void addCurrDecksToUndoHistory() {
        @SuppressWarnings("unchecked")
        ArrayList<Deck> currDeck = (ArrayList<Deck>) this.decks.clone();
        if (undoHistory.empty() || !currDeck.equals(undoHistory.peek())) {
            undoHistory.push(currDeck);
        }
    }

    /**
     * Used for debugging.
     */
    // For Debugging
    public void printDeckHistory() {
        System.out.println("DECK HISTORY SIZE: " + deckHistory.toArray().length);
        System.out.println("TOP DECK");
        if (!deckHistory.empty()) {
            for (Deck d : deckHistory.peek()) {
                System.out.println(d.getDeckName());
            }
        }
        System.out.println("DECK HISTORY END");
    }

    /**
     * Used for debugging.
     */
    // For Debugging
    public void printDecks() {
        System.out.println("DECKS SIZE: " + this.decks.toArray().length);
        for (Deck d : this.decks) {
            System.out.println(d.getDeckName());
        }
        System.out.println("DECKS END");
    }

    /**
     * Undoes the latest changes to the current Deck.
     *
     * @throws NoDeckHistoryException
     */
    public void undoDeckChanges() throws NoDeckHistoryException {
        if (deckHistory.empty()) {
            throw new NoDeckHistoryException("There is no action to undo!");
        }
        System.out.println("Undo");
        // Adds the current deck to the stack of undos
        addCurrDecksToUndoHistory();

        // Remove the last deck from history and makes it the curr list of Decks
        System.out.println("this.decks");
        for (Deck d : this.decks) {
            System.out.println(d.getDeckName());
        }
        System.out.println("deckHistory");
        for (Deck d : this.deckHistory.peek()) {
            System.out.println(d.getDeckName());
        }

        ArrayList<Deck> newCurr = deckHistory.pop();
        this.decks = newCurr;
        for (Deck d : this.decks) {
            System.out.println(d.getDeckName());
        }
    }

    /**
     * Redoes the latest changes to the current Deck.
     *
     * @throws NoUndoHistoryException
     */
    public void redoDeckChanges() throws NoUndoHistoryException {
        if (undoHistory.empty()) {
            throw new NoUndoHistoryException("An undo command must be done first!");
        }

        // Adds the current deck to the stack of deckHistory
        ArrayList<Deck> curr = decks;
        addCurrDecksToDeckHistory();

        // Remove the last deck from history and makes it the curr list of Decks
        ArrayList<Deck> newCurr = undoHistory.pop();
        this.decks = newCurr;
        for (Deck d : decks) {
            System.out.println(d.getDeckName());
        }
    }
}
