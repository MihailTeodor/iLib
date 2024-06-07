package it.gurzu.swam.iLib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "movies_DVD")
public class MovieDVD extends Article {

	@NotNull
	private String director;
	@NotNull
	private String isan;

	MovieDVD() {	}
	
	public MovieDVD(String uuid) {
		super(uuid);
	}
	
	public String getDirector() {
		return director;
	}
	
	public void setDirector(String director) {
		this.director = director;
	}
	
	public String getIsan() {
		return isan;
	}
	
	public void setIsan(String isan) {
		this.isan = isan;
	}
	
}