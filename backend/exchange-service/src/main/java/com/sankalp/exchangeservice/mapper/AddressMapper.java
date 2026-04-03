package com.sankalp.exchangeservice.mapper;

import com.sankalp.exchangeservice.dto.AddressDto;
import com.sankalp.exchangeservice.entity.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddressMapper {
    
    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        return dto;
    }

    public Address toEntity(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        Address address = new Address();
        address.setId(dto.getId());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setZipCode(dto.getZipCode());
        return address;
    }

    public List<AddressDto> toDtoList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
