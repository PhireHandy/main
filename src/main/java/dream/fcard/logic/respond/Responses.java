package dream.fcard.logic.respond;

import dream.fcard.logic.respond.commands.CreateCommand;
import dream.fcard.logic.respond.commands.HelpCommand;
import dream.fcard.logic.storage.StorageManager;
import dream.fcard.model.Deck;
import dream.fcard.model.State;
import dream.fcard.model.StateEnum;
import dream.fcard.model.cards.FrontBackCard;
import dream.fcard.model.exceptions.DuplicateInChoicesException;
import dream.fcard.util.RegexUtil;
import java.util.ArrayList;

/**
 * The enums are composed of three properties:
 *  1) regex the input must match
 *  2) ResponseGroup(s) the enum belong to
 *  3) function processing input and state if input matches regex
 *
 *  Order in which the enums are declared is IMPORTANT, as top most enums
 *  are checked first before last, thus last enums should be more generic
 *  and higher should be more specific; thus you can see valid enums
 *  followed by error enums declared in that order often.
 *
 *  This class is to be used for all parsing, state mutation logic and dispatcher calls.
 *  In no other class should they take the responsibility.
 */
public enum Responses {
    // DEFAULT GROUP STARTS ----------------------------------
    CREATE_NEW_DECK_WITH_NAME(
            "^((?i)create)\\s+((?i)deck/)\\s*\\S",
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                String deckName = i.split("(?i)deck/\\s*")[1];
                s.mode = StateEnum.CREATE;
                s.createModeDeck = new Deck(deckName);
                s.decks.add(s.createModeDeck);
                Dispatcher.accept(ConsumerSchema.CREATE_NEW_DECK_W_NAME, deckName);
                return true;
            }
    ),
    CREATE_ERROR(
            "^((?i)create)",
            new ResponseGroup[] {ResponseGroup.DEFAULT},
            (i,s) -> {
                Dispatcher.accept(ConsumerSchema.DISPLAY_MESSAGE,"Create command is invalid!");
                return true;
            }
    ),
    // ADD_CARD regex format: add deck/DECK_NAME [priority/PRIORITY_NAME] front/FRONT back/BACK [choice/CHOICE]
    // Only used for MCQ and FrontBack cards
    // Note that back for MCQ cards will be used for identifying the correct CHOICE
    ADD_CARD(
            RegexUtil.commandFormatRegex("add", new String[]{"deck/", "front/", "back/"}),
            new ResponseGroup[] {ResponseGroup.DEFAULT},
            (i,s) -> {
                ArrayList<ArrayList<String>> res = RegexUtil.parseCommandFormat("add",
                        new String[]{"deck/", "priority/", "front/", "back/", "choice/"},
                        i);
                try {
                    return CreateCommand.createMcqFrontBack(res);
                } catch (DuplicateInChoicesException dicExc) {
                    Dispatcher.accept(ConsumerSchema.DISPLAY_MESSAGE,"There are duplicated choices!");
                    return true;
                }
            }
    ),
    ADD_CARD_ERROR(
            "^((?i)(add)",
            new ResponseGroup[] {ResponseGroup.DEFAULT},
            (i,s) -> {
                Dispatcher.accept(ConsumerSchema.DISPLAY_MESSAGE,"Add command is invalid!");
                return true;
            }
    ),
    SEE_SPECIFIC_DECK(
            "^((?i)view)\\s+[0-9]+$",
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                int num = Integer.parseInt(i.split("^(?i)view\\s+")[1]);
                Dispatcher.accept(ConsumerSchema.SEE_SPECIFIC_DECK, num);
                return true;
            }
    ),
    SEE_SPECIFIC_DECK_ERROR(
            "^((?i)view)",
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                //TODO dispatcher print no numeric index given
                return true;
            }
    ),
    ADD_NEW_ROW_MCQ(
            "^((?i)add)\\s+((?i)option)$",
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                //TODO not implemented
                return true;
            }
    ),
    HELP_W_COMMAND(
            RegexUtil.commandFormatRegex("", new String[]{"command/"}),
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                ArrayList<ArrayList<String>> res = RegexUtil.parseCommandFormat("help",
                        new String[]{"command/"},
                        i);
                for (String curr : HelpCommand.allCommands) {
                    Dispatcher.accept(ConsumerSchema.DISPLAY_MESSAGE, curr);
                    return true;
                }
                return false;
            }
    ),
    HELP(
            "^((?i)help)(.)*",
            new ResponseGroup[]{ResponseGroup.DEFAULT},
            (i,s) -> {
                //TODO open a window to UserGuide.html
                return true;
            }
    ),
    // DEFAULT GROUP ENDS ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // CREATE GROUP STARTS -------------------------------------
    EXIT_CREATE(
            "^((?i)exit)\\s*$",
            new ResponseGroup[]{ResponseGroup.CREATE},
            (i,s) -> {
                s.mode = StateEnum.DEFAULT;
                Dispatcher.accept(ConsumerSchema.EXIT_CREATE, true);
                return true;
            }
    ),
    PROCESS_INPUT_FRONT_BACK(
            RegexUtil.commandFormatRegex("", new String[]{"front/", "back/"}),
            new ResponseGroup[]{ResponseGroup.CREATE},
            (i,s) -> {
                ArrayList<ArrayList<String>> res = RegexUtil.parseCommandFormat("", new String[]{"front/", "back/"}, i);
                if (res.get(0).size() > 0 && res.get(1).size() > 0) {
                    FrontBackCard card = new FrontBackCard(res.get(0).get(0), res.get(1).get(0));
                    s.createModeDeck.addNewCard(card);
                    StorageManager.writeDeck(s.createModeDeck);
                    // dispatch card to CreateDeckDisplay to be added to tempDeck
                    // make editing window dispatches
                } else {
                    //TODO arguments cannot be blank
                }
                return true;
            }
    ),
    // CREATE GROUP ENDS ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // MATCH ALL GROUP STARTS ---------------------------------
    QUIT(
            "^((?i)quit)\\s*$",
            new ResponseGroup[]{ResponseGroup.MATCH_ALL},
            (i,s) -> {
                Dispatcher.accept(ConsumerSchema.QUIT_PROGRAM, true);
                return true;
            }
    ),
    UNKNOWN(
            ".*",
            new ResponseGroup[]{ResponseGroup.MATCH_ALL},
            (i,s) -> {
                //TODO dispatcher display unknown input
                return true;
            }
    )
    // MATCH ALL GROUP ENDS ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    ;

    private String regex;
    private ResponseGroup[] group;
    private ResponseFunc func;
    Responses(String r, ResponseGroup[] grp, ResponseFunc f) {
        regex = r;
        group = grp;
        func = f;
    }

    /**
     * Given a string and program state, if string matches regex
     * this enum will call its response function.
     *
     * @param i input string
     * @param s state object
     * @return boolean if the string has matched
     */
    public boolean call(String i, State s) {
        if (i.matches(regex)) {
            return func.funcCall(i, s);
        }
        return false;
    }

    /**
     * Given a ResponseGroup, determine if this Response belongs to it.
     * @param groupArg  ResponseGroup
     * @return          True, belongs to group
     */
    public boolean isInGroup(ResponseGroup groupArg) {
        for (ResponseGroup g : group) {
            if (g == groupArg) {
                return true;
            }
        }
        return false;
    }
}
