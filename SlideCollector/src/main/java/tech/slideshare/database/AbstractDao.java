package tech.slideshare.database;

import java.sql.Connection;
import java.util.Objects;

public abstract class AbstractDao {
    protected Connection con;

    public AbstractDao(Connection con) {
        Objects.requireNonNull(con);
        this.con = con;
    }
}
