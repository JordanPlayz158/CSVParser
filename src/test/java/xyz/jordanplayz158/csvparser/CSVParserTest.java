package xyz.jordanplayz158.csvparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class CSVParserTest {
    // RFC 4180 2.1
    @Test
    void testCsvCRLFLastLine() {
        Map<String, List<String>> data = CSVParser.parse("one,two,three\r\nfour,five,six\r\n", false);

        Assertions.assertEquals(Map.of("0", List.of("one", "four"), "1", List.of("two", "five"), "2", List.of("three", "six")), data);
    }

    // RFC 4180 2.2
    @Test
    void testCsv() {
        Map<String, List<String>> data = CSVParser.parse("one,two,three\r\nfour,five,six", false);

        Assertions.assertEquals(Map.of("0", List.of("one", "four"), "1", List.of("two", "five"), "2", List.of("three", "six")), data);
    }

    // RFC 4180 2.3
    @Test
    void testCsvWithHeaders() {
        Map<String, List<String>> data = CSVParser.parse("header0,header1,header2\r\none,two,three", true);

        Assertions.assertEquals(Map.of("header0", List.of("one"), "header1", List.of("two"), "header2", List.of("three")), data);
    }

    // TODO:
    //  RFC 4180 2.4 validation
    //  "Each line should contain the same number of fields throughout the file."

    // RFC 4180 2.4
    //  "Spaces are considered part
    //       of a field and should not be ignored."
    @Test
    void testCsvWithHeadersAndDataWithLeadingAndTrailingSpaces() {
        Map<String, List<String>> data = CSVParser.parse(" header0,header1 , header2 \r\none , two, three ", true);

        Assertions.assertEquals(Map.of(" header0", List.of("one "), "header1 ", List.of(" two"), " header2 ", List.of(" three ")), data);
    }

    // RFC 4180 2.5 and 2.7
    @Test
    void testCsvWithQuotedData() {
        Map<String, List<String>> data = CSVParser.parse("one,\"two with \"quotes\"\",three", false);

        Assertions.assertEquals(Map.of("0", List.of("one"), "1", List.of("two with \"quotes\""), "2", List.of("three")), data);
    }

    // RFC 4180 2.5 and 2.7
    @Test
    void testCsvWithQuotedDataAndWithHeaders() {
        Map<String, List<String>> data = CSVParser.parse("header0,header1,header2\r\none,\"two with \"\"quotes\"\"\",three", true);

        Assertions.assertEquals(Map.of("header0", List.of("one"), "header1", List.of("two with \"quotes\""), "header2", List.of("three")), data);
    }

    // RFC 4180 2.5
    @Test
    void testCsvWithQuotesInHeaders() {
        Map<String, List<String>> data = CSVParser.parse("\"header 0 with space\",\"header 1 with space\"\r\n\"one\",\"two with \"\"quotes\"\"\"", true);

        Assertions.assertEquals(Map.of("header 0 with space", List.of("one"), "header 1 with space", List.of("two with \"quotes\"")), data);
    }

    // RFC 4180 2.5
    @Test
    void testCsvWithCommasInQuotes() {
        Map<String, List<String>> data = CSVParser.parse("\"one, two, three\",\"four, five, six\",\"seven, eight, nine\"", false);

        Assertions.assertEquals(Map.of("0", List.of("one, two, three"), "1", List.of("four, five, six"), "2", List.of("seven, eight, nine")), data);
    }

    // RFC 4180 2.7
    @Test
    void testCsvWithQuotedQuotes() {
        Map<String, List<String>> data = CSVParser.parse("one,two,\"three \"\"quote\"\"\"", false);

        Assertions.assertEquals(Map.of("0", List.of("one"), "1", List.of("two"), "2", List.of("three \"quote\"")), data);
    }

    // RFC 4180 2.6
    @Test
    void testCsvWithNewLinesQuotesAndCommasInQuotes() {
        Map<String, List<String>> data = CSVParser.parse("\"one\",\"t\r\nwo\",\"three\"\r\nfour,five,six", false);

        Assertions.assertEquals(Map.of("0", List.of("one", "four"), "1", List.of("t\r\nwo", "five"), "2", List.of("three", "six")), data);
    }
}
