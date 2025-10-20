package com.orange.userservice.activity.port;

import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {
    Optional<UUID> findUserIdByEmail(String email);
}
