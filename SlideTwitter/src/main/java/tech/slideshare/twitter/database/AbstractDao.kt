package tech.slideshare.twitter.database

import java.sql.Connection

abstract class AbstractDao(protected val con: Connection)
