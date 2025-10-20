package com.orange.userservice.activity.net;

import jakarta.servlet.http.HttpServletRequest;

public interface IpResolver {
    String resolveClientIp(HttpServletRequest request);
}
