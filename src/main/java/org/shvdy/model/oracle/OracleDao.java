package org.shvdy.model.oracle;

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.shvdy.model.Dao;
import org.shvdy.model.OutdatedObjectException;
import org.shvdy.model.types.ModelObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

public class OracleDao<T extends ModelObject<T>> implements Dao<T> {
    private OracleDaoFactory oracleDaoFactory;
    private Supplier<T> defaultObjectConstructor;
    private Map<BigInteger, T> cache;
    private String typeName;

    private final static String selectByColumnString = "SELECT object.OBJECT_ID, param.PARAM_NAME, param.PARAM_VALUE_TYPE," +
            " param_desc.TEXT_VAL, param_desc.NUMBER_VAL, object.LAST_MODIFIED " +
            "      FROM OBJECT object " +
            "      INNER JOIN PARAMETER param ON param.TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) " +
            "      LEFT JOIN PARAMETER_DESC param_desc ON param_desc.PARAM_ID = param.PARAM_ID " +
            "      AND param_desc.OBJECT_ID = object.OBJECT_ID " +
            "WHERE object.OBJECT_ID = ? AND object.TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) ";

    private final static String selectByIdString = "SELECT object.OBJECT_ID, param.PARAM_NAME, param.PARAM_VALUE_TYPE," +
            " param_desc.TEXT_VAL, param_desc.NUMBER_VAL, object.LAST_MODIFIED " +
            "      FROM OBJECT object " +
            "      INNER JOIN PARAMETER param ON param.TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) " +
            "      LEFT JOIN PARAMETER_DESC param_desc ON param_desc.PARAM_ID = param.PARAM_ID " +
            "      AND param_desc.OBJECT_ID = object.OBJECT_ID " +
            "WHERE object.OBJECT_ID = ? AND object.TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) ";

    private final static String selectByTypeString = "SELECT object.OBJECT_ID, param.PARAM_NAME, param.PARAM_VALUE_TYPE, " +
            "param_desc.TEXT_VAL, param_desc.NUMBER_VAL, object.LAST_MODIFIED " +
            "      FROM OBJECT object " +
            "      INNER JOIN PARAMETER param ON param.TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) " +
            "      INNER JOIN PARAMETER_DESC param_desc ON param_desc.PARAM_ID = param.PARAM_ID " +
            "      AND param_desc.OBJECT_ID = object.OBJECT_ID " +
            "ORDER BY OBJECT_ID ";

    private final static String insertObjectString = "INSERT INTO OBJECT (OBJECT_ID, TYPE_ID, LAST_MODIFIED) VALUES " +
            "(?,  (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ? ) , ?) ";

    private final static String insertParameterDescString = "INSERT INTO PARAMETER_DESC( PARAM_ID, OBJECT_ID, TEXT_VAL, NUMBER_VAL) VALUES " +
            "((SELECT PARAM_ID FROM PARAMETER WHERE (TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ?) " +
            "AND PARAM_NAME = ?)), ?, ?, ?)";

    private final static String deleteString = "DELETE FROM OBJECT WHERE OBJECT_ID = ?";

    private final static String updateObjectLastModifiedString = "UPDATE OBJECT SET LAST_MODIFIED = ? WHERE OBJECT_ID = ?";

    private final static String updateNumValString = "UPDATE PARAMETER_DESC " +
            "SET NUMBER_VAL = ? " +
            "WHERE PARAM_ID = (SELECT PARAM_ID FROM PARAMETER WHERE  " +
            "    (TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ?) AND PARAM_NAME = ? AND PARAM_VALUE_TYPE = 'NUMBER')) " +
            "AND OBJECT_ID IN (SELECT OBJECT_ID FROM PARAMETER_DESC WHERE OBJECT_ID = ?)";

    private final static String updateTextValString = "UPDATE PARAMETER_DESC " +
            "SET TEXT_VAL = ? " +
            "WHERE PARAM_ID = (SELECT PARAM_ID FROM PARAMETER WHERE  " +
            "    (TYPE_ID = (SELECT TYPE_ID FROM TYPE WHERE TYPE_NAME = ?) AND PARAM_NAME = ? AND PARAM_VALUE_TYPE = 'TEXT')) " +
            "AND OBJECT_ID IN (SELECT OBJECT_ID FROM PARAMETER_DESC WHERE OBJECT_ID = ?)";

    private final static String selectLastModifiedById = "SELECT LAST_MODIFIED FROM OBJECT WHERE OBJECT_ID = ?";

    OracleDao(OracleDaoFactory factory, Supplier<T> defaultConstructor) {
        oracleDaoFactory = factory;
        this.defaultObjectConstructor = defaultConstructor;
        typeName = defaultConstructor.get().getClass().getSimpleName();
        cache = Collections.synchronizedMap(new LinkedHashMap<BigInteger, T>(20, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<BigInteger, T> eldest) {
                return size() > 40;
            }
        });
    }

    public Set<BigInteger> getCacheEntries() {
        return cache.keySet();
    }

    public T getFromCache(BigInteger id) {
        if (cache.containsKey(id)) {
            T object = cache.get(id);
            return object.getCopy();
        } else
            return null;
    }

    private void putInCache(T object, BigInteger newDateTime) throws OutdatedObjectException {
        if (cache.containsKey(object.getId())) {
            BigInteger cacheVersion = cache.get(object.getId()).getLastModifiedDate();
            if (cacheVersion.equals(object.getLastModifiedDate())) {
                if (newDateTime != null)
                    object.setLastModifiedDate(newDateTime);
                cache.replace(object.getId(), object);
            } else {
                throw new OutdatedObjectException("\nTried to update cache object from the outdated source object:" +
                        "\nID: " + object.getId() + "\nSource object version: " + object.getLastModifiedDate() +
                        "\nCache object version: " + cacheVersion);
            }
        } else
            cache.put(object.getId(), object);
    }

    private T getObjectFromParams(HashMap<String, Object> params, BigInteger id, BigInteger lastModified) {
        if (params != null) {
            T obj = defaultObjectConstructor.get();
            if (id != null)
                obj.setId(id);
            if (lastModified != null) {
                obj.setLastModifiedDate(lastModified);
            }
            obj.setParameters(params);
            return obj;
        } else
            return null;
    }

    private HashMap<String, Object> parseResultSetById(ResultSet queryResult, BigInteger id) throws SQLException {
        HashMap<String, Object> objectParams = new HashMap<>();
        while (queryResult.getBigDecimal("OBJECT_ID").equals(new BigDecimal(id))) {
            objectParams.put(queryResult.getString("PARAM_NAME").toLowerCase(),
                    queryResult.getObject(queryResult.getString("PARAM_VALUE_TYPE") + "_VAL"));
            if (!queryResult.next()) break;
        }
        return objectParams;
    }

//    public T findByColumn(String columnName) throws SQLException, ClassNotFoundException {
//        T resultObject = null;
//        try (Connection connection = oracleDaoFactory.openConnection();
//             PreparedStatement preparedQuery = connection.prepareStatement(selectByIdString)) {
//            preparedQuery.setString(1, typeName);
//            preparedQuery.setBigDecimal(2, new BigDecimal(id));
//            preparedQuery.setString(3, typeName);
//            ResultSet queryOutput = preparedQuery.executeQuery();
//            if (queryOutput.next()) {
//                BigInteger currentId = queryOutput.getBigDecimal("OBJECT_ID").toBigInteger();
//                BigInteger currentLastModified = queryOutput.getBigDecimal("LAST_MODIFIED").toBigInteger();
//                resultObject = getObjectFromParams(parseResultSetById(queryOutput, currentId), currentId, currentLastModified);
//                putInCache(resultObject, null);
//            }
//        } catch (OutdatedObjectException e) {
//            System.out.println("Tried to write in cache outdated object in findById");
//        }
//        return resultObject;
//    }

    public T findById(BigInteger id) throws SQLException, ClassNotFoundException {
        T resultObject = null;
        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedQuery = connection.prepareStatement(selectByIdString)) {
            preparedQuery.setString(1, typeName);
            preparedQuery.setBigDecimal(2, new BigDecimal(id));
            preparedQuery.setString(3, typeName);
            ResultSet queryOutput = preparedQuery.executeQuery();
            if (queryOutput.next()) {
                BigInteger currentId = queryOutput.getBigDecimal("OBJECT_ID").toBigInteger();
                BigInteger currentLastModified = queryOutput.getBigDecimal("LAST_MODIFIED").toBigInteger();
                resultObject = getObjectFromParams(parseResultSetById(queryOutput, currentId), currentId, currentLastModified);
                putInCache(resultObject, null);
            }
        } catch (OutdatedObjectException e) {
            System.out.println("Tried to write in cache outdated object in findById");
        }
        return resultObject;
    }

    public List<T> findByType() throws SQLException, ClassNotFoundException {
        ObservableList<T> objects = FXCollections.observableArrayList();

        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedQuery = connection.prepareStatement(selectByTypeString)) {
            preparedQuery.setString(1, typeName);
            ResultSet queryOutput = preparedQuery.executeQuery();
            if (queryOutput.next()) {
                while (!queryOutput.isAfterLast()) {
                    BigInteger currentId = queryOutput.getBigDecimal("OBJECT_ID").toBigInteger();
                    BigInteger currentLastModified = queryOutput.getBigDecimal("LAST_MODIFIED").toBigInteger();
                    T currentObject = getObjectFromParams(parseResultSetById(queryOutput, currentId), currentId, currentLastModified);
                    objects.add(currentObject);
                    putInCache(currentObject, null);
                }
            }
        } catch (OutdatedObjectException e) {
            System.out.println("Tried to write in cache outdated object in findByType");
        }
        return objects;
    }

    public void insert(T newObject) throws SQLException, ClassNotFoundException {
        try {
            putInCache(newObject, null);
        } catch (OutdatedObjectException e) {
            System.out.println("Tried to write in cache outdated object in insert");
        }
        HashMap<String, Property> parameters = newObject.getParameters();

        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedInsertObject = connection.prepareStatement(insertObjectString);
             PreparedStatement preparedInsertParamDesc = connection.prepareStatement(insertParameterDescString)) {

            preparedInsertObject.setBigDecimal(1, new BigDecimal(newObject.getId()));
            preparedInsertObject.setString(2, typeName);
            preparedInsertObject.setBigDecimal(3, new BigDecimal(newObject.getLastModifiedDate()));
            preparedInsertObject.executeUpdate();

            for (String paramName : parameters.keySet()) {
                preparedInsertParamDesc.setString(1, typeName);
                preparedInsertParamDesc.setString(2, paramName.toUpperCase());
                preparedInsertParamDesc.setBigDecimal(3, new BigDecimal(newObject.getId()));
                Object paramValue = parameters.get(paramName).getValue();
                if (paramValue.getClass().equals(String.class)) {
                    preparedInsertParamDesc.setString(4, (String) paramValue);
                    preparedInsertParamDesc.setNull(5, 3);
                } else {
                    preparedInsertParamDesc.setNull(4, 12);
                    preparedInsertParamDesc.setInt(5, (int) paramValue);
                }
                preparedInsertParamDesc.executeUpdate();
            }
        }
    }

    public void delete(T object) throws SQLException, ClassNotFoundException {
        cache.remove(object.getId());
        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedDelete = connection.prepareStatement(deleteString)) {
            preparedDelete.setBigDecimal(1, new BigDecimal(object.getId()));
            preparedDelete.executeUpdate();
        }
    }

    public void update(T object) throws SQLException, ClassNotFoundException, OutdatedObjectException {
        HashMap<String, Property> parameters = object.getParameters();

        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedQuery = connection.prepareStatement(selectLastModifiedById)) {
            preparedQuery.setBigDecimal(1, new BigDecimal(object.getId()));
            ResultSet queryOutput = preparedQuery.executeQuery();
            queryOutput.next();
            BigInteger dbLastModified = queryOutput.getBigDecimal("LAST_MODIFIED").toBigInteger();
            if (!dbLastModified.equals(object.getLastModifiedDate())) {
                throw new OutdatedObjectException("\nTried to update database object from the outdated source object:" +
                        "\nID: " + object.getId() + "\nSource object version: " + object.getLastModifiedDate() +
                        "\nDatabase object version: " + dbLastModified);
            }
        }

        BigInteger updateDateTime = new BigInteger(LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMuuHHmmssSSS")));
        try (Connection connection = oracleDaoFactory.openConnection();
             PreparedStatement preparedLastModifiedUpdate = connection.prepareStatement(updateObjectLastModifiedString);
             PreparedStatement preparedNumUpdate = connection.prepareStatement(updateNumValString);
             PreparedStatement preparedTextUpdate = connection.prepareStatement(updateTextValString)) {
            preparedLastModifiedUpdate.setBigDecimal(1, new BigDecimal(updateDateTime));
            preparedLastModifiedUpdate.setBigDecimal(2, new BigDecimal(object.getId()));
            preparedLastModifiedUpdate.executeUpdate();
            for (String paramName : parameters.keySet()) {
                if (parameters.get(paramName).getValue().getClass().equals(String.class)) {
                    preparedTextUpdate.setString(1, (String) parameters.get(paramName).getValue());
                    preparedTextUpdate.setString(2, typeName);
                    preparedTextUpdate.setString(3, paramName.toUpperCase());
                    preparedTextUpdate.setBigDecimal(4, new BigDecimal(object.getId()));
                    preparedTextUpdate.executeUpdate();
                } else {
                    preparedNumUpdate.setInt(1, (int) parameters.get(paramName).getValue());
                    preparedNumUpdate.setString(2, typeName);
                    preparedNumUpdate.setString(3, paramName.toUpperCase());
                    preparedNumUpdate.setBigDecimal(4, new BigDecimal(object.getId()));
                    preparedNumUpdate.executeUpdate();
                }
            }
        }
        putInCache(object, updateDateTime);
    }
}
