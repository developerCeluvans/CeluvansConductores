package com.imaginamos.taxisya.taxista.model;

/**
 * Created by leo on 2/9/16.
 */
public class City {
    private int id;
    private String name;
    private int department_id;

    public City(int _id, String _name, int _department_id) {
        id = _id;
        name = _name;
        department_id = _department_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public String toString()
    {
        return( name );
    }
}
