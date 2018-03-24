package org.shvdy.model;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface Dao<T> {

    List<T> findByType() throws SQLException, ClassNotFoundException;

    T findById(BigInteger id) throws SQLException, ClassNotFoundException;

    void insert(T newObject) throws SQLException, ClassNotFoundException;

    void update(T object) throws SQLException, ClassNotFoundException, OutdatedObjectException;

    void delete(T object) throws SQLException, ClassNotFoundException;

    Set<BigInteger> getCacheEntries();

    T getFromCache(BigInteger id);
}
