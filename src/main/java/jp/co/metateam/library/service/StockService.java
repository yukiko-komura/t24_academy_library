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
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.service.RentalManageService;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository, RentalManageService rentalManageService) {
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
    public Integer findByRentalwaitDateAndStatus(Date newDate, List<String> availableStockId) {
        return this.rentalManageRepository.findByRentalwaitDateAndStatus(newDate, availableStockId);
    }

    @Transactional
    public Integer findByRentallingDateAndStatus(Date newDate, List<String> availableStockId) {
        List<RentalManage> unavailableStockLists = this.rentalManageRepository.findByRentallingDateAndStatus(newDate,
                availableStockId);
        Integer unavailableStockNum = unavailableStockLists.size();

        return unavailableStockLists.size();
    }

    @Transactional
    public List<Stock> lendableBook(Date choiceDate, String title) {
        return this.stockRepository.lendableBook(choiceDate, title);
    }

    @Transactional
    public List<Stock> findByBookMstIdAndAvailableStatus(String title) {
        return this.stockRepository.findByBookMstIdAndAvailableStatus(title);
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
        

        for (BookMst bookList : books) {
            String title = bookList.getTitle();
            List<Stock> availableStocks = this.stockRepository.findByBookMstIdAndAvailableStatus(title);
            List<String> values = new ArrayList<>();
            values.add(bookList.getTitle());
            int stocks = availableStocks.size();
            values.add(String.valueOf(stocks));
            
            List<String> availableStockId = new ArrayList<>();
            for (Stock stockList : availableStocks) {
                availableStockId.add(stockList.getId());
            }

            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate day = LocalDate.of(year, month, i);
                Date newDate = Date.valueOf(day);

                Integer rentallingcount = findByRentallingDateAndStatus(newDate, availableStockId);
                Integer rentalwaitcount = findByRentalwaitDateAndStatus(newDate, availableStockId);

                String stockCount = String.valueOf(stocks - rentallingcount - rentalwaitcount);
                if (stockCount.equals("0")) {
                    stockCount = "✕";
                }
                values.add(stockCount);
            }
            bigValues.add(values);
        }
        return bigValues;
    }

    public List<Stock> availableStockValues(java.sql.Date choiceDate, String title) {

        List<Stock> availableList = lendableBook(choiceDate, title);
        List<Stock> StockAvailable = this.findByBookMstIdAndAvailableStatus(title);

        StockAvailable.removeAll(availableList);

        return StockAvailable;
    }
}
