package com.sankalp.companyservice.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@SQLDelete(sql = "UPDATE company SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String name;
	
	private long turnover;
	
	private String ceo;
	
	private String brief;
	
	private String bod;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CompanyStatus status = CompanyStatus.PENDING;

	@Column(name = "is_deleted", nullable = false)
	private boolean deleted = false;
	
	@ManyToOne
	private Sector sector;

	public Company(int id, String name, long turnover, String ceo, String brief, Sector sector) {
		super();
		this.id = id;
		this.name = name;
		this.turnover = turnover;
		this.ceo = ceo;
		this.brief = brief;
		this.sector = sector;
	}
	
}
