package com.projektamtai.projekt.database;

import com.projektamtai.projekt.auth.Authorize;
import com.projektamtai.projekt.models.ExtinguisherModel;
import com.projektamtai.projekt.models.UserModel;
import com.projektamtai.projekt.repositories.ExtinguisherRepository;
import com.projektamtai.projekt.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/database")
public class DatabaseService {
    Authorize authorize;
    UserRepository userRepository;
    ExtinguisherRepository extinguisherRepository;

    public DatabaseService(Authorize authorize, UserRepository userRepository, ExtinguisherRepository extinguisherRepository) {
        this.authorize = authorize;
        this.userRepository = userRepository;
        this.extinguisherRepository = extinguisherRepository;
    }

    @GetMapping("/inspection")
    public ResponseEntity<?> inspection(@RequestHeader("Authorization") String token, @RequestParam String id) {
        if(!authorize.isAuthorized(token, 0L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(extinguisher.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
    }

    @PatchMapping("/use")
    public ResponseEntity<String> use(@RequestHeader("Authorization") String token, @RequestParam String id) {
        if(!authorize.isAuthorized(token, 0L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        if(extinguisher.get().getUsed())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gaśnica już zużyta.");
        extinguisher.get().setUsed(true);
        extinguisherRepository.save(extinguisher.get());
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica zużyta.");

        //Wysyłaj PUSH do frontu
    }

    @PatchMapping("/move")
    public ResponseEntity<String> move(@RequestHeader("Authorization") String token, @RequestParam String id, @RequestParam String location, @RequestParam(required = false) String notes) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        if(extinguisher.get().getUsed())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie można przenieść zużytej gaśnicy.");
        if(extinguisher.get().getExpire().isAfter(Instant.now()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Przeterminowana gaśnica nie moze zostać przeniesiona.");
        if(extinguisher.get().getLocation().equals(location))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gaśnica już znajduje się w tym miejscu.");
        extinguisher.get().setLocation(location);
        extinguisher.get().setNotes(notes);
        extinguisherRepository.save(extinguisher.get());
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica przeniesiona.");
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader("Authorization") String token, @RequestParam Instant expire, @RequestParam(required = false) String customID) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<String>extinguisherIDs = extinguisherRepository.findAll().stream().map(ExtinguisherModel::getId).toList();
        if(extinguisherIDs.contains(customID))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gaśnica o takim ID już istnieje.");
        for(int i = 1;i < 10000;i++)
            if(!extinguisherIDs.contains("G"+i)) {
                extinguisherRepository.save(new ExtinguisherModel("G" + i, expire, false, null, null));
                return ResponseEntity.status(HttpStatus.OK).body(i);
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Za dużo gaśnic w bazie danych.");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestHeader("Authorization") String token, @RequestParam String id) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        Optional<ExtinguisherModel> extinguisher = extinguisherRepository.findById(id);
        if(extinguisher.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono takiej gaśnicy.");
        extinguisherRepository.delete(extinguisher.get());
        return ResponseEntity.status(HttpStatus.OK).body("Gaśnica usunięta.");
    }

    @GetMapping("/list_useless")
    public ResponseEntity<?> listUseless(@RequestHeader("Authorization") String token) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<ExtinguisherModel> useless = extinguisherRepository.findAll().stream().filter(extinguisherModel -> extinguisherModel.getUsed() || extinguisherModel.getExpire().isAfter(Instant.now())).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(useless);
    }

    @GetMapping("/get_users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String token) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<UserModel> users = userRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PostMapping("/add_user")
    public ResponseEntity<?> addUser(@RequestHeader("Authorization") String token, @RequestParam String username, @RequestParam Long role) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        if(role != 0L && role != 1L)
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

    @PostMapping("/remove_user")
    public ResponseEntity<?> removeUser(@RequestHeader("Authorization") String token, @RequestParam String username) {
        if(!authorize.isAuthorized(token, 1L))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak uprawnień.");
        List<UserModel> users = userRepository.findAll();
        for(UserModel user : users)
            if(user.getUsername().equals(username)) {
                userRepository.delete(user);
                return ResponseEntity.status(HttpStatus.OK).body("Użytkownik usunięty.");
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Taki użytkownik nie istnieje.");
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
}
