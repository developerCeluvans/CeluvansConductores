package com.imaginamos.taxisya.taxista.model;

import java.util.Random;

/**
 * Clase simple para obtener los datos de un rectángulo
 * 
 * @author Miguel Ángel
 */

public class Rectangulo {

	private int base;
	private int altura;
	
	
	
	/**
	 * El constructor crea rectángulos aleatorios
	 */
	public Rectangulo() {
		Random random = new Random();
		base = random.nextInt(50) + 1;
		altura = random.nextInt(50) + 1;
	}


	public int getBase() {
		return base;
	}


	public int getAltura() {
		return altura;
	}
	
	
	public int getArea() {
		return altura*base;
	}
	
	public int getPerimetro() {
		return altura*2 + base*2;
	}
	
	
	
}
