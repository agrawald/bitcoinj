package dj.poc.bitcoinj;

import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.MemoryBlockStore;
import dj.poc.bitcoinj.service.WalletSvc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by proy on 13/06/2016.
 */
@Slf4j
@SpringBootApplication
public class Application {
    private static String NETWORK = "test";

    @Bean
    NetworkParameters testNetworkParameters() {
        return TestNet3Params.get();
    }

    @Bean
    ECKey ecKey() {
        ECKey ecKey = new ECKey();
        log.info("We created a key: \n" + ecKey);
        return ecKey;
    }

    @Bean
    Address address(ECKey ecKey, NetworkParameters networkParameters) {
        Address addressFromKey = ecKey.toAddress(networkParameters);
        log.info("On the " + NETWORK + " network, we can use this address: \n" + addressFromKey);
        return addressFromKey;
    }

    @Bean
    File walletFile(@Value("${wallet.file:test.wallet}") String walletFile) {
        return new File(walletFile);
    }

    @Bean
    Wallet wallet(NetworkParameters networkParameters) {
        Wallet wallet = new Wallet(networkParameters);
        wallet.addEventListener(new WalletEventListener() {
            public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                log.info("onCoinsReceived");
            }

            public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                log.info("onCoinsSent");
            }

            public void onReorganize(Wallet wallet) {
                log.info("onReorganize");
            }

            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                log.info("onTransactionConfidenceChanged");

            }

            public void onWalletChanged(Wallet wallet) {
                log.info("onWalletChanged");

            }

            public void onKeysAdded(Wallet wallet, List<ECKey> keys) {
                log.info("onKeysAdded");

            }

            public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
                log.info("onScriptsAdded");
            }
        });
        return wallet;
    }

    @Bean
    BlockStore blockStore(NetworkParameters networkParameters) {
        return new MemoryBlockStore(networkParameters);
    }

    @Bean
    BlockChain blockChain(NetworkParameters networkParameters, BlockStore blockStore) throws BlockStoreException {
        return new BlockChain(networkParameters, blockStore);
    }

    @Bean
    PeerAddress peerAddress() throws UnknownHostException {
        return new PeerAddress(InetAddress.getLocalHost());
    }

    @Bean
    Peer peer(NetworkParameters networkParameters, BlockChain chain, PeerAddress peerAddress,
              @Value("${bitcoin.software.name:test}") String softwareName,
              @Value("bitcoin.software.version") String version) {
        return new Peer(networkParameters, chain, peerAddress, softwareName, version);
    }

    @Bean
    PeerGroup peerGroup(NetworkParameters networkParameters,
                        BlockChain blockChain,
                        Wallet wallet,
                        @Value("${bitcoin.software.name:test}") String name,
                        @Value("bitcoin.software.version") String version) {
        PeerGroup peerGroup = new PeerGroup(networkParameters, blockChain);
        peerGroup.setUserAgent(name, version);
        peerGroup.addWallet(wallet);
        peerGroup.addPeerDiscovery(new DnsDiscovery(networkParameters));
        peerGroup.addEventListener(new AbstractPeerEventListener() {
            @Override
            public void onPeerConnected(Peer peer, int peerCount) {
                super.onPeerConnected(peer, peerCount);
                log.info(">>>> Peer connected: {}->{}", peerCount, peer);
            }
        });
        return peerGroup;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpringApplication.run(Application.class, args);
    }
}
