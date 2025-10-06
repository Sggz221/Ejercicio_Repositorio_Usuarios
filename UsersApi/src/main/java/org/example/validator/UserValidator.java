package org.example.validator;


import org.example.exceptions.UserException;
import org.example.models.User;

public class UserValidator {
    private UserValidator() {}

    public static boolean validate(User user) throws UserException.InvalidException {
        if(user.getName() == null || user.getName().isEmpty()) throw new UserException.InvalidException("El nombre de usuario no puede ser nulo o estar vacio");

        if(user.getUsername() == null || user.getUsername().isEmpty()) throw new UserException.InvalidException("El username no puede ser nulo o estar vacio");

        if(user.getEmail() == null || user.getEmail().isEmpty()) throw new UserException.InvalidException("El email no puede ser nulo o estar vacio");

        return true;
    }
}
