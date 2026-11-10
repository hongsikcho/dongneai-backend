package com.dongneai.region.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRegionRepository extends JpaRepository<UserRegion, UserRegionId> {

    List<UserRegion> findByUserId(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from UserRegion ur where ur.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
