package org.geektimes.projects.user;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 */
public class JNDITest {

    public void getDatasource() throws NamingException, SQLException {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/UserPlatformDB");
        Connection conn = ds.getConnection();
        System.out.println(conn.isClosed());
    }
}
