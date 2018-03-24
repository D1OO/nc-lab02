package org.shvdy.model.types;

import javafx.beans.property.Property;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;

public abstract class ModelObject<T extends ModelObject> {
    private BigInteger id;
    private BigInteger lastModifiedDate;
    protected HashMap<String, Property> properties = new HashMap<>();

    protected ModelObject() {
        lastModifiedDate = new BigInteger(LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMuuHHmmssSSS")));

        id = new BigInteger(3 + lastModifiedDate.toString() +
                String.format("%02d", new Random().nextInt(99) + 1));
    }

    protected ModelObject(ModelObject original) {
        this.id = new BigInteger(original.id.toString());
        this.lastModifiedDate = new BigInteger(original.lastModifiedDate.toString());
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(BigInteger lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        for (String paramName : parameters.keySet()) {
            properties.get(paramName).setValue(parameters.get(paramName));
        }
    }

    public void setParameterByName(String propertyName, Object value) {
        properties.get(propertyName).setValue(value);
    }

    public HashMap<String, Property> getParameters() {
        return properties;
    }

    public abstract T getCopy();
}
