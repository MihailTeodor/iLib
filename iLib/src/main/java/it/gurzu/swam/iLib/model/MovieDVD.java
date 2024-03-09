package it.gurzu.swam.iLib.model;

public class MovieDVD extends Article {

	private String director;
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