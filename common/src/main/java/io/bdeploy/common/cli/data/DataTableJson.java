package io.bdeploy.common.cli.data;

import java.io.PrintStream;
import java.util.List;

class DataTableJson extends DataTableBase {

    DataTableJson(PrintStream output) {
        super(output);
    }

    @Override
    public void render() {
        out().println("[");

        for (int i = 0; i < getRows().size(); ++i) {
            List<DataTableCell> row = getRows().get(i);

            out().print("  { ");

            int colIndex = 0;
            for (int y = 0; y < row.size(); ++y) {
                DataTableColumn col = getColumns().get(colIndex);

                out().print(DataRenderingHelper.quoteJson(col.getName()) + ": "
                        + DataRenderingHelper.quoteJson(row.get(y).getData()));

                if (y == (row.size() - 1)) {
                    if (i == (getRows().size() - 1)) {
                        out().println(" }");
                    } else {
                        out().println(" },");
                    }
                } else {
                    out().print(", ");
                }

                colIndex += row.get(y).getSpan();
            }
        }

        out().println("]");
    }
}
