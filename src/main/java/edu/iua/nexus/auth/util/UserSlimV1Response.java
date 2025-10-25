package edu.iua.nexus.auth.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * DTO reducido que viaja al frontend después del login interno,
 * suficiente para poblar la sesión (username, email y roles en texto).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSlimV1Response {
    private String username;
    private String email;
    private List<String> roles;
}

