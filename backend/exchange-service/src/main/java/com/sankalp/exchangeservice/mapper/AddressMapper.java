package com.sankalp.exchangeservice.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;

import com.sankalp.exchangeservice.dto.AddressDto;
import com.sankalp.exchangeservice.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

	AddressDto toDto(Address address);

	Address toEntity(AddressDto dto);

	default List<AddressDto> toDtoList(List<Address> addresses) {
		if (addresses == null) {
			return Collections.emptyList();
		}
		return addresses.stream().map(this::toDto).toList();
	}
}
