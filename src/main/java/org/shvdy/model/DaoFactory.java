package org.shvdy.model;

import org.shvdy.model.oracle.OracleDaoFactory;
import org.shvdy.model.types.ModelObject;

import java.util.function.Supplier;

public interface DaoFactory {

    enum Implementations {ORACLE}

    <T extends ModelObject<T>> Dao<T> getDao(Supplier<T> supplier);

    static DaoFactory getDaoFactory(Implementations impl) {
        switch (impl) {
            case ORACLE:
                return new OracleDaoFactory();
            default:
                return null;
        }
    }
}
