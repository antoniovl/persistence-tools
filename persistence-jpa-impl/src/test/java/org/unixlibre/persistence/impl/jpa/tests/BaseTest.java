package org.unixlibre.persistence.impl.jpa.tests;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unixlibre.persistence.CommandExecutor;
import org.unixlibre.persistence.ExecutorContext;
import org.unixlibre.persistence.SQLExecutor;
import org.unixlibre.persistence.impl.jpa.JPAExecutorContext;
import org.unixlibre.persistence.impl.jpa.tests.model.Author;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Created by antoniovl on 13/05/17.
 */
public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    public static final String PU_NAME = "persistence-test-PU";
    public static final String JDBC_URL = "jdbc:derby:memory:persistenceTest;create=true";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CommandExecutor executor;
    private EmbeddedDriver embeddedDriver;

    public void setupDatabase() {
        String sql = loadDatabaseSchema("db-schema-derby.sql");
        embeddedDriver = new EmbeddedDriver();
        ;
        try (Connection conn = embeddedDriver.connect(JDBC_URL, new Properties())) {
            SQLExecutor sqlExecutor = new SQLExecutor(conn, sql);
            sqlExecutor.execute();
        } catch (SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String loadDatabaseSchema(String file) {
        String resource = String.format("org/unixlibre/persistence/impl/jpa/tests/derby/%s", file);
        InputStream is = BaseTest.class.getClassLoader().getResourceAsStream(resource);
        final int BLOCK_SIZE = 8192;
        byte[] b = new byte[BLOCK_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean done = false;

        try {
            while (!done) {
                int r = is.read(b, 0, BLOCK_SIZE);
                if (r < 0) {
                    done = true;
                } else {
                    baos.write(b, 0, r);
                }
            }
            return new String(b, "UTF-8");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public EntityManager getEntityManager(ExecutorContext executorContext) {
        JPAExecutorContext ctx = JPAExecutorContext.fromExecutorContext(executorContext);
        if (ctx != null) {
            return ctx.getEntityManager();
        }
        throw new IllegalStateException("JPAExecutorContext null");
    }
}