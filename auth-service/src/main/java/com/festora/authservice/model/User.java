package com.festora.authservice.model;

import com.festora.authservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data

@Document(collection = "user")
public class User {
    @Id
    private String userId;

    private Role role;
    private String mobileNum;
    private String userName;
    private String password;
    @Indexed(unique = true)
    private String email;
    private boolean enabled;
    private long dateCreated;
    private long lastModified;
}
