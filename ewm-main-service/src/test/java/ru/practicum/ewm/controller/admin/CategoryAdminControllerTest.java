package ru.practicum.ewm.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
public class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        newCategoryDto = NewCategoryDto.builder()
                .name("New Test Category")
                .build();
    }

    @Test
    void createCategory_ShouldReturnCreatedStatus() throws Exception {
        when(categoryService.addNewCategory(any(NewCategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));
    }

    @Test
    void deleteCategory_ShouldReturnNoContentStatus() throws Exception {
        doNothing().when(categoryService).deleteCategoryById(anyLong());

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCategory_ShouldReturnOkStatus() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));
    }
}