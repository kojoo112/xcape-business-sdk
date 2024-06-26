package com.chadev.xcape.core.repository;

import java.util.List;
import java.util.Optional;

import com.chadev.xcape.core.domain.entity.Hint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HintRepository extends JpaRepository<Hint, Long> {

	List<Hint> findAllByThemeId(Long themeId);

	Optional<Hint> findByCode(String code);
}
