package com.sport.service.specifications;

import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;
import com.sport.service.mappers.DistrictStringMapper;
import com.sport.service.web.models.event.EventFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public interface EventSpecification {

    static Specification<Event> withFilter(EventFilter filter) {
        return Specification.where(byEventDistrict(filter.getDistrict()))
                .and(byEventDate(filter.getDate()));
    }

    static Specification<Event> byEventDistrict(String districtString) {
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

    static Specification<Event> byEventDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        LocalDate date = LocalDate.parse(dateString);
        return (root, query, cb) -> cb.equal(root.get("date"), date);
    }
}