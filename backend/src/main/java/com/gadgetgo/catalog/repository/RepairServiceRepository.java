package com.gadgetgo.catalog.repository;

import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.domain.RepairType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairServiceRepository extends JpaRepository<RepairService, Long> {

    @EntityGraph(attributePaths = "category")
    Optional<RepairService> findBySlugAndActiveTrueAndCategory_ActiveTrue(String slug);

    @Query("""
            select distinct service.repairType
            from RepairService service
            join service.category category
            where service.active = true and category.active = true
            order by service.repairType
            """)
    List<RepairType> findActiveRepairTypes();

    @Query(value = """
            select service.*
            from repair_services service
            join device_categories category on category.id = service.category_id
            where service.active = true
              and category.active = true
              and (cast(:categorySlug as varchar) is null or category.slug = :categorySlug)
              and (cast(:repairType as varchar) is null or service.repair_type = :repairType)
              and (
                  cast(:queryText as varchar) is null
                  or service.search_vector @@ plainto_tsquery('simple', :queryText)
                  or to_tsvector('simple', coalesce(category.name, '') || ' ' || coalesce(category.description, ''))
                      @@ plainto_tsquery('simple', :queryText)
              )
            order by
                case when :sortOption = 'name' then lower(service.name) end asc,
                case when :sortOption = 'name-desc' then lower(service.name) end desc,
                case when :sortOption = 'type' then service.repair_type end asc,
                case when :sortOption = 'newest' then service.created_at end desc,
                case when :sortOption = 'oldest' then service.created_at end asc,
                service.id asc
            """,
            countQuery = """
                    select count(*)
                    from repair_services service
                    join device_categories category on category.id = service.category_id
                    where service.active = true
                      and category.active = true
                      and (cast(:categorySlug as varchar) is null or category.slug = :categorySlug)
                      and (cast(:repairType as varchar) is null or service.repair_type = :repairType)
                      and (
                          cast(:queryText as varchar) is null
                          or service.search_vector @@ plainto_tsquery('simple', :queryText)
                          or to_tsvector('simple', coalesce(category.name, '') || ' ' || coalesce(category.description, ''))
                              @@ plainto_tsquery('simple', :queryText)
                      )
                    """,
            nativeQuery = true)
    Page<RepairService> findPublicServices(
            @Param("categorySlug") String categorySlug,
            @Param("repairType") String repairType,
            @Param("queryText") String queryText,
            @Param("sortOption") String sortOption,
            Pageable pageable);
}
