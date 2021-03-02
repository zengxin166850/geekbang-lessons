package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.InMemoryUserRepository;
import org.geektimes.web.mvc.annotation.Autowired;
import org.geektimes.web.mvc.annotation.Component;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    InMemoryUserRepository repository;
    @Override
    public boolean register(User user) {
        return repository.save(user);
    }

    @Override
    public boolean deregister(User user) {
        return repository.deleteById(user.getId());
    }

    @Override
    public boolean update(User user) {
        return repository.save(user);
    }

    @Override
    public User queryUserById(Long id) {
        return repository.getById(id);
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return repository.getByNameAndPassword(name,password);
    }
}
