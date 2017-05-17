package com.imaginamos.taxisya.taxista.model;

/**
 * Created by leo on 2/9/16.
 */
public class Department {
    private int id;
    private String name;
    private int country_id;

    public Department(int _id, String _name, int _country_id) {
        id = _id;
        name = _name;
        country_id = _country_id;
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

    public int getCountry_id() {
        return country_id;
    }

    public void setCountry_id(int country_id) {
        this.country_id = country_id;
    }

    public String toString()
    {
        return( name );
    }
}
