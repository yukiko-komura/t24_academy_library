package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;

import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public List<Stock> findByBookMstIdAndAvailableStatus(Long newBookId) {
        return this.stockRepository.findByBookMstIdAndAvailableStatus(newBookId);
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            // idと同じフィールドにあるレコードを全件取得
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<List<String>> generateValues(Integer year, Integer month, Integer daysInMonth) {
        
        List<BookMst> books = this.bookMstRepository.findAllDeletedAtIsNull();
        List<List<String>> bigValues = new ArrayList<>();

        for (BookMst booklist : books) {
            Long newBookId = booklist.getId();
            List<Stock> availableStocks = this.stockRepository.findByBookMstIdAndAvailableStatus(newBookId);
            List<String> values = new ArrayList<>();
            values.add(booklist.getTitle());
            int stocks = availableStocks.size();
            values.add(String.valueOf(stocks));
            // カウントする文を書く
            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate day = LocalDate.of(year,month,i);
                Date newDate = Date.valueOf(day);
                   
                Integer rentallingcount = this.rentalManageRepository
                            .findByRentallingDateAndStatus(newDate, availableStocks);
                Integer rentalwaitcount = this.rentalManageRepository
                            .findByRentalwaitDateAndStatus(newDate, availableStocks);

                String stockCount = String.valueOf(stocks-rentallingcount-rentalwaitcount);                
                if (stockCount.equals("0") ){
                    stockCount = "✕";
                }
                values.add(stockCount);
            }
            bigValues.add(values);
        }
        return bigValues; 
    }
}


/*
 * String[] stockNum = {"1", "2", "3", "4", "×"};
 * Random rnd = new Random();
 * List<String> values = new ArrayList<>();
 * values.add("スッキリわかるJava入門 第4版"); // 対象の書籍名
 * values.add("10"); // 対象書籍の在庫総数
 * 
 * for (int i = 1; i <= daysInMonth; i++) {
 * int index = rnd.nextInt(stockNum.length);
 * values.add(stockNum[index]);
 * }
 * return values;
 * }
 * }
 */