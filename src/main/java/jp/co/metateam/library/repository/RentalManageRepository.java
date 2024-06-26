package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jp.co.metateam.library.model.Stock;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    //select * from rental_manage;
    List<RentalManage> findAll();

    @Query("select r from RentalManage r where r.stock.id = ?1 and r.status in (0,1)")
    List<RentalManage> findByStockIdAndStatus(String newStockId);
    //在庫管理番号を引数にして紐づける

    @Query(value="SELECT * FROM rental_manage as rm WHERE CAST(rm.rentaled_At as date) <= :newDate and :newDate <= rm.expected_return_on AND rm.stock_id IN (:availableStocks) AND rm.status = 1", nativeQuery = true)
    List<RentalManage> findByRentallingDateAndStatus(@Param("newDate")Date newDate,@Param("availableStocks") List<String> availableStockId);

    @Query("select count(rm) from RentalManage rm where ?1 >=rm.expectedRentalOn and ?1 <=rm.expectedReturnOn and rm.status = 0 and rm.stock.id in ?2")
    Integer findByRentalwaitDateAndStatus(Date newDate,List<String> availableStockId);

    //select * from rental_manage where id = id;
	Optional<RentalManage> findById(Long id);
}
