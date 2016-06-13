package dj.poc.bitcoinj.service;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by proy on 13/06/2016.
 */
@Slf4j
@Service
public class WalletSvc {
    private Wallet wallet;
    private File walletFile;

    @Autowired
    public WalletSvc(Wallet wallet, File walletFile) {
        this.wallet = wallet;
        this.walletFile = walletFile;
    }

    public boolean add(ECKey ecKey) {
        return wallet.addKey(ecKey);
    }

    public boolean check(ECKey ecKey) {
        return wallet.isPubKeyHashMine(ecKey.getPubKeyHash());
    }

    public ECKey get(int idx) {
        return wallet.getKeys().get(idx);
    }

    public List<ECKey> get() {
        return wallet.getKeys();
    }

    @PreDestroy
    public void preDestroy() {
        try {
            this.wallet.saveToFile(walletFile);
        } catch (IOException e) {
            log.error("Unable to save the wallet", e);
        }
    }
}
