package com.projektamtai.projekt.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;

@Service
public class SubscriptionService {

    @Value("BH95eyFrwzvQiU1Iz_yjHWxR41P5rIZSpKldHa9in6ncodggarHkO461E6NECMTt3KMWZxpERvWCeHW9lQmrIms")
    private String publicKey;
    @Value("kpnvrM5WDY6Iod51SWSZpjOmi3R_yLN2Wd5dQRw4ZrA")
    private String privateKey;

    private PushService pushService;
    private List<Subscription> subscriptions = new ArrayList<>();

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void subscribe(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    public void unsubscribe(String endpoint) {
        subscriptions.removeIf(s -> endpoint.equals(s.endpoint));
    }

    public void sendNotification(Subscription subscription, String message) {
        try {
            pushService.send(new Notification(subscription, message));
        } catch (Exception ignored) {
        }
    }

    public void sendNotificationToAll(String message) {
        subscriptions.forEach(subscription -> {
            sendNotification(subscription, message);
        });
    }
}