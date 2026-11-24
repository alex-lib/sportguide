package com.sport.service.web.models.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListPlaceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PlaceResponse> list;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public ListPlaceResponse(List<PlaceResponse> list) {
        this.list = list;
    }
}
