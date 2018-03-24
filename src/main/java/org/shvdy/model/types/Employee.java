package org.shvdy.model.types;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;

public class Employee extends ModelObject<Employee> {
    private SimpleIntegerProperty empno = new SimpleIntegerProperty(this, "empno");
    private SimpleStringProperty ename = new SimpleStringProperty(this, "ename");
    private SimpleStringProperty job = new SimpleStringProperty(this, "job");
    private SimpleIntegerProperty mgr = new SimpleIntegerProperty(this, "mgr");
    private SimpleStringProperty hiredate = new SimpleStringProperty(this, "hiredate");
    private SimpleIntegerProperty sal = new SimpleIntegerProperty(this, "sal");
    private SimpleIntegerProperty comm = new SimpleIntegerProperty(this, "comm");
    private SimpleIntegerProperty deptno = new SimpleIntegerProperty(this, "deptno");

    public int getEmpno() {
        return empno.get();
    }

    public void setEmpno(int empno) {
        this.empno.set(empno);
    }

    public String getEname() {
        return ename.get();
    }

    public void setEname(String ename) {
        this.ename.set(ename);
    }

    public String getJob() {
        return job.get();
    }

    public void setJob(String job) {
        this.job.set(job);
    }

    public int getMgr() {
        return mgr.get();
    }

    public void setMgr(int mgr) {
        this.mgr.set(mgr);
    }

    public String getHiredate() {
        return hiredate.get();
    }

    public void setHiredate(String hiredate) {
        this.hiredate.set(hiredate);
    }

    public int getSal() {
        return sal.get();
    }

    public void setSal(int sal) {
        this.sal.set(sal);
    }

    public int getComm() {
        return comm.get();
    }

    public void setComm(int comm) {
        this.comm.set(comm);
    }

    public int getDeptno() {
        return deptno.get();
    }

    public void setDeptno(int dptnumber) {
        this.deptno.set(dptnumber);
    }

    public Employee() {
        super();
        fillProperties();
    }

    public Employee(Employee original){
        super(original);
        fillProperties();
        HashMap<String, Property> originalParameters = original.getParameters();
        for (String paramName : originalParameters.keySet()) {
            properties.get(paramName).setValue(originalParameters.get(paramName).getValue());
        }
    }

    private void fillProperties(){
        properties.put(empno.getName(), empno);
        properties.put(ename.getName(), ename);
        properties.put(job.getName(), job);
        properties.put(mgr.getName(), mgr);
        properties.put(hiredate.getName(), hiredate);
        properties.put(sal.getName(), sal);
        properties.put(comm.getName(), comm);
        properties.put(deptno.getName(), deptno);
    }

    public Employee getCopy(){
        return new Employee(this);
    }
}
