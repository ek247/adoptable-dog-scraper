package org.etk247;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public class EmailBuilder {

    private final StringBuilder html;
    private final List<AdoptableDog> rows;
    private final List<String> columnNames;
    private final List<Function<AdoptableDog, String>> rowToHtmlMappers;

    public EmailBuilder() {
        html = new StringBuilder();
        rows = new ArrayList<>();
        columnNames = new ArrayList<>();
        rowToHtmlMappers = new ArrayList<>();
    }

    public EmailBuilder withRows(List<AdoptableDog> dogs) {
        this.rows.addAll(dogs);
        return this;
    }

    public EmailBuilder withColumn(String columnName, Function<AdoptableDog, String> rowToHtmlMapper) {
        columnNames.add(columnName);
        rowToHtmlMappers.add(rowToHtmlMapper);
        return this;
    }

    public String buildHtml() {
        html
            .append("<html>")
            .append("<body>")
            .append("<table>");

        String tableHeaders = columnNames.stream()
            .map(name -> String.format("<th> %s </th>", name))
            .collect(joining("\n"));

        html.append(String.format("<tr> %s </tr>", tableHeaders));

        String tableRows = rows.stream()
            .map(dog -> String.format(
                "<tr> %s </tr>", rowToHtmlMappers.stream()
                    .map(f -> String.format("<th> %s </th>", f.apply(dog)))
                    .collect(joining("\n"))))
            .collect(joining("\n"));

        html.append(tableRows);

        html
            .append("</table>")
            .append("</body>")
            .append("</html>");

        return html.toString();
    }

}
