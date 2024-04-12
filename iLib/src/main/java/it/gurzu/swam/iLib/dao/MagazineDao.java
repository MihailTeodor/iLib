package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.Magazine;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MagazineDao extends BaseDao<Magazine> {
	
	public MagazineDao() {
		super(Magazine.class);
	}
	
	public List<Magazine> findMagazinesByIssn(String issn) {
		return this.em.createQuery("FROM Magazine where issn = :issn", Magazine.class)
				.setParameter("issn", issn)
				.getResultList();		
	}


}
