package org.shvdy.model.oracle;

import org.shvdy.model.DaoFactory;
import org.shvdy.model.types.ModelObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

public class OracleDaoFactory implements DaoFactory {
    public final static String URL = "jdbc:oracle:thin:hpmeta/password@localhost:1521:xe";
    public final static String DRIVER = "oracle.jdbc.driver.OracleDriver";

    public <T extends ModelObject<T>> OracleDao<T> getDao(Supplier<T> supplier) {
        return new OracleDao<>(this, supplier);
    }

    public Connection openConnection() throws ClassNotFoundException, SQLException {
        Connection conn;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Oracle JDBC driver is missing.\n" + e);
            throw e;
        } catch (SQLException e) {
            System.out.println("Failed to connect to database.\n" + e);
            throw e;
        }
        return conn;
    }

}

//    public ResultSet executeQuery(String queryStmt) throws SQLException, ClassNotFoundException {
//        CachedRowSetImpl queryResult;
//
//        System.out.println("Executing:\n" + queryStmt + "\n");
//        try (Connection connection = openConnection()) {
//            try (Statement statement = connection.createStatement();
//                 ResultSet resultSet = statement.executeQuery(queryStmt)) {
//                queryResult = new CachedRowSetImpl();
//                queryResult.populate(resultSet);
//                return queryResult;
//                //return resultSet;
//            } catch (SQLException e) {
//                System.out.println("SQL select query failed.\n" + e);
//                throw e;
//            }
//        }
//    }
//
//    public int executeUpdate(String sqlStmt) throws SQLException, ClassNotFoundException {
//        System.out.println("Executing:\n" + sqlStmt + "\n");
//        try (Connection connection = openConnection()) {
//            try (Statement statement = connection.createStatement()) {
//                return statement.executeUpdate(sqlStmt);
//            } catch (SQLException e) {
//                System.out.println("SQL update query failed.\n" + e);
//                throw e;
//            }
//        }
//    }

//    private OracleDataSource pooledDataSource;

//    public OracleDataSource getPooledDataSource() {
//        return pooledDataSource;
//    }

//    public void deployPooledDataSource(){
//        try {
//            OracleConnectionPoolDataSource ocpds = new OracleConnectionPoolDataSource();
//            ocpds.setServerName("DB_Server");
//            ocpds.setDatabaseName("Metamodel");
//            ocpds.setDescription("Connection pooling for Metamodel DBMS");
//            ocpds.setURL(connURL);
//            Context ctx = new InitialContext();
//            ctx.bind("jdbc/pool/MetamodelDB", ocpds);
//
//            OracleDataSource ods = new OracleDataSource();
//            ods.setDescription("produces pooled connections to Metamodel");
//            ods.setDataSourceName("jdbc/pool/MetamodelDB");
//            ctx.bind("jdbc/MetamodelDB", ods);
//
//            pooledDataSource = ods;
//
//        } catch (SQLException e){
//            System.out.println("Failed to connect to database.");
//            e.printStackTrace();
//        } catch (NamingException e) {
//            System.out.println("Failed to register JNDI name.");
//            e.printStackTrace();
//        }