package tech.slideshare.bluesky.database;

import java.sql.Connection;

public abstract class AbstractDao {
    protected final Connection con;

    public AbstractDao(Connection con) {
        this.con = con;
    }
}

