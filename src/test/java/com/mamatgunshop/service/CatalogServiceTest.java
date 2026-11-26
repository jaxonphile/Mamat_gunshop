package com.mamatgunshop.service;

import com.mamatgunshop.model.FilterCriteria;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.model.SortOption;
import com.mamatgunshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogServiceTest {

    @TempDir
    Path tempDir;

    private CatalogService service;

    @BeforeEach
    void setUp() {
        ProductRepository repository = new ProductRepository(tempDir.resolve("products.json"));
        repository.save(TestFixtures.product("p1", "Glock 17", "Glock", GunType.PISTOL, "9mm", 17, "6800000", 10));
        repository.save(TestFixtures.product("p2", "Glock 19X", "Glock", GunType.PISTOL, "9mm", 19, "7200000", 0));
        repository.save(TestFixtures.product("p3", "Colt 1911", "Colt", GunType.PISTOL, ".45 ACP", 7, "8900000", 5));
        repository.save(TestFixtures.product("p4", "Garuda R56", "Garuda", GunType.RIFLE, "5.56mm", 30, "14000000", 3));
        repository.save(TestFixtures.product("p5", "Remington 870", "Remington", GunType.SHOTGUN, "12 gauge", 7, "9800000", 2));
        service = new CatalogService(repository);
    }

    @Test
    void filterBySingleCaliber() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.getCalibers().add("9mm");

        List<Product> result = service.search(criteria);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getCaliber().equals("9mm")));
    }

    @Test
    void multipleCalibersAreCombinedWithOr() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.getCalibers().add("9mm");
        criteria.getCalibers().add(".45 ACP");

        assertEquals(3, service.search(criteria).size());
    }

    @Test
    void differentCriteriaAreCombinedWithAnd() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.getCalibers().add("9mm");
        criteria.getBrands().add("Glock");

        List<Product> result = service.search(criteria);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getBrand().equals("Glock")));
    }

    @Test
    void keywordSearchIsCaseInsensitive() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setKeyword("GLOCK");

        assertEquals(2, service.search(criteria).size());
    }

    @Test
    void keywordSearchAlsoMatchesDescription() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setKeyword("Deskripsi Garuda R56");

        assertEquals(1, service.search(criteria).size());
    }

    @Test
    void priceRangeFiltersCorrectly() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setMinPrice(new BigDecimal("7000000"));
        criteria.setMaxPrice(new BigDecimal("9000000"));

        List<Product> result = service.search(criteria);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Glock 19X")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Colt 1911")));
    }

    @Test
    void inStockOnlyExcludesZeroStock() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setInStockOnly(true);

        List<Product> result = service.search(criteria);

        assertEquals(4, result.size());
        assertTrue(result.stream().noneMatch(p -> p.getName().equals("Glock 19X")));
    }

    @Test
    void sortByPriceAscending() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setSort(SortOption.PRICE_ASC);

        List<Product> result = service.search(criteria);

        assertEquals("Glock 17", result.get(0).getName());
        assertEquals("Garuda R56", result.get(4).getName());
    }

    @Test
    void sortByPriceDescending() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setSort(SortOption.PRICE_DESC);

        assertEquals("Garuda R56", service.search(criteria).get(0).getName());
    }

    @Test
    void emptyCriteriaReturnsEverything() {
        assertEquals(5, service.search(new FilterCriteria()).size());
    }

    @Test
    void availableOptionsAreDistinctAndSorted() {
        assertEquals(List.of(".45 ACP", "12 gauge", "5.56mm", "9mm"), service.availableCalibers());
        assertEquals(List.of(GunType.PISTOL, GunType.RIFLE, GunType.SHOTGUN), service.availableTypes());
        assertEquals(4, service.availableBrands().size());
    }
}
