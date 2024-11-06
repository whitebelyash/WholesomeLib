package ru.whbex.lib.sql;

import java.sql.ResultSet;

public record SQLResponse(ResultSet resultSet, int updateResult, int[] updateResultBatch) {
}
