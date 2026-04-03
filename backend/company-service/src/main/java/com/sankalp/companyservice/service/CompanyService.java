package com.sankalp.companyservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.repository.CompanyRepository;
import com.sankalp.companyservice.repository.IpoRepository;
import com.sankalp.companyservice.repository.StockRepository;

@Service
public class CompanyService {
	
	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private IpoRepository ipoRepository;
	
	public List<Company> getAllCompanies(){
		return companyRepository.findAll();
	}
	
	public Company getCompanyById(int id){
		Optional<Company> companyOptional = companyRepository.findById(id);
		if(companyOptional.isPresent()) {
			return companyOptional.get();
		}
		return null;
	}
	
	public Company createCompany(Company company) {
		return companyRepository.save(company);
	}

	public Company updateCompany(int id, Company company) {
		Optional<Company> companyOptional = companyRepository.findById(id);
		if(companyOptional.isPresent()) {
			company.setId(id);
			return companyRepository.save(company);
		}
		return null;
	}
	
	public DeleteCompanyResult deactivateCompany(int id) {
		Company companyToDelete = getCompanyById(id);
		if(companyToDelete == null) {
			return new DeleteCompanyResult(
					DeleteCompanyResult.Status.NOT_FOUND,
					null,
					"Company not found with id: " + id);
		}

		boolean hasStocks = stockRepository.existsByCompanyId(id);
		boolean hasIpo = ipoRepository.existsByCompanyId(id);
		if (hasStocks || hasIpo) {
			return new DeleteCompanyResult(
					DeleteCompanyResult.Status.BLOCKED_BY_DEPENDENCIES,
					companyToDelete,
					"Delete blocked: company is referenced by stock/IPO records");
		}

		companyRepository.delete(companyToDelete);
		return new DeleteCompanyResult(
				DeleteCompanyResult.Status.DELETED,
				companyToDelete,
				"Company deactivated successfully");
	}
	
	public List<Company> getCompanyByPattern(String pattern){
		return companyRepository.findByNameContainingIgnoreCase(pattern);
	}
	
	public List<Company> getCompanyByStockExchangeId(int id){
		return companyRepository.findCompanyByExchangeId(id);
	}
	
}
