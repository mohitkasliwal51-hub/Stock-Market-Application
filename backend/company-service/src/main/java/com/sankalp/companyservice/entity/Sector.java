package com.sankalp.companyservice.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sector {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String name;
	
	private String brief;
	
	@OneToMany(mappedBy = "sector")
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<Company> companies;

	public Sector(int id, String name, String brief) {
		super();
		this.id = id;
		this.setName(name);
		this.setBrief(brief);
	}
	
}
