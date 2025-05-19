package ru.practicum.ewm.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        newCategoryDto = NewCategoryDto.builder()
                .name("New Test Category")
                .build();
    }

    @Test
    void getCategories_ShouldReturnCategoryDtoList() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(categoryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(category)));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category.getId(), result.get(0).getId());
        assertEquals(category.getName(), result.get(0).getName());
        verify(categoryRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategoryDto() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(1L));
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void addNewCategory_ShouldReturnCategoryDto() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.addNewCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteCategoryById_WhenCategoryExistsAndNoEvents_ShouldDeleteCategory() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(eventRepository.findByCategory(any(Category.class))).thenReturn(Collections.emptyList());
        doNothing().when(categoryRepository).deleteById(anyLong());

        categoryService.deleteCategoryById(1L);

        verify(categoryRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findByCategory(category);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategoryById_WhenCategoryHasEvents_ShouldThrowConflictException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(eventRepository.findByCategory(any(Category.class))).thenReturn(List.of(new Event()));

        assertThrows(ConflictException.class, () -> categoryService.deleteCategoryById(1L));
        verify(categoryRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findByCategory(category);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateCategory_WhenCategoryExistsAndNameUnique_ShouldReturnUpdatedCategoryDto() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals(updatedCategory.getId(), result.getId());
        assertEquals(updatedCategory.getName(), result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Updated Category");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenNameAlreadyExists_ShouldThrowConflictException() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Existing Category")
                .build();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.updateCategory(1L, updateDto));
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Existing Category");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenNameIsTheSame_ShouldNotCheckUniqueness() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(1L, updateDto));
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}