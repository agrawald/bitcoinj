package dj.poc.bitcoinj;

import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.MemoryBlockStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by proy on 13/06/2016.
 */
@Slf4j
@SpringBootApplication
public class SenderApplication {
    private static final Sha256Hash BLOCK_HASH = new Sha256Hash
            ("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008");

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
    File walletFile(@Value("${wallet.file:test2.wallet}") String walletFile) {
        return new File(walletFile);
    }

    @Bean
    Wallet wallet(NetworkParameters networkParameters) {
        return new Wallet(networkParameters);
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
              @Value("${bitcoin.software.name:sender}") String softwareName,
              @Value("bitcoin.software.version") String version) {
        return new Peer(networkParameters, chain, peerAddress, softwareName, version);
    }

    @Bean
    Address recipientAddress(NetworkParameters networkParameters) throws AddressFormatException {
        return new Address(networkParameters, "musr98QU6XzxtP3JEz1DJqfAbSi1E6JxjF");
    }

    @Bean
    PeerGroup peerGroup(NetworkParameters networkParameters,
                        BlockChain blockChain,
                        Wallet wallet,
                        @Value("${bitcoin.software.name:sender}") String name,
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

    @Autowired
    PeerGroup peerGroup;
    @Autowired
    Address recipientAddress;
    @Autowired
    Wallet wallet;

    @PostConstruct
    public void postConstruct() throws ExecutionException, InterruptedException, UnknownHostException, InsufficientMoneyException {
        peerGroup.startAndWait();
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);
        BigInteger btcToSend = new BigInteger(String.valueOf(10));
        Transaction sendTxn = wallet.sendCoins(peer, Wallet.SendRequest.to(recipientAddress, btcToSend));
        log.info(">>>> TXN: {}", sendTxn);
    }

    public static void main(String[] args) {
        SpringApplication.run(SenderApplication.class, args);
    }
}
