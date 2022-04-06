package com.x0.sbootplayground.rest;

import com.x0.sbootplayground.data.Wallet;
import com.x0.sbootplayground.data.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController {

    @Autowired
    private WalletRepository repo;

    @GetMapping
    public List<Wallet> getWallets()
    {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @PostMapping
    public Long create(@RequestParam String name, @RequestParam String surname)
            throws RequestParamException
    {
        if (name.isBlank()) {
            throw new RequestParamException("name", "cannot be empty");
        }
        if (surname.isBlank()) {
            throw new RequestParamException("surname", "cannot be empty");
        }

        Wallet wallet = new Wallet(name, surname);
        repo.save(wallet);
        return wallet.getId();
    }

    @GetMapping("{wallet}")
    public float getBalance(@PathVariable long wallet)
    {
        return getWallet(wallet).getBalance();
    }

    @Transactional()
    @PutMapping("{walletId}/topup")
    public float addMoney(@PathVariable long walletId, @RequestParam float amount)
            throws RequestParamException
    {
        if (amount <= 0) {
            throw new RequestParamException("amount", "should be a positive number");
        }

        Wallet wallet = getWallet(walletId);
        wallet.setBalance(wallet.getBalance() + amount);
        repo.save(wallet);

        return wallet.getBalance();
    }

    @Transactional()
    @PutMapping("{walletId}/withdraw")
    public float withdrawMoney(@PathVariable long walletId, @RequestParam float amount)
            throws RequestParamException
    {
        if (amount <= 0) {
            throw new RequestParamException("amount", "should be a positive number");
        }

        Wallet wallet = getWallet(walletId);
        if (wallet.getBalance() < amount) {
            throw new ValidationException("Not enough funds");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        repo.save(wallet);

        return wallet.getBalance();
    }

    @Transactional()
    @PutMapping("{walletId}/transfer/{targetId}")
    public float transferMoney(@PathVariable long walletId, @PathVariable long targetId, @RequestParam float amount)
            throws RequestParamException
    {
        if (walletId == targetId) {
            throw new ValidationException("Target wallet cannot be a source one");
        }
        if (amount <= 0) {
            throw new RequestParamException("amount", "should be a positive number");
        }

        Wallet fromWallet = getWallet(walletId);
        if (fromWallet.getBalance() < amount) {
            throw new ValidationException("Not enough funds");
        }
        Wallet toWallet = getWallet(targetId);

        fromWallet.setBalance(fromWallet.getBalance() - amount);
        toWallet.setBalance(toWallet.getBalance() + amount);

        repo.saveAll(Arrays.asList(fromWallet, toWallet));

        return fromWallet.getBalance();
    }

    private Wallet getWallet(long walletId)
    {
        return repo.findById(walletId).orElseThrow(() -> new BadRequestException("Invalid wallet id"));
    }

    @ExceptionHandler({ RequestParamException.class })
    private ResponseEntity<Object> handleParamException(RequestParamException ex, WebRequest req) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ValidationException.class })
    private ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest req) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

class RequestParamException extends Exception {

    private final String paramName;
    private final String validationMessage;

    public RequestParamException(String param, String validationMessage) {
        super(String.format("Parameter '%s' %s", param, validationMessage));
        this.paramName = param;
        this.validationMessage = validationMessage;
    }

    public String getParamName() {
        return paramName;
    }

    public String getValidationMessage() {
        return validationMessage;
    }
}
