package xyz.jordanplayz158.csvparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVParser {
    public static Map<String, List<String>> parse(String csv, boolean header) {
        Map<String, List<String>> csvData = new LinkedHashMap<>();

        // Not using System#lineSeparator as RFC 4180 dictates ONLY CRLF for line break.
        List<String> csvLines = new ArrayList<>();

        // Need to be lower than -1
        // as otherwise first columns
        // starting with quotes will
        // lead to incorrect parsing
        int startQuote = -2;
        int endQuote = -2;
        int lastCarriageReturnLineFeed = -2;

        for (int position = 0; position < csv.length(); position++) {
            int positionTwo = position + 1;

            char character = csv.charAt(position);

            if(character == '"') {
                if (isQuoteInQuotes(startQuote, position)) {
                    startQuote = -2;
                } else if (isQuoteInQuotes(endQuote, position)) {
                    endQuote = -2;
                } else {
                    if (startQuote > endQuote) {
                        endQuote = position;
                    } else {
                        startQuote = position;
                    }
                }
            } else if(character == '\r') {
                try {
                    if(csv.charAt(positionTwo) == '\n') {
                        if(isInQuotes(startQuote, endQuote)) {
                            csvLines.add(csv.substring(lastCarriageReturnLineFeed != -2 ? lastCarriageReturnLineFeed : 0, position));
                            lastCarriageReturnLineFeed = positionTwo + 1;
                        }
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }
        }

        if(lastCarriageReturnLineFeed != csv.length()) {
            csvLines.add(csv.substring(lastCarriageReturnLineFeed != -2 ? lastCarriageReturnLineFeed : 0));
        }

        int i = 0;

        if(header) {
            for (String fieldName : csvLines.get(0).split(",")) {
                if(fieldName.startsWith("\"")) fieldName = fieldName.substring(1, fieldName.length() - 1);
                csvData.put(fieldName, new ArrayList<>());
            }

            i = 1;
        }

        for (; i < csvLines.size(); i++) {
            List<String> columns = new ArrayList<>();
            String line = csvLines.get(i);

            // Need to be lower than -1
            // as otherwise first columns
            // starting with quotes will
            // lead to incorrect parsing
            startQuote = -2;
            endQuote = -2;
            int lastComma = -2;

            /* Go through string 1 character at a time
             *
             * Find opening and closing QUOTES while
             *  going through the string by checking
             *  if startQuote is GREATER THAN endQuote
             *  then set endQuote to current position
             *  ELSE set startQuote to current position
             *  If startQuote or endQuote = position - 1
             *  set endQuote or startQuote to -1 as
             *  double empty quote inside quote is
             *  actually a single internal quote and can
             *  be ignored
             *
             * Once COMMA is found check if startQuote
             *  position is less than or equal to endQuote position
             *  store the string as a column
             */
            for(int position = 0; position < line.length(); position++) {
                char character = line.charAt(position);

                if(character == '"') {
                    if(isQuoteInQuotes(startQuote, position)) {
                        startQuote = -2;
                    } else if(isQuoteInQuotes(endQuote, position)) {
                        endQuote = -2;
                    } else {
                        if (startQuote > endQuote) {
                            endQuote = position;
                        } else {
                            startQuote = position;
                        }
                    }
                } else if(character == ',') {
                    if(isInQuotes(startQuote, endQuote)) {
                        columns.add(line.substring(lastComma != -2 ? lastComma : 0, position));
                        lastComma = position + 1;
                    }
                }
            }

            columns.add(line.substring(lastComma != -2 ? lastComma : 0));

            Set<String> mapKeys = csvData.keySet();

            if(header) {
                List<String> headers = new ArrayList<>(mapKeys);

                for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                    String column = parseColumn(columns.get(columnIndex));

                    csvData.get(headers.get(columnIndex)).add(column);
                }
            } else {
                for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                    String columnIndexString = String.valueOf(columnIndex);

                    String column = parseColumn(columns.get(columnIndex));

                    csvData.putIfAbsent(columnIndexString, new ArrayList<>());
                    csvData.get(columnIndexString).add(column);
                }
            }
        }

        return csvData;
    }

    private static boolean isInQuotes(int startQuote, int endQuote) {
        return startQuote <= endQuote;
    }

    /**
     * For CSVs, you can have a quote inside quoted data
     *  for the parser to differentiate, a single quote (")
     *  actually requires 2 quotes ("") when inside the quoted
     *  data, so we check if the new quotes position is one
     *  position ahead of the other, if that is the case, we know
     *  that quote is an embedded quote.
     */
    private static boolean isQuoteInQuotes(int quotePosition, int position) {
        return quotePosition == position - 1;
    }

    private static String parseColumn(String column) {
        if(column.startsWith("\"")) column = column.substring(1, column.length() - 1);

        return column.replaceAll("\"\"", "\"");
    }
}
