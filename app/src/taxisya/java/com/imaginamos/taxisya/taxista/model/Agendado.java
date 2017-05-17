package com.imaginamos.taxisya.taxista.model;

public class Agendado {

    private String idService;
    private String idUsuario;
    private String fecha;
    private String tipo;
    private String indice;
    private String comp1;
    private String comp2;
    private String numero;
    private String barrio;
    private String obs;
    private String estado;
    private String name;
    private String lastname;

    public Agendado(String idService, String idUsuario, String fecha, String tipo, String indice, String comp1, String comp2, String numero, String barrio, String obs, String estado, String name, String lastname) {
        this.idService = idService;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
        this.tipo = tipo;
        this.indice = indice;
        this.comp1 = comp1;
        this.comp2 = comp2;
        this.numero = numero;
        this.barrio = barrio;
        this.obs = obs;
        this.estado = estado;
        this.name = name;
        this.lastname = lastname;
    }

    public String getIdService() {
        return idService;
    }

    public void setIdService(String idService) {
        this.idService = idService;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }

    public String getComp1() {
        return comp1;
    }

    public void setComp1(String comp1) {
        this.comp1 = comp1;
    }

    public String getComp2() {
        return comp2;
    }

    public void setComp2(String comp2) {
        this.comp2 = comp2;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

}
