package edu.espe.springlab.repository;

import edu.espe.springlab.domain.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository repo;

    private Student build(String name, String email, boolean active) {
        Student s = new Student();
        s.setFullName(name);
        s.setEmail(email);
        s.setBirthDate(LocalDate.of(2000, 1, 1));
        s.setActive(active);
        return s;
    }

    // -------------------------------------------------------
    // SAVE + FIND BY EMAIL
    // -------------------------------------------------------
    @Test
    void shouldSaveAndFindByEmail() {
        repo.save(build("Test User", "test@mail.com", true));

        var result = repo.findByEmail("test@mail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Test User");
    }

    // -------------------------------------------------------
    // EXISTS BY EMAIL
    // -------------------------------------------------------
    @Test
    void shouldReturnTrueIfEmailExists() {
        repo.save(build("User A", "exists@mail.com", true));

        boolean exists = repo.existsByEmail("exists@mail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() {
        boolean exists = repo.existsByEmail("missing@mail.com");

        assertThat(exists).isFalse();
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------
    @Test
    void shouldFindById() {
        Student saved = repo.save(build("User", "id@mail.com", true));

        var found = repo.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("id@mail.com");
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------
    @Test
    void shouldFindAllStudents() {
        repo.save(build("A", "a@mail.com", true));
        repo.save(build("B", "b@mail.com", true));

        var list = repo.findAll();

        assertThat(list).hasSize(2);
    }

    // -------------------------------------------------------
    // DELETE BY ID
    // -------------------------------------------------------
    @Test
    void shouldDeleteById() {
        Student saved = repo.save(build("User", "delete@mail.com", true));

        repo.deleteById(saved.getId());

        assertThat(repo.findById(saved.getId())).isNotPresent();
    }

    // -------------------------------------------------------
    // COUNT ACTIVE / INACTIVE
    // -------------------------------------------------------


    // -------------------------------------------------------
    // EMAIL ÚNICO (muy común en examen)
    // -------------------------------------------------------
    @Test
    void shouldNotAllowDuplicatedEmail() {
        repo.save(build("First", "dup@mail.com", true));

        Student duplicated = build("Second", "dup@mail.com", true);

        assertThatThrownBy(() -> {
            repo.saveAndFlush(duplicated);
        }).isInstanceOf(Exception.class); // ConstraintViolationException / DataIntegrityViolationException
    }
}
