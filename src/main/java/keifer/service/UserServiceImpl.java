package keifer.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import keifer.api.model.Login;
import keifer.api.model.User;
import keifer.converter.UserConverter;
import keifer.persistence.UserRepository;
import keifer.persistence.model.UserEntity;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
//    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(@NonNull UserRepository userRepository,
                           @NonNull UserConverter userConverter) {
//                           @NonNull PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
//        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(Login login) throws ServletException {

        String jwtToken;

        if (login.getUsername() == null || login.getPassword() == null) {
            throw new ServletException("Please fill in username and password.");
        }

        String username = login.getUsername();
        String password = login.getPassword();

        UserEntity userEntity = userRepository.findOneByUsername(username);

        if (userEntity == null) {
            throw new ServletException("Username not found.");
        }

        // TODO encrypt
        String pwd = userEntity.getPassword();

        if (!password.equals(pwd)) {
            throw new ServletException("Invalid login. Please check your name and password.");
        }

        jwtToken = Jwts.builder().setSubject(username).claim("id", userEntity.getId()).setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();

        User user  = userConverter.convert(userEntity);
        user.setToken(jwtToken);

        return user;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll().stream().map(userConverter::convert).collect(Collectors.toList());
    }

    @Override
    public User getUser(Long userId) {
        return userConverter.convert(userRepository.findOneById(userId));
    }

    @Override
    public User saveUser(User user) {

        //TODO validations

        UserEntity userEntity = UserEntity.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .build();
        return userConverter.convert(userRepository.save(userEntity));
    }

    @Override
    public void deleteUser(Long userId) {
        //TODO
    }

}
