package org.ict4htw.atomfeed.client;

import com.sun.syndication.feed.atom.Feed;
import org.ict4htw.atomfeed.SpringIntegrationIT;
import org.ict4htw.atomfeed.client.repository.AllFeeds;
import org.ict4htw.atomfeed.client.repository.datasource.WebClient;
import org.ict4htw.atomfeed.server.repository.DbEventRecordCreator;
import org.ict4htw.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import com.sun.syndication.feed.atom.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

public class AtomFeedClientIT extends SpringIntegrationIT{

    private DbEventRecordCreator recordCreator;
    private Connection connection;
    private AllEventRecordsJdbcImpl eventRecords;
    private AllFeeds allFeeds;

    @Before
    public void before() throws SQLException {
        connection = getConnection();
        Statement statement = connection.createStatement();
        statement.execute("delete from atomfeed.event_records");
        statement.close();
        eventRecords = new AllEventRecordsJdbcImpl(getProvider(connection));
        recordCreator = new DbEventRecordCreator(eventRecords);
        allFeeds = new AllFeeds(new WebClient());
    }

    @After
    public void after() throws SQLException {
        connection.close();
    }

    //TODO - Set Chunking history chunk_size so that we always query for the first feed. Simplicity.
    //Spawn mvn jetty:run - either from here or a script outside.
    // Also, this should be run on Travis.
    //Set atom mime type on response from server, so that response can be de-Serialized appropriately.
    // Make the spawned mvn jetty server read from the DB all the while to ensure that this test passes.
    @Ignore
    public void shouldReadEventsCreatedEvents() throws URISyntaxException {
        String uuid = UUID.randomUUID().toString();
        createOneEvent(uuid,"One Event","http://google.com");
        Integer id = eventRecords.get(uuid).getId();
        URI uri = new URI(String.format("http://localhost:8080/events/1"));
        Feed feed = allFeeds.getFor(uri);
        List entries = feed.getEntries();
        assertEquals(1, entries.size());
        Entry entry = (Entry) entries.get(0);
        assertEquals(entry.getId(),uuid);
    }

    private void createOneEvent(String uuid,String title, String url) throws URISyntaxException {
        recordCreator.create(uuid,title,url,null);
    }
}