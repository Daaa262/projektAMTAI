package com.projektamtai.projekt.database;

import com.projektamtai.projekt.auth.Authorize;
import com.projektamtai.projekt.models.ExtinguisherModel;
import com.projektamtai.projekt.models.UserModel;
import com.projektamtai.projekt.repositories.ExtinguisherRepository;
import com.projektamtai.projekt.repositories.UserRepository;
import com.projektamtai.projekt.services.SubscriptionService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DatabaseService {
    Authorize authorize;
    UserRepository userRepository;
    ExtinguisherRepository extinguisherRepository;
    SubscriptionService subscriptionService;

    public DatabaseService(Authorize authorize, UserRepository userRepository, ExtinguisherRepository extinguisherRepository, SubscriptionService subscriptionService) {
        this.authorize = authorize;
        this.userRepository = userRepository;
        this.extinguisherRepository = extinguisherRepository;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/inspection")
    public ResponseEntity<?> inspection(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String id) {
        if(token == null || !authorize.isAuthorized(token, 0))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(extinguisher.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
    }

    @PatchMapping("/use")
    public ResponseEntity<String> use(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String id) {
        if(token == null || !authorize.isAuthorized(token, 0))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        if(extinguisher.get().getUsed())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gaśnica już zużyta.");
        extinguisher.get().setUsed(true);
        extinguisherRepository.save(extinguisher.get());
        subscriptionService.sendNotificationToAll("Gaśnica \"" + id + "\" w " + (extinguisher.get().getLocation() == null?"magazynie":extinguisher.get().getLocation()) + " została zużyta przez " + authorize.getUsernameFromToken(token));
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica zużyta.");
    }

    @PatchMapping("/move")
    public ResponseEntity<String> move(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String id, @RequestParam(required = false, defaultValue = "") String location, @RequestParam(required = false) String notes) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        if(extinguisher.get().getUsed())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie można przenieść zużytej gaśnicy.");
        if(extinguisher.get().getExpire().isBefore(LocalDate.now()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Przeterminowana gaśnica nie moze zostać przeniesiona.");
        extinguisher.get().setLocation(location);
        extinguisher.get().setNotes(notes);
        extinguisherRepository.save(extinguisher.get());
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica przeniesiona.");
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam LocalDate expire, @RequestParam(required = false) String location, @RequestParam(required = false) String notes, @RequestParam(required = false) String customID) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<String>extinguisherIDs = extinguisherRepository.findAll().stream().map(ExtinguisherModel::getId).toList();
        if(customID != null){
            if(extinguisherIDs.contains(customID))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gaśnica o takim ID już istnieje.");
            extinguisherRepository.save(new ExtinguisherModel(customID, expire, false, location, notes));
            return ResponseEntity.status(HttpStatus.OK).body(customID);
        }
        for(int i = 1;i < 10000;i++)
            if(!extinguisherIDs.contains("G"+i)) {
                extinguisherRepository.save(new ExtinguisherModel("G" + i, expire, false, location, notes));
                return ResponseEntity.status(HttpStatus.OK).body("G" + i);
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Za dużo gaśnic w bazie danych.");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> remove(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String id) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        extinguisherRepository.delete(extinguisher.get());
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica usunięta.");
    }

    @GetMapping("/list_useless")
    public ResponseEntity<?> listUseless(@RequestHeader(value = "Authorization", required = false) String token) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<ExtinguisherModel> useless = extinguisherRepository.findAll().stream().filter(extinguisherModel -> extinguisherModel.getUsed() || extinguisherModel.getExpire().isBefore(LocalDate.now())).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(useless);
    }

    @GetMapping("/get_users")
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<UserModel> users = userRepository.findAll();
        if(authorize.isAuthorized(token, 2))
            return ResponseEntity.status(HttpStatus.OK).body(users);
        else for(UserModel user : users)
            if(user.getRole() > 0 && !user.getUsername().equals(authorize.getUsernameFromToken(token)))
                user.setPassword(null);

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PostMapping("/add_user")
    public ResponseEntity<?> addUser(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String username, @RequestParam Integer role) {
        if(token == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        if(role == 1 && !authorize.isAuthorized(token, 2))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        if(!authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        if(role != 0 && role != 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny rodzaj uprawnień.");
        List<UserModel> users = userRepository.findAll();
        for(UserModel user : users)
            if(user.getUsername().equals(username))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Użytkownik o takiej nazwie już istnieje.");
        String password = generatePassword();
        UserModel newUser = new UserModel(username, password, role);
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @DeleteMapping("/remove_user")
    public ResponseEntity<?> removeUser(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String username) {
        if(token == null || !authorize.isAuthorized(token, 1))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<UserModel> users = userRepository.findAll();
        for(UserModel user : users)
            if(user.getUsername().equals(username)) {
                if(user.getRole() == 2 || user.getRole() == 1 && !authorize.isAuthorized(token, 2))
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
                userRepository.delete(user);
                return ResponseEntity.status(HttpStatus.OK).body("Użytkownik usunięty.");
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Taki użytkownik nie istnieje.");
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestBody Subscription subscription) {
        subscriptionService.subscribe(subscription);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody String endpoint) {
        endpoint = endpoint.replace("\"", "");
        subscriptionService.unsubscribe(endpoint);
    }

    private String generatePassword() {
        final int length = 12;
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    @GetMapping("/test")
    public String test() {
        return "DZIAŁA!!!";
    }
}
