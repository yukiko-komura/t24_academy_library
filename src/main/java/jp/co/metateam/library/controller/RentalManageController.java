package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import java.util.List;
import jp.co.metateam.library.model.RentalManage;

import java.util.Date;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
            AccountService accountService,
            RentalManageService rentalManageService,
            StockService stockService) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * 
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出管理テーブルから全件取得
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に渡すデータをmodelに追加
        return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();
        // アカウントとストックテーブルから全件取得
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra) {
        try {
            Stock stock = this.stockService.findById(rentalManageDto.getStockId());
            // 貸出管理テーブルの入力された在庫管理番号に紐づく在庫テーブルのデータを持ってくる
            int stockStatus = stock.getStatus();
            String dateError = rentalManageDto.dateCheck();
            if (stockStatus == 1) {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は利用できません");
                result.addError(fieldError);
            }
            if (dateError != null){
                result.addError(new FieldError("rentalManageDto","expectedRentalOn",dateError));
            }
            String newStockId = rentalManageDto.getStockId();
            List<RentalManage> renatalManagelList = this.rentalManageService.findByStockIdAndStatus(newStockId);
            // 入力された在庫管理番号に紐づく貸出管理テーブルの貸出ステータス(01のみ)のデータを持ってくる
            if (renatalManagelList != null){
                for (RentalManage list : renatalManagelList) {
                if (list.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0 &&
                        rentalManageDto.getExpectedRentalOn().compareTo(list.getExpectedReturnOn()) <= 0) {
                        FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は利用できません");
                        result.addError(fieldError);
                }
            }
        }
            if (result.hasErrors()) {
            
                throw new Exception("Validation error.");
                }
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {

        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(id);
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setAccount(rentalManage.getAccount());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStock(rentalManage.getStock());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setStockId(rentalManage.getStock().getId());

            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {

                throw new Exception("Validation error.");
            }
            RentalManage rentalManage = this.rentalManageService.findById(id);
            int preStatus = rentalManage.getStatus();
            int newStatus = rentalManageDto.getStatus();
            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
            Date date = new Date();

            if (newStatus == 1 && date.before(expectedRentalOn)) {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "「貸出予定日」が貸出日より後の日付で変更できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            } else if (newStatus == 2 && date.before(expectedReturnOn)) {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "「返却予定日」が返却日より後の日付で変更できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            }

            else if (preStatus == 0 && newStatus == 2) {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "ステータスが「貸出待ち」の状態で「返却済み」は選択できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            } else if (preStatus == 1 && (newStatus == 0 || newStatus == 3)) {
                FieldError fieldError = new FieldError("rentalManageDto", "status",
                        "ステータスが「貸出中」の状態で「貸出待ち」/「キャンセル」は選択できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            } else if (preStatus == 2 && (newStatus == 0 || newStatus == 1 || newStatus == 3)) {
                FieldError fieldError = new FieldError("rentalManageDto", "status",
                        "ステータスが「返却済み」の状態で「貸出待ち」/「貸出中」/「キャンセル」は選択できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            } else if (preStatus == 3 && (newStatus == 0 || newStatus == 1 || newStatus == 2)) {
                FieldError fieldError = new FieldError("rentalManageDto", "status",
                        "ステータスが「キャンセル」の状態で「貸出待ち」/「貸出中」/「返却済み」は選択できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            }

            Stock stock = this.stockService.findById(rentalManageDto.getStockId());
            // 貸出管理の入力された在庫管理番号に紐づく在庫テーブルのデータを持ってくる
            int stockStatus = stock.getStatus();

            if (stockStatus == 1) {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は利用できません");
                result.addError(fieldError);

                throw new Exception("Validation error.");
            }
            String newStockId = rentalManageDto.getStockId();
            List<RentalManage> renatalManageList = this.rentalManageService.findByStockIdAndStatus(newStockId);
            // 入力された在庫管理番号に紐づく貸出管理テーブルの貸出ステータス(01のみ)のデータを持ってくる
            for (RentalManage list : renatalManageList) {
                if (list.getId() == rentalManageDto.getId()) {
                    continue;
                }
                if (list.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0 &&
                        rentalManageDto.getExpectedRentalOn().compareTo(list.getExpectedReturnOn()) <= 0) {
                    FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は利用できません");
                    result.addError(fieldError);

                    throw new Exception("Validation error.");
                }
            }
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return String.format("redirect:/rental/%s/edit", id);
        }
    }
}
