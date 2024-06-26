package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

  List<Stock> findAll();

  List<Stock> findByDeletedAtIsNull();

  List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

  Optional<Stock> findById(String id);

  List<Stock> findByBookMstIdAndStatus(Long book_id, Integer status);

  @Query("select s from Stock s where s.bookMst.title = ?1 and s.status = 0 and s.deletedAt is null")
  List<Stock> findByBookMstIdAndAvailableStatus(String title);

  @Query("SELECT DISTINCT s " +
      "FROM Stock s " +
      "LEFT OUTER JOIN RentalManage rm ON s.id = rm.stock.id " +
      "WHERE ?1 BETWEEN rm.expectedRentalOn AND rm.expectedReturnOn " +
      "AND s.bookMst.title = ?2 " +
      "AND s.status = 0 " +
      "AND deletedAt IS null")
  List<Stock> lendableBook(Date choiceDate, String title);
}