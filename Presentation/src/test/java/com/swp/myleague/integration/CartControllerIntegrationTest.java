package com.swp.myleague.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.repo.ProductRepo;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.generate-ddl=true",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop",
        "spring.sql.init.mode=always",
        "spring.sql.init.platform=h2"
})
@Sql(scripts = "/schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class CartControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepo productRepo;

    @Test
    void postCart_withRealRepo_addsItemToSessionCart() throws Exception {
        Product p = new Product();
        p.setProductName("Integration Product");
        p.setProductDescription("desc");
        p.setProductPrice(12.5);
        p.setProductAmount(100);
        p.setProductImgPath("/img.png");
        Product saved = productRepo.save(p);
        UUID id = saved.getProductId();

        MockHttpSession session = new MockHttpSession();
        MvcResult mvcResult = mockMvc.perform(post("/cart").param("productId", id.toString()).param("productAmount", "2").session(session))
                .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        assertThat(cart).isNotNull();
        assertThat(cart.containsKey(id.toString())).isTrue();
        assertThat(cart.get(id.toString()).getProductAmount()).isEqualTo(2);
        // verify product persisted and linked
        assertThat(cart.get(id.toString()).getProduct()).isNotNull();
        assertThat(cart.get(id.toString()).getProduct().getProductId()).isEqualTo(id);
    }

    @Test
    void getIp_withRealRepo_incrementsQuantity() throws Exception {
        Product p = new Product();
        p.setProductName("Integration Inc");
        p.setProductDescription("desc");
        p.setProductPrice(5.0);
        p.setProductAmount(50);
        p.setProductImgPath("/img2.png");
        Product saved = productRepo.save(p);
        UUID id = saved.getProductId();

        MockHttpSession session = new MockHttpSession();
        // first increase
        MvcResult mvcResult = mockMvc.perform(get("/cart/ip").param("productId", id.toString()).session(session))
                .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        assertThat(cart).isNotNull();
        assertThat(cart.get(id.toString()).getProductAmount()).isEqualTo(1);

        // call increase again using same session
        MvcResult mvcResult2 = mockMvc.perform(get("/cart/ip").param("productId", id.toString()).session(session))
                .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart2 = (Map<String, CartItem>) session.getAttribute("cart");
        assertThat(cart2.get(id.toString()).getProductAmount()).isEqualTo(2);
    }
}
