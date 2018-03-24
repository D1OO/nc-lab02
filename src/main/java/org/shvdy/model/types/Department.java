package org.shvdy.model.types;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.math.BigInteger;
import java.util.HashMap;

public class Department extends ModelObject<Department> {
    private SimpleIntegerProperty deptno = new SimpleIntegerProperty(this, "deptno");
    private SimpleStringProperty dname = new SimpleStringProperty(this, "dname");
    private SimpleStringProperty loc = new SimpleStringProperty(this, "loc");
    private SimpleMapProperty<BigInteger, Employee> employees;

    public int getDeptno() {
        return deptno.get();
    }

    public void setDeptno(int deptno) {
        this.deptno.set(deptno);
    }

    public String getDname() {
        return dname.get();
    }

    public void setDname(String dname) {
        this.dname.set(dname);
    }

    public String getLoc() {
        return loc.get();
    }

    public void setLoc(String loc) {
        this.loc.set(loc);
    }

    public Object getEmployees() {
        ObservableMap m = FXCollections.observableHashMap();
        return employees.get();
    }

    public void setEmployees(ObservableMap<BigInteger, Employee> employees) {
        this.employees.set(employees);
    }

    public Department() {
        super();
        fillProperties();
    }

    public Department(Department original) {
        super(original);
        fillProperties();
        HashMap<String, Property> originalParameters = original.getParameters();
        for (String paramName : originalParameters.keySet()) {
            properties.get(paramName).setValue(originalParameters.get(paramName).getValue());
        }
    }

    @Override
    public Department getCopy() {
        return new Department(this);
    }

    private void fillProperties(){
        properties.put(deptno.getName(), deptno);
        properties.put(dname.getName(), dname);
        properties.put(loc.getName(), loc);
        properties.put(employees.getName(), employees);
    }
}
