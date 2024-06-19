package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    //select * from rental_manage;
    List<RentalManage> findAll();

    @Query("select r from RentalManage r where r.stock.id = ?1 and r.status in (0,1)")
    List<RentalManage> findByStockIdAndStatus(String newStockId);
    //在庫管理番号を引数にして紐づける

    @Query("select count(rm) from RentalManage rm where ?1 >=rm.rentaledAt and ?1 <=rm.expectedReturnOn and rm.status = 1 and rm.stock in ?2")
    Integer findByRentallingDateAndStatus(Date newDate,List<Stock> availableStocks);

    @Query("select count(rm) from RentalManage rm where ?1 >=rm.expectedRentalOn and ?1 <=rm.expectedReturnOn and rm.status = 0 and rm.stock in ?2")
    Integer findByRentalwaitDateAndStatus(Date newDate,List<Stock> availableStocks);

    //select * from rental_manage where id = id;
	Optional<RentalManage> findById(Long id);
}
