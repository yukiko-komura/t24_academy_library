package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.BookMst;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface BookMstRepository extends JpaRepository<BookMst, Long> {
	List<BookMst> findAll();

	@Query("select b from BookMst b where b.deletedAt is null")
	List<BookMst> findAllDeletedAtIsNull();

	Optional<BookMst> findById(BigInteger id);
}
