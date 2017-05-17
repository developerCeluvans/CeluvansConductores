package com.imaginamos.taxisya.taxista.model;

import org.json.JSONObject;

public class Servicio {

    private String idServicio;
    private String indice;
    private String comp1;
    private String comp2;
    private String numero;
    private String barrio;
    private String obs;
    private String latitud;
    private String longitud;
    private String name;
    private String lastname;
    private int typeagend;
    private JSONObject schedule;
    private String rat;
    private String fecha;
    private int kind_id = -1;
    private String destino;
    private String houragend;
    private String direccion;
    private int payType;
    private String payReference;
    private String userId;
    private String userEmail;
    private String cardReference;
    private String units;
    private String charge1;
    private String charge2;
    private String charge3;
    private String charge4;
    private String value;


    public int getRand_id() {
        return kind_id;
    }

    public void setRand_id(int rand_id) {
        this.kind_id = rand_id;
    }

    public JSONObject getSchedule() {
        return schedule;
    }

    public void setSchedule(JSONObject schedule) {
        this.schedule = schedule;
    }

    public Servicio(String idServicio, String indice, String comp1,
                    String comp2, String numero, String barrio, String obs,
                    String latitud, String longitud, String name, String lastname,
                    int typeagend, String destino, String houragend, String direccion, int payType, String payReference, String userId, String userEmail, String cardReference,
                    String units, String charge1, String charge2, String charge3, String charge4, String value) {
        this.idServicio = idServicio;
        this.indice = indice;
        this.comp1 = comp1;
        this.comp2 = comp2;
        this.numero = numero;
        this.barrio = barrio;
        this.obs = obs;
        this.latitud = latitud;
        this.longitud = longitud;
        this.name = name;
        this.lastname = lastname;
        this.typeagend = typeagend;
        this.destino = destino;
        this.houragend = houragend;
        this.direccion = direccion;
        this.payType = payType;
        this.payReference = payReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.cardReference = cardReference;
        this.units = units;
        this.charge1 = charge1;
        this.charge2 = charge2;
        this.charge3 = charge3;
        this.charge4 = charge4;
        this.value = value;
    }

    public Servicio(String idServicio, String indice, String comp1,
                    String comp2, String numero, String barrio, String obs,
                    String latitud, String longitud, int typeagend, String name,
                    int kind_id, String destino, String houragend, String direccion,
                    int payType, String payReference, String userId, String userEmail, String cardReference,
                    String units, String charge1, String charge2, String charge3, String charge4, String value) {
        this.idServicio = idServicio;
        this.indice = indice;
        this.comp1 = comp1;
        this.comp2 = comp2;
        this.numero = numero;
        this.barrio = barrio;
        this.obs = obs;
        this.latitud = latitud;
        this.longitud = longitud;
        this.typeagend = typeagend;
        this.name = name;
        this.kind_id = kind_id;
        this.destino = destino;
        this.houragend = houragend;
        this.direccion = direccion;
        this.payType = payType;
        this.payReference = payReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.cardReference = cardReference;
        this.units = units;
        this.charge1 = charge1;
        this.charge2 = charge2;
        this.charge3 = charge3;
        this.charge4 = charge4;
        this.value = value;
    }

    public int getTypeagend() {
        return typeagend;
    }

    public void setTypeagend(int typeagend) {
        this.typeagend = typeagend;
    }

    public Servicio() {
        idServicio = "1";
        indice = "Calle";
        comp1 = "82B";
        comp2 = "95D";
        numero = "44";
        barrio = "Bochica";
        obs = "Apartamento";
        latitud = "4";
        longitud = "-4";
        name = "Didier";
        lastname = "Neira";
    }

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public String getIndice() {
        return indice;
    }

    public String getIndiceName() {
//	if (indice.equals("0")) {
//	    return "Calle";
//	} else if (indice.equals("1")) {
//	    return "Carrera";
//	} else if (indice.equals("2")) {
//	    return "Transversal";
//
//	} else if (indice.equals("3")) {
//	    return "Diagonal";
//	} else if (indice.equals("4")) {
//	    return "Avenida";
//	} else {
//	    return "Calle";
//	}
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

    public String getLatitud() {
        return (!latitud.equals("null")) ? latitud : "0";
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return (!longitud.equals("null")) ? longitud : "0";
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
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

    public String getRat() {
        return rat;
    }

    public void setRat(String rat) {
        this.rat = rat;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String barrio) {
        this.destino = destino;
    }

    public String getHora() {
        return houragend;
    }

    public void setHora(String houragend) {
        this.houragend = houragend;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public String getPayReference() {
        return payReference;
    }

    public void setPayReference(String payReference) {
        this.payReference = payReference;
    }


    public String getCardReference() {
        return cardReference;
    }

    public void setCardReference(String cardReference) {
        this.cardReference = cardReference;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getCharge1() {
        return charge1;
    }

    public void setCharge1(String charge1) {
        this.charge1 = charge1;
    }

    public String getCharge2() {
        return charge2;
    }

    public void setCharge2(String charge2) {
        this.charge2 = charge2;
    }

    public String getCharge3() {
        return charge3;
    }

    public void setCharge3(String charge3) {
        this.charge3 = charge3;
    }

    public String getCharge4() {
        return charge4;
    }

    public void setCharge4(String charge4) {
        this.charge4 = charge4;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
