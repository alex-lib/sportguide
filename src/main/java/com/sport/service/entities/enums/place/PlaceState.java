package com.sport.service.entities.enums.place;

import com.sport.service.entities.enums.common.District;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceState {
	private District district;
	private SubDistrict subDistrict;
	private PlaceType placeType;
	private Boolean outdoor;
	private String name;
	private String address;
	private String description;
	private String website;
	private byte[] photo;
	private String coordinates;
	private CreatePlaceStep step;
}
