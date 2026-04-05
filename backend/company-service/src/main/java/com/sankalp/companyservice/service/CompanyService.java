package com.sankalp.companyservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.entity.CompanyStatus;
import com.sankalp.companyservice.entity.Sector;
import com.sankalp.companyservice.repository.CompanyRepository;
import com.sankalp.companyservice.repository.IpoRepository;
import com.sankalp.companyservice.repository.SectorRepository;
import com.sankalp.companyservice.repository.StockRepository;

@Service
public class CompanyService {
	
	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private IpoRepository ipoRepository;

	@Autowired
	private SectorRepository sectorRepository;
	
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
		if (company.getStatus() == null) {
			company.setStatus(CompanyStatus.PENDING);
		}
		company.setSector(resolveSector(company.getSector()));
		return companyRepository.save(company);
	}

	public Company updateCompany(int id, Company company) {
		Optional<Company> companyOptional = companyRepository.findById(id);
		if(companyOptional.isPresent()) {
			Company existingCompany = companyOptional.get();
			existingCompany.setName(company.getName());
			existingCompany.setCeo(company.getCeo());
			existingCompany.setBod(company.getBod());
			existingCompany.setBrief(company.getBrief());
			existingCompany.setTurnover(company.getTurnover());
			if (company.getStatus() != null) {
				existingCompany.setStatus(company.getStatus());
			}
			Sector resolvedSector = resolveSector(company.getSector());
			existingCompany.setSector(resolvedSector != null ? resolvedSector : existingCompany.getSector());
			return companyRepository.save(existingCompany);
		}
		return null;
	}

	private Sector resolveSector(Sector requestedSector) {
		if (requestedSector == null || requestedSector.getId() <= 0) {
			return null;
		}
		return sectorRepository.findById(requestedSector.getId()).orElse(null);
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

		companyToDelete.setStatus(CompanyStatus.DEACTIVATED);
		companyRepository.save(companyToDelete);
		return new DeleteCompanyResult(
				DeleteCompanyResult.Status.DELETED,
				companyToDelete,
				"Company deactivated successfully");
	}

	public List<Company> getPendingApprovalCompanies() {
		return companyRepository.findByStatus(CompanyStatus.PENDING);
	}

	public Company updateCompanyStatus(int id, CompanyStatus status) {
		Optional<Company> companyOptional = companyRepository.findById(id);
		if (companyOptional.isEmpty()) {
			return null;
		}

		Company company = companyOptional.get();
		if (!isValidTransition(company.getStatus(), status)) {
			return null;
		}

		company.setStatus(status);
		return companyRepository.save(company);
	}

	private boolean isValidTransition(CompanyStatus currentStatus, CompanyStatus targetStatus) {
		if (targetStatus == null || currentStatus == targetStatus) {
			return false;
		}

		if (currentStatus == CompanyStatus.BANNED) {
			return false;
		}

		if (currentStatus == CompanyStatus.PENDING) {
			return targetStatus == CompanyStatus.APPROVED || targetStatus == CompanyStatus.REJECTED;
		}

		if (currentStatus == CompanyStatus.REJECTED) {
			return targetStatus == CompanyStatus.PENDING;
		}

		if (currentStatus == CompanyStatus.APPROVED) {
			return targetStatus == CompanyStatus.SUSPENDED
					|| targetStatus == CompanyStatus.DEACTIVATED
					|| targetStatus == CompanyStatus.BANNED;
		}

		if (currentStatus == CompanyStatus.SUSPENDED) {
			return targetStatus == CompanyStatus.APPROVED
					|| targetStatus == CompanyStatus.DEACTIVATED
					|| targetStatus == CompanyStatus.BANNED;
		}

		if (currentStatus == CompanyStatus.DEACTIVATED) {
			return targetStatus == CompanyStatus.APPROVED || targetStatus == CompanyStatus.BANNED;
		}

		return false;
	}
	
	public List<Company> getCompanyByPattern(String pattern){
		return companyRepository.findByNameContainingIgnoreCase(pattern);
	}
	
	public List<Company> getCompanyByStockExchangeId(int id){
		return companyRepository.findCompanyByExchangeId(id);
	}
	
}
