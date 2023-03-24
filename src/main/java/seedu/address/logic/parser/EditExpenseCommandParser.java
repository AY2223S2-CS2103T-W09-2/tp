package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRICE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditExpenseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Javadoc
 */
public class EditExpenseCommandParser implements Parser<EditExpenseCommand> {

    /**
     * JavaDoc
     * @param args xxx
     * @return xxx
     * @throws ParseException
     */
    public EditExpenseCommand parse(String args) throws ParseException {
        //First check if the given index is valid.
        ArgumentMultimap argMultiMap =
                ArgumentTokenizer.tokenize(args, PREFIX_CATEGORY, PREFIX_DATE, PREFIX_NAME, PREFIX_PRICE);
        Index index = ParserUtil.parseIndex(argMultiMap.getPreamble());
        String newExpenseName;
        Double newPrice;
        LocalDate newDate;
        String newCategory;

        //Get the new category name if applicable
        if (isPrefixPresent(argMultiMap, PREFIX_CATEGORY)) {
            newCategory = argMultiMap.getValue(PREFIX_CATEGORY).get();
        } else {
            newCategory = null;
        }

        if (isPrefixPresent(argMultiMap, PREFIX_NAME)) {
            newExpenseName = argMultiMap.getValue(PREFIX_NAME).get();
        } else {
            newExpenseName = null;
        }

        if (isPrefixPresent(argMultiMap, PREFIX_PRICE)) {
            String inputPriceInString = argMultiMap.getValue(PREFIX_PRICE).get();
            try {
                newPrice = Double.parseDouble(inputPriceInString);
            } catch (NumberFormatException nfe) {
                throw new ParseException("Invalid price!", nfe);
            }
        } else {
            newPrice = null;
        }

        if (isPrefixPresent(argMultiMap, PREFIX_DATE)) {
            String inputDateInString = argMultiMap.getValue(PREFIX_DATE).get();
            try {
                newDate = ParserUtil.parseDate(inputDateInString);
            } catch (ParseException pe) {
                throw new ParseException("Invalid date format! Please use DD-MM-YY format!", pe);
            }
        } else {
            newDate = null;
        }

        return new EditExpenseCommand(index, newExpenseName, newPrice, newDate, newCategory);
    }

    /**
     * Returns true if the given prefix does not contain {@code Optional} values in the given {@code ArgumentMultimap}
     * @param argMultiMap The argument multimap to check for.
     * @param toCheck The prefix to check for.
     * @return Boolean that indicates whether the given prefix is present or not.
     */
    private static boolean isPrefixPresent(ArgumentMultimap argMultiMap, Prefix toCheck) {
        return argMultiMap.getValue(toCheck).isPresent();
    }

}
