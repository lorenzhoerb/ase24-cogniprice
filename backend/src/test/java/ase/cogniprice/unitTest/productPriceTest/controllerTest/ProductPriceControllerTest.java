package ase.cogniprice.unitTest.productPriceTest.controllerTest;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.ProductPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductPriceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductPriceService productPriceService;

    private List<PriceDto> priceDtoList;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        priceDtoList = new ArrayList<>();
        priceDtoList.add(new PriceDto(1L, new BigDecimal("99.99"), "Competitor A", "USD",
                LocalDateTime.of(2024, 12, 1, 14, 30)));
        priceDtoList.add(new PriceDto(1L, new BigDecimal("95.50"), "Competitor A", "USD",
                LocalDateTime.of(2024, 12, 2, 14, 30)));
        priceDtoList.add(new PriceDto(1L, new BigDecimal("101.75"), "Competitor B", "USD",
                LocalDateTime.of(2024, 12, 1, 14, 30)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPriceTrends_ShouldReturnPriceTrends() throws Exception {
        Long productId = 1L;

        when(productPriceService.getProductPricesByProductIt(productId)).thenReturn(priceDtoList);

        mockMvc.perform(get("/api/productPrices/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(priceDtoList)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPriceTrends_ShouldReturnNotFound_WhenNoPrices() throws Exception {
        Long productId = 2L;

        when(productPriceService.getProductPricesByProductIt(productId))
                .thenThrow(new EntityNotFoundException("No price trends found for productId: " + productId));

        mockMvc.perform(get("/api/productPrices/{productId}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No price trends found for productId: " + productId));
    }

    @Test
    void getPriceTrends_ShouldReturnForbidden_WhenUserNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/productPrices/{productId}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
