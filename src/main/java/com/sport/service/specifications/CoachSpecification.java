package com.sport.service.specifications;

import com.sport.service.entities.Coach;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.mappers.string.SexStringMapper;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.web.models.coach.CoachFilter;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public interface CoachSpecification {

    static Specification<Coach> withFilter(CoachFilter filter) {
        return Specification.where(byCoachSportTypes(filter.getSportTypes()))
                .and(byCoachAge(filter.getAge()))
                .and(byCoachSex(filter.getSex()))
                .and(byCoachYearsOfExperience(filter.getYearsOfExperience()));
    }

    static Specification<Coach> byCoachSportTypes(List<String> sportTypesStrings) {
        if (sportTypesStrings == null || sportTypesStrings.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        List<SportType> sportTypes =
                SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(sportTypesStrings);

        return (root, query, cb) -> {
            query.distinct(true);
            return root.join("sportTypes").in(sportTypes);
        };
    }


    static Specification<Coach> byCoachAge(Integer age) {
        if (age == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(root.get("age"), age);
    }

    static Specification<Coach> byCoachSex(String sexString) {
        if (sexString == null || sexString.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        Sex sex = SexStringMapper.sexStringToSexEnum(sexString);
        return (root, query, cb) -> cb.equal(root.get("sex"), sex);
    }

    static Specification<Coach> byCoachYearsOfExperience(Integer yearsOfExperience) {
        if (yearsOfExperience == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(root.get("yearsOfExperience"), yearsOfExperience);
    }
}