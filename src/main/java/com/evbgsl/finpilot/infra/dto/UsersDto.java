package com.evbgsl.finpilot.infra.dto;

import java.util.ArrayList;
import java.util.List;

public class UsersDto {
    public List<UserDto> users = new ArrayList<>();

    public static class UserDto {
        public String login;
        public String passwordHash;
    }
}
