package keifer.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import keifer.api.model.Login;
import keifer.api.model.User;
import keifer.converter.UserConverter;
import keifer.persistence.UserRepository;
import keifer.persistence.model.UserEntity;
import keifer.service.model.YAMLConfig;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private YAMLConfig yamlConfig;

    public UserServiceImpl(@NonNull UserRepository userRepository,
                           @NonNull UserConverter userConverter,
                           YAMLConfig yamlConfig) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.yamlConfig = yamlConfig;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll().stream().map(userConverter::convert).collect(Collectors.toList());
    }

    @Override
    public User getUser(Long userId) {
        return userConverter.convert(userRepository.findOneById(userId));
    }

    @SneakyThrows
    @Override
    public User login(Login login) {

        if (login.getUsername() == null || login.getPassword() == null) {
            throw new ServletException("Invalid login.");
        }

        String username = login.getUsername();
        String password = login.getPassword();

        UserEntity userEntity = userRepository.findOneByUsername(username);

        if (userEntity == null) {
            throw new ServletException("Username not found.");
        }

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(userEntity.getSalt());

        if (!Arrays.equals(md.digest(password.getBytes(StandardCharsets.UTF_8)), userEntity.getPassword())) {
            throw new ServletException("Invalid login.");
        }

        String token = Jwts.builder()
                .setSubject(username)
                .claim("id", userEntity.getId())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, yamlConfig.getSecretKey())
                .compact();

        User user = userConverter.convert(userEntity);
        user.setToken(token);

        return user;
    }

    @SneakyThrows
    @Override
    public User saveUser(User user) {

        String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();

        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(email)) {
            throw new ServletException("Invalid registration.");
        }

        if (userRepository.findOneByUsername(username) != null) {
            throw new ServletException("Username: " + username + " already exists.");
        }

        if (userRepository.findOneByEmail(email) != null) {
            throw new ServletException("Email: " + email + " is already in use.");
        }

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(md.digest(password.getBytes(StandardCharsets.UTF_8)))
                .salt(salt)
                .email(email)
                .build();
        return userConverter.convert(userRepository.save(userEntity));
    }

    @SneakyThrows
    @Override
    public User resetPassword(User user) {

        String username = user.getUsername();
        String password = user.getPassword();

        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(password)) {
            throw new ServletException("Invalid registration.");
        }

        UserEntity userEntity = userRepository.findOneByUsername(username);
        if (userEntity == null) {
            throw new ServletException("Invalid username.");
        }

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        userEntity.setPassword(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        userEntity.setSalt(salt);

        return userConverter.convert(userRepository.save(userEntity));

    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
