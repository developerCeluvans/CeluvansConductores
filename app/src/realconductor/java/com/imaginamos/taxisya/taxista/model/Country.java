package com.imaginamos.taxisya.taxista.model;

/**
 * Created by leo on 2/9/16.
 */
public class Country {
    private int id;
    private String name;

    public Country(int _id, String _name) {
        id = _id;
        name = _name;
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

    public String toString()
    {
        return( name );
    }

}
