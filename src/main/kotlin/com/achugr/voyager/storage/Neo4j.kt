package com.achugr.voyager.storage

import com.achugr.voyager.config
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory

val sessionFactory = run {
    val configuration = Configuration.Builder()
        .uri(config.getString("storage.neo4j.url"))
        .credentials(
            config.getString("storage.neo4j.credentials.username"),
            config.getString("storage.neo4j.credentials.password")
        )
        .autoIndex("update")
        .build()
    SessionFactory(configuration, "com.achugr.voyager.entity")
}

fun <T> transaction(statement: (session: Session) -> T): T {
    val s = sessionFactory.openSession()
    val tx = s.beginTransaction()
    try {
        val result = statement.invoke(s)
        tx.commit()
        return result
    } catch (e: Exception) {
        tx.rollback()
        throw e
    } finally {
        tx.close()
    }
}


