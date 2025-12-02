package com.sport.service.specifications;

import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.mappers.string.DistrictStringMapper;
import com.sport.service.mappers.string.OutdoorStringMapper;
import com.sport.service.mappers.string.PlaceTypeStringMapper;
import com.sport.service.mappers.string.SubDistrictStringMapper;
import com.sport.service.web.models.place.PlaceFilter;
import org.springframework.data.jpa.domain.Specification;

public interface PlaceSpecification {

    static Specification<Place> withFilter(PlaceFilter filter) {
        return Specification.where(byPlaceDistrict(filter.getDistrict()))
                .and(byPlaceSubDistrict(filter.getSubDistrict()))
                .and(byPlaceOutdoor(filter.getOutdoor()))
                .and(byPlaceType(filter.getPlaceType()));
    }

    static Specification<Place> byPlaceDistrict(String districtString) {
        if (districtString == null || districtString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        District district = DistrictStringMapper.districtStringToDistrictEnum(districtString);
        return (root, query, cb) -> {
            if (district == District.ALL_DISTRICTS) {
                return cb.conjunction();
            }
            return cb.equal(root.get("district"), district);
        };
    }

    static Specification<Place> byPlaceSubDistrict(String subDistrictString) {
        if (subDistrictString == null || subDistrictString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        SubDistrict subDistrict = SubDistrictStringMapper.subDistrictStringToSubDistrictEnum(subDistrictString);
        return (root, query, cb) -> {
            if (subDistrict == SubDistrict.ALL_SUBDISTRICTS) {
                return cb.conjunction();
            }
            return cb.equal(root.get("subDistrict"), subDistrict);
        };
    }

    static Specification<Place> byPlaceOutdoor(String outdoorString) {
        if (outdoorString == null || outdoorString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        Boolean outdoor = OutdoorStringMapper.outdoorStringToOutdoorEnum(outdoorString);
        return (root, query, cb) -> cb.equal(root.get("outdoor"), outdoor);
    }

    static Specification<Place> byPlaceType(String placeTypeString) {
        if (placeTypeString == null || placeTypeString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        PlaceType placeType = PlaceTypeStringMapper.placeTypeStringToPlaceTypeEnum(placeTypeString);
        return (root, query, cb) -> {
            if (placeType == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("placeType"), placeType);
        };
    }
}