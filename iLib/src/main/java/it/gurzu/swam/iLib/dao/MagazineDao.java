package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.Magazine;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MagazineDao extends BaseDao<Magazine> {
	
	public MagazineDao() {
		super(Magazine.class);
	}
	
	public List<Magazine> findMagazineByIssueNumber(String issueNumber) {
		return this.em.createQuery("FROM Magazine where issueNumber = :issueNumber", Magazine.class)
				.setParameter("issueNumber", issueNumber)
				.getResultList();		
	}

	public List<Magazine> findMagazineByIssn(String issn) {
		return this.em.createQuery("FROM Magazine where issn = :issn", Magazine.class)
				.setParameter("issn", issn)
				.getResultList();		
	}


}
