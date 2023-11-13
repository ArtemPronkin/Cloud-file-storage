package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PathNameUtils {

    public String getBackPath(String fullPathName) {
        if (fullPathName.isEmpty()) return "";
        fullPathName = fullPathName.substring(0, fullPathName.lastIndexOf('/'));
        return fullPathName.substring(0, fullPathName.lastIndexOf('/') + 1);
    }

    public String encode(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8)
                .replace('+', ' ');
    }
}
