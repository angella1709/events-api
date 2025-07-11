package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.Category;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.web.dto.CreateEventRequest;
import com.example.angella.eventsapi.web.dto.EventDto;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMapper.class, UserMapper.class}
)
public interface EventMapper {

    @Mapping(target = "schedule.description", source = "schedule")
    @Mapping(target = "location.city", source = "cityLocation")
    @Mapping(target = "location.street", source = "streetLocation")
    @Mapping(target = "creator.id", source = "creatorId")
    Event toEntity(CreateEventRequest request);

    @Mapping(target = "schedule.description", source = "schedule")
    Event toEntity(UpdateEventRequest request);

    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "creator", source = "creator")
    EventDto toDto(Event event);

    List<EventDto> toDtoList(List<Event> events);

    @IterableMapping(qualifiedByName = "mapToCategory")
    Set<Category> mapToCategories(Set<String> categories);

    @Named("mapToCategory")
    default Category mapToCategory(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        Category category = new Category();
        category.setName(categoryName);
        return category;
    }
}