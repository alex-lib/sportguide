package com.sport.service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.place.ListPlaceResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

class PlacePaginationTest {
    private final PlaceRepository repository = mock(PlaceRepository.class);
    private final PlaceMapper mapper = mock(PlaceMapper.class);
    private final PlaceService service =
            new PlaceService(
                    repository,
                    mapper,
                    mock(MinioService.class),
                    mock(ApplicationEventPublisher.class));

    @Test
    void paginatedRequestLimitsDatabaseQueryAndPreservesFiltersAndTotals() {
        List<Place> content = List.of(Place.builder().id(41L).name("Зал").build());
        when(repository.findWithFilters(
                        eq(District.CENTRALNYY),
                        isNull(),
                        eq(false),
                        eq(PlaceType.GYM),
                        eq("зал"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(2, 20), 41));
        when(mapper.listPlaceToListPlaceResponse(content))
                .thenReturn(new ListPlaceResponse(List.of()));
        var result =
                service.findPage("CENTRALNYY", "ALL_SUBDISTRICTS", "false", "GYM", " ЗАЛ ", 2, 20);
        var paging = ArgumentCaptor.forClass(Pageable.class);
        verify(repository)
                .findWithFilters(
                        eq(District.CENTRALNYY),
                        isNull(),
                        eq(false),
                        eq(PlaceType.GYM),
                        eq("зал"),
                        paging.capture());
        assertEquals(40, paging.getValue().getOffset());
        assertEquals(20, paging.getValue().getPageSize());
        assertTrue(paging.getValue().getSort().getOrderFor("id").isAscending());
        assertEquals(2, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(41L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        verifyNoMoreInteractions(repository);
        verify(mapper).listPlaceToListPlaceResponse(content);
    }

    @Test
    void mapAndLegacyRequestsStillReturnAllFilteredPlaces() {
        when(repository.findWithFilters(null, null, null, PlaceType.MARTIAL_ARTS_HALL, null))
                .thenReturn(List.of());
        when(mapper.listPlaceToListPlaceResponse(List.of()))
                .thenReturn(new ListPlaceResponse(List.of()));
        var result =
                service.findAll(
                        "ALL_DISTRICTS", "ALL_SUBDISTRICTS", null, "MARTIAL_ARTS_HALL", null);
        assertNull(result.getPage());
        verify(repository).findWithFilters(null, null, null, PlaceType.MARTIAL_ARTS_HALL, null);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void invalidPaginationCannotReachDatabase() {
        for (int[] args : new int[][] {{-1, 20}, {0, 0}, {0, 101}}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.findPage(null, null, null, null, null, args[0], args[1]));
        }
        verifyNoInteractions(repository, mapper);
    }
}
