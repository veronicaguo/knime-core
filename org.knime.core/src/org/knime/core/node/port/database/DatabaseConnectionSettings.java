/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.core.node.port.database;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.core.node.util.StringHistory;
import org.knime.core.util.KnimeEncryption;

/**
 *
 * @author Thomas Gabriel, University of Konstanz
 */
public class DatabaseConnectionSettings {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(DatabaseConnectionSettings.class);

    /** Config for SQL statement. */
    public static final String CFG_STATEMENT = "statement";

    /** Keeps the history of all loaded driver and its order. */
    public static final StringHistory DRIVER_ORDER = StringHistory.getInstance(
            "database_drivers");

    /** Keeps the history of all database URLs. */
    public static final StringHistory DATABASE_URLS = StringHistory.getInstance(
            "database_urls");

    private static final ExecutorService CONNECTION_CREATOR_EXECUTOR =
        Executors.newCachedThreadPool();

    /**
     * DriverManager login timeout for database connection; not implemented/
     * used by all databases.
     */
    private static final int LOGIN_TIMEOUT = initLoginTimeout();
    private static int initLoginTimeout() {
        String tout = System.getProperty(
                KNIMEConstants.KNIME_DATABASE_LOGIN_TIMEOUT);
        int timeout = 5;
        if (tout != null) {
            try {
                timeout = Integer.parseInt(tout);
                if (timeout <= 0) {
                    LOGGER.warn("Database login timeout not valid (<=0) '"
                        + tout + "', using default '" + LOGIN_TIMEOUT + "'.");
                }
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Database login timeout not valid '" + tout
                        + "', using default '5'.");
            }
        }
        LOGGER.info("Database login timeout: " + LOGIN_TIMEOUT + " sec.");
        DriverManager.setLoginTimeout(LOGIN_TIMEOUT);
        return timeout;
    }

    /**
     * DriverManager fetch size to chunk specified number of rows.
     */
    public static final Integer FETCH_SIZE = initFetchSize();
    private static Integer initFetchSize() {
        String fsize = System.getProperty(
                KNIMEConstants.KNIME_DATABASE_FETCHSIZE);
        if (fsize != null) {
            try {
                return Integer.parseInt(fsize);
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Database fetch size not valid '" + fsize
                        + "', no fetch size will be set.");
            }
        }
        return null;
    }

    private String m_driver;
    private String m_dbName;
    private String m_user = null;
    private String m_pass = null;

    /**
     * Create a default settings connection object.
     */
    public DatabaseConnectionSettings() {
        // init default driver with the first from the driver list
        // or use Java JDBC-ODBC as default
        String[] history = DRIVER_ORDER.getHistory();
        if (history != null && history.length > 0) {
            m_driver = history[0];
        } else {
            m_driver = DatabaseDriverLoader.JDBC_ODBC_DRIVER;
        }
        // create database name from driver class
        m_dbName = DatabaseDriverLoader.getURLForDriver(m_driver);
    }

    /**
     * Creates a new <code>DBConnection</code> based on the given connection
     * object.
     * @param conn connection used to copy settings from
     */
    public DatabaseConnectionSettings(final DatabaseConnectionSettings conn) {
        this();
        m_driver = conn.m_driver;
        m_dbName = conn.m_dbName;
        m_user   = conn.m_user;
        m_pass   = conn.m_pass;
    }

    /**
     * Create a database connection based on this settings.
     * @return a new database connection object.
     * @throws SQLException {@link SQLException}
     * @throws InvalidSettingsException {@link InvalidSettingsException}
     * @throws IllegalBlockSizeException {@link IllegalBlockSizeException}
     * @throws BadPaddingException {@link BadPaddingException}
     * @throws InvalidKeyException {@link InvalidKeyException}
     * @throws IOException {@link IOException}
     */
    public Connection createConnection()
            throws InvalidSettingsException, SQLException,
            BadPaddingException, IllegalBlockSizeException,
            InvalidKeyException, IOException {
        if (m_dbName == null || m_user == null || m_pass == null
                || m_driver == null) {
            throw new InvalidSettingsException("No settings available "
                    + "to create database connection.");
        }
        Driver d = DatabaseDriverLoader.registerDriver(m_driver);
        if (!d.acceptsURL(m_dbName)) {
            throw new InvalidSettingsException("Driver \"" + d
                    + "\" does not accept URL: " + m_dbName);
        }

        final String password = KnimeEncryption.decrypt(m_pass);
        final String user = m_user;
        final String dbName = m_dbName;

        Callable<Connection> callable = new Callable<Connection>() {
            /** {@inheritDoc} */
            @Override
            public Connection call() throws Exception {
                LOGGER.debug("Opening database connection to \""
                        + dbName + "\"...");
                return DriverManager.getConnection(dbName, user, password);
            }
        };
        Future<Connection> task = CONNECTION_CREATOR_EXECUTOR.submit(callable);
        try {
            return task.get(LOGIN_TIMEOUT + 1, TimeUnit.SECONDS);
        } catch (ExecutionException ee) {
            throw new SQLException(ee.getCause());
        } catch (Exception ie) {
            throw new SQLException(ie);
        }
    }

    /**
     * Save settings.
     * @param settings connection settings
     */
    public void saveConnection(final ConfigWO settings) {
        settings.addString("driver", m_driver);
        settings.addString("database", m_dbName);
        settings.addString("user", m_user);
        settings.addString("password", m_pass);
        final File driverFile =
            DatabaseDriverLoader.getDriverFileForDriverClass(m_driver);
        settings.addString("loaded_driver",
                (driverFile == null ? null : driverFile.getAbsolutePath()));
        DRIVER_ORDER.add(m_driver);
        DATABASE_URLS.add(m_dbName);
    }

    /**
     * Validate settings.
     * @param settings to validate
     * @throws InvalidSettingsException if the settings are not valid
     */
    public void validateConnection(final ConfigRO settings)
            throws InvalidSettingsException {
        loadConnection(settings, false);
    }

    /**
     * Load validated settings.
     * @param settings to load
     * @return true, if settings have changed
     * @throws InvalidSettingsException if settings are invalid
     */
    public boolean loadValidatedConnection(final ConfigRO settings)
            throws InvalidSettingsException {
        return loadConnection(settings, true);
    }

    private boolean loadConnection(final ConfigRO settings,
            final boolean write) throws InvalidSettingsException {
        if (settings == null) {
            throw new InvalidSettingsException(
                    "Connection settings not available!");
        }
        String driver = settings.getString("driver");
        String database = settings.getString("database");
        String user = settings.getString("user");
        // password
        String password = settings.getString("password", "");
        // write settings or skip it
        if (write) {
            m_driver = driver;
            DRIVER_ORDER.add(m_driver);
            boolean changed = false;
            if (m_user != null && m_dbName != null && m_pass != null) {
                if (!user.equals(m_user) || !database.equals(m_dbName)
                        || !password.equals(m_pass)) {
                    changed = true;
                }
            }
            m_dbName = database;
            DATABASE_URLS.add(m_dbName);
            m_user = user;
            m_pass = password;
            // loaded driver
            String loadedDriver = settings.getString("loaded_driver", null);
            if (loadedDriver != null) {
                try {
                    DatabaseDriverLoader.loadDriver(new File(loadedDriver));
                } catch (Throwable t) {
                    LOGGER.info("Could not load driver from file \""
                            + loadedDriver + "\"" + (t.getMessage() != null 
                                ? ", reason: " + t.getMessage() : "."));
                }
            }
            return changed;
        }
        return false;
    }

    /**
     * Execute statement on current database connection.
     * @param statement to be executed
     * @throws SQLException {@link SQLException}
     * @throws InvalidSettingsException {@link InvalidSettingsException}
     * @throws IllegalBlockSizeException {@link IllegalBlockSizeException}
     * @throws BadPaddingException {@link BadPaddingException}
     * @throws InvalidKeyException {@link InvalidKeyException}
     * @throws IOException {@link IOException}
     */
    public void execute(final String statement) throws InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException,
            InvalidSettingsException,
            SQLException, IOException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = createConnection();
            stmt = conn.createStatement();
            stmt.execute(statement);
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * Create connection model with all settings used to create a database
     * connection.
     * @return database connection model
     */
    public ModelContentRO createConnectionModel() {
        ModelContent cont = new ModelContent("database_connection_model");
        saveConnection(cont);
        return cont;
    }
    
    /**
     * @return database driver used to open the connection
     */
    public final String getDriver() {
        return m_driver;
    }
    
    /**
     * @return database name used to access the db URL
     */
    public final String getDBName() {
        return m_dbName;
    }
}
