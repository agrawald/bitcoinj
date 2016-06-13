package dj.poc.bitcoinj.service;

import com.google.bitcoin.core.PeerGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;

/**
 * Created by proy on 13/06/2016.
 */
@Slf4j
@Service
public class PeerSvc {
    @Autowired
    private PeerGroup peerGroup;

    @Autowired
    public PeerSvc(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
    }

    @PostConstruct
    public void postConstruct() throws ExecutionException, InterruptedException {
        log.info("PeerGroup: {}", peerGroup.startAndWait());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("PeerGroup: {}", this.peerGroup.stopAndWait());
    }
}
