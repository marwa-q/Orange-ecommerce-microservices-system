package com.orange.userservice.activity.adapter;

import com.orange.userservice.activity.port.UserLookupPort;
import com.orange.userservice.user.repo.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserLookupAdapter implements UserLookupPort {

    private final UserRepository userRepository;

    public UserLookupAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(u -> u.getUuid());
    }
}
