package com.sport.service.specifications;

import com.sport.service.entities.JointTraining;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.mappers.string.DistrictStringMapper;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.web.models.joint_training.JointTrainingFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public interface JointTrainingSpecification {

    static Specification<JointTraining> withFilter(JointTrainingFilter filter) {
        return Specification.where(byJointTrainingDistrict(filter.getDistrict()))
                .and(byJointTrainingDate(filter.getDate()))
                .and(byJointTrainingSportType(filter.getSportType()));
    }

    static Specification<JointTraining> byJointTrainingSportType(List<String> sportTypesStrings) {
        if (sportTypesStrings == null || sportTypesStrings.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        List<SportType> sportTypes = SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(sportTypesStrings);
        return (root, query, cb) -> root.get("sportType").in(sportTypes);
    }

    static Specification<JointTraining> byJointTrainingDistrict(String districtString) {
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

    static Specification<JointTraining> byJointTrainingDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        LocalDate date = LocalDate.parse(dateString);
        return (root, query, cb) -> cb.equal(root.get("date"), date);
    }
}
